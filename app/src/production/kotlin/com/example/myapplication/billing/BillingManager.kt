package com.example.myapplication.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val entitlementManager: EntitlementManager
) : PurchasesUpdatedListener {

    private val _productDetailsList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetailsList: StateFlow<List<ProductDetails>> = _productDetailsList.asStateFlow()

    private val _billingErrorMessage = MutableStateFlow<String?>(null)
    val billingErrorMessage: StateFlow<String?> = _billingErrorMessage.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient setup successfully finished")
                    queryProductDetails()
                    queryPurchases()
                } else {
                    Log.e(TAG, "BillingClient setup failed: ${billingResult.debugMessage}")
                    _billingErrorMessage.value = billingResult.debugMessage
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient disconnected. Attempting reconnect...")
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_FAMILY_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_FAMILY_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && detailsList.isNotEmpty()) {
                _productDetailsList.value = detailsList
                Log.d(TAG, "Product details retrieved: ${detailsList.size} products")
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchSubscriptionFlow(activity: Activity, productId: String = PRODUCT_ID_FAMILY_MONTHLY) {
        val details = _productDetailsList.value.firstOrNull { it.productId == productId }
            ?: _productDetailsList.value.firstOrNull()

        if (details == null) {
            _billingErrorMessage.value = "구독 상품 정보를 불러오지 못했습니다. 네트워크 상태를 확인해주세요."
            return
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingErrorMessage.value = "결제 창을 열지 못했습니다: ${result.debugMessage}"
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled billing flow")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            _billingErrorMessage.value = billingResult.debugMessage
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                        entitlementManager.updateFamilySubscriptionStatus(isHost = true)
                    }
                }
            } else {
                entitlementManager.updateFamilySubscriptionStatus(isHost = true)
            }
        }
    }

    fun queryPurchases() {
        if (!billingClient.isReady) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSubscription = purchases.any { purchase ->
                    (purchase.products.contains(PRODUCT_ID_FAMILY_MONTHLY) || purchase.products.contains(PRODUCT_ID_FAMILY_YEARLY)) &&
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (hasActiveSubscription) {
                    entitlementManager.updateFamilySubscriptionStatus(isHost = true)
                }
            }
        }
    }

    companion object {
        private const val TAG = "IrisFitBilling"
        const val PRODUCT_ID_FAMILY_MONTHLY = "family_pass_monthly"
        const val PRODUCT_ID_FAMILY_YEARLY = "family_pass_yearly"
    }
}
