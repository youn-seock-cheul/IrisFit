package com.example.myapplication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.BuildConfig

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("개인정보 처리 안내") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "화면 휴식 타이머는 계정 없이 동작하며 카메라·마이크·위치·다른 앱 실행 정보를 사용하지 않습니다. " +
                        "알림 예약 시각과 재부팅 구분값만 앱 전용 저장소에 보관합니다. 앱 자체 서버로 전송하지 않습니다.\n\n" +
                        "알림 권한은 휴식 알림 표시에만 사용합니다. 거절해도 앱 화면에서 타이머를 사용할 수 있습니다. " +
                        "Google Play 업데이트 확인을 선택하면 외부 스토어 또는 브라우저가 열리며 해당 서비스의 정책이 적용됩니다.\n\n" +
                        "설정 기록은 다음 예약으로 덮어쓰거나 타이머 종료 시 삭제합니다. 이전 버전의 보정값·점수·대화 기록이 남아 있다면 " +
                        "‘이 기기의 앱 기록 전체 삭제’에서 함께 지울 수 있습니다. 앱 데이터는 클라우드 백업과 기기 이전 대상에서 제외합니다."
                )
                if (BuildConfig.DEBUG) {
                    Text(
                        "\n\n개발용 카메라 실험은 별도 선택 후 사용합니다. 전면 카메라 프레임을 기기 내 ML Kit 얼굴 분석에 전달하고 " +
                            "앱은 영상 파일로 저장하거나 서버에 업로드하지 않습니다. 미검출은 졸음을 의미하지 않습니다. " +
                            "알림·앱에서 중지하거나 화면이 꺼지면 종료하고, 최대 2분 후 자동 종료합니다. 연구 기능은 정식 판매 빌드에서 제외됩니다."
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }
    )
}
