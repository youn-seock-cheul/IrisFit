package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.R
import com.example.myapplication.session.GuidedSessionViewModel

@Composable
fun PublicGlobalChallengeCard(sessionModel: GuidedSessionViewModel) {
    var showBetaNoticeDialog by remember { mutableStateOf(false) }

    if (showBetaNoticeDialog) {
        BetaNoticeDialog(sessionModel) { showBetaNoticeDialog = false }
    }

    IrisCard {
        IrisCardHeader(
            icon = R.drawable.ic_ms_public,
            title = sessionModel.tr("global_challenge_title"),
            subtitle = sessionModel.tr("global_challenge_sub"),
            badge = sessionModel.tr("badge_beta")
        )
        IrisBodyText(sessionModel.tr("global_challenge_body"))
        IrisPrimaryButton(
            text = sessionModel.tr("global_challenge_join"),
            icon = R.drawable.ic_ms_arrow_forward,
            onClick = { showBetaNoticeDialog = true }
        )
    }
}