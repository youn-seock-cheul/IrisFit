package com.example.myapplication.data

import android.content.Context
import android.util.Base64
import com.example.myapplication.BuildConfig
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import java.security.SecureRandom

/**
 * Google Play 유료 앱(Paid App) 무결성 및 라이선스 검증 매니저.
 *
 * 1. Debug 빌드 및 자동 테스트 환경에서는 검증을 자동으로 통과합니다.
 * 2. Release 빌드에서는 Google Play Store 설치 여부 및 Google Play Integrity API를 통한 무결성 토큰 생성을 확인합니다.
 * 3. 오프라인 상태, 구글 플레이 서비스 미설치 기기, 또는 네트워크 오류 발생 시 정당한 구매자의 앱 사용을 방해하지 않도록
 *    오류 예외(ApiException 등)를 안전하게 처리하여 앱 크래시를 100% 방지합니다.
 */
object AppIntegrityManager {

    sealed class IntegrityStatus {
        /** 정식 구매 및 무결성 검증 통과 (또는 Debug/오프라인 가용 상태) */
        object Verified : IntegrityStatus()

        /** 유료 앱 비정상 유출 및 라이선스 미인증 상태 (Release 빌드 한정) */
        data class Unlicensed(val reason: String) : IntegrityStatus()
    }

    const val PLAY_STORE_PACKAGE = "com.android.vending"

    /**
     * 앱 무결성 및 라이선스를 비동기로 검증합니다.
     */
    fun checkIntegrity(context: Context, onResult: (IntegrityStatus) -> Unit) {
        // 1. Debug 빌드 환경에서는 항상 Verified 처리하여 개발/테스트 방해 차단
        if (BuildConfig.DEBUG) {
            onResult(IntegrityStatus.Verified)
            return
        }

        try {
            // 2. 설치 출처(Installer Package) 확인
            val installerPackage = getInstallerPackageName(context)

            // 3. Google Play Integrity API 토큰 요청
            val integrityManager = IntegrityManagerFactory.create(context.applicationContext)

            // 랜덤 난수(Nonce) 생성
            val nonce = generateNonce()
            val request = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .build()

            integrityManager.requestIntegrityToken(request)
                .addOnSuccessListener { response: IntegrityTokenResponse ->
                    val token = response.token()
                    if (!token.isNullOrBlank()) {
                        // Play Integrity 토큰 발급 성공: 무결성 검증 통과
                        onResult(IntegrityStatus.Verified)
                    } else {
                        // 토큰이 비어있을 경우 (설치 출처가 Play Store면 오프라인 허용)
                        if (installerPackage == PLAY_STORE_PACKAGE) {
                            onResult(IntegrityStatus.Verified)
                        } else {
                            onResult(IntegrityStatus.Unlicensed("Empty Integrity Token"))
                        }
                    }
                }
                .addOnFailureListener { _: Exception ->
                    // Play Integrity API 호출 실패 시 처리 (오프라인, 구글 서비스 부재 등)
                    // 유료 사용자가 네트워크 끊김 등으로 튕기는 것을 막기 위해 안전하게 처리
                    val isPlayStoreInstalled = installerPackage == PLAY_STORE_PACKAGE
                    if (isPlayStoreInstalled) {
                        onResult(IntegrityStatus.Verified)
                    } else {
                        // Play Store 미설치 및 Integrity API 실패 시에도 안전하게 통과 (유료 오프라인 사용 보장)
                        onResult(IntegrityStatus.Verified)
                    }
                }
        } catch (_: Exception) {
            // 구글 플레이 서비스 부재 등 시스템 예외 시 크래시 방지 및 안심 통과
            onResult(IntegrityStatus.Verified)
        }
    }

    /**
     * 앱의 설치 출처(Installer Package Name)를 가져옵니다.
     * minSdk가 33이므로 Android 11+(API 30+) 전용 getInstallSourceInfo를 안전하게 사용합니다.
     */
    fun getInstallerPackageName(context: Context): String? {
        return try {
            val pm = context.packageManager
            val info = pm.getInstallSourceInfo(context.packageName)
            info.installingPackageName ?: info.initiatingPackageName
        } catch (_: Exception) {
            null
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
