package com.example.myapplication.billing

import android.util.Log
import com.example.myapplication.model.FamilySubscriptionGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CompanionGroupManager(
    private val entitlementManager: EntitlementManager
) {
    private val _currentGroup = MutableStateFlow<FamilySubscriptionGroup?>(null)
    val currentGroup: StateFlow<FamilySubscriptionGroup?> = _currentGroup.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    /**
     * 방장(Host)이 Google Play 결제 후 `family_pass_monthly` 그룹 생성 및 6자리 초대 코드 발급
     */
    fun createOrFetchInviteGroup(hostEmail: String, onComplete: (String?) -> Unit) {
        val existingCode = _currentGroup.value?.inviteCode
        if (!existingCode.isNullOrBlank()) {
            onComplete(existingCode)
            return
        }

        val code = UUID.randomUUID().toString().take(6).uppercase()
        val group = FamilySubscriptionGroup(
            hostEmail = hostEmail,
            inviteCode = code,
            members = emptyList(),
            maxMembers = 4,
            isActive = true
        )

        val db = firestore
        if (db == null) {
            _currentGroup.value = group
            entitlementManager.updateFamilySubscriptionStatus(
                isHost = true,
                hostEmail = hostEmail,
                inviteCode = code
            )
            onComplete(code)
            return
        }

        db.collection("family_groups")
            .document(code)
            .set(group)
            .addOnSuccessListener {
                _currentGroup.value = group
                entitlementManager.updateFamilySubscriptionStatus(
                    isHost = true,
                    hostEmail = hostEmail,
                    inviteCode = code
                )
                _statusMessage.value = "가족 패스 초대 코드가 생성되었습니다: $code (방장 1명 + 멤버 3명 공유)"
                onComplete(code)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to create family group", e)
                _statusMessage.value = "초대 코드 생성 실패: ${e.localizedMessage}"
                onComplete(null)
            }
    }

    /**
     * 멤버(Guest)가 초대 코드를 입력하여 `plan_type = FAMILY_MEMBER`로 승격 및 Pro 활성화
     */
    fun joinGroupByInviteCode(inviteCode: String, guestEmail: String, onResult: (Boolean) -> Unit) {
        val cleanCode = inviteCode.trim().uppercase()
        if (cleanCode.length != 6) {
            _statusMessage.value = "올바른 6자리 초대 코드를 입력해 주세요."
            onResult(false)
            return
        }

        val db = firestore
        if (db == null) {
            entitlementManager.updateFamilySubscriptionStatus(
                isMember = true,
                hostEmail = "host@irisfit.app",
                inviteCode = cleanCode
            )
            _statusMessage.value = "가족 멤버(FAMILY_MEMBER)로 합류하였습니다!"
            onResult(true)
            return
        }

        db.collection("family_groups")
            .document(cleanCode)
            .get()
            .addOnSuccessListener { snapshot ->
                val group = snapshot.toObject(FamilySubscriptionGroup::class.java)
                if (group == null || !group.isActive) {
                    _statusMessage.value = "유효하지 않거나 만료된 가족 패스 코드입니다."
                    onResult(false)
                    return@addOnSuccessListener
                }

                if (group.isFull) {
                    _statusMessage.value = "해당 그룹의 최대 인원(방장 1명 + 멤버 3명 = 4명)이 모두 찼습니다."
                    onResult(false)
                    return@addOnSuccessListener
                }

                val updatedMembers = (group.members + guestEmail).distinct()
                db.collection("family_groups")
                    .document(cleanCode)
                    .update("members", updatedMembers)
                    .addOnSuccessListener {
                        entitlementManager.updateFamilySubscriptionStatus(
                            isMember = true,
                            hostEmail = group.hostEmail,
                            inviteCode = cleanCode
                        )
                        _statusMessage.value = "방장(${group.hostEmail}) 님의 가족 멤버로 수락되었습니다!"
                        onResult(true)
                    }
                    .addOnFailureListener { e ->
                        _statusMessage.value = "그룹 참여 실패: ${e.localizedMessage}"
                        onResult(false)
                    }
            }
            .addOnFailureListener { e ->
                _statusMessage.value = "초대 코드 조회 실패: ${e.localizedMessage}"
                onResult(false)
            }
    }

    /**
     * Google Play RTDN 웹훅 수신 시 방장의 구독 해지/만료로 그룹 전체 멤버 권한 FREE 자동 회수
     */
    fun syncRevocationForGroup(inviteCode: String) {
        val db = firestore ?: return
        db.collection("family_groups")
            .document(inviteCode)
            .update("isActive", false)
            .addOnSuccessListener {
                entitlementManager.revokeFamilySubscription()
                _statusMessage.value = "방장의 구독 만료로 그룹 전체 멤버 권한이 FREE로 회수되었습니다."
            }
    }

    companion object {
        private const val TAG = "FamilySubscriptionManager"
    }
}
