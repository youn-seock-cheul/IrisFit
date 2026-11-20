package com.example.myapplication.model

data class FamilySubscriptionGroup(
    val hostEmail: String = "",
    val inviteCode: String = "",
    val members: List<String> = emptyList(), // Max 3 guest members (Total 4 including host)
    val maxMembers: Int = 4,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalCount: Int get() = members.size + 1
    val isFull: Boolean get() = totalCount >= maxMembers
}

typealias CompanionGroup = FamilySubscriptionGroup
