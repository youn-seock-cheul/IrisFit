package com.example.myapplication.session

object AppLocalization {

    private val stringsKo = mapOf(
        "nav_home" to "홈",
        "nav_gym" to "눈 운동 존",
        "nav_bluelight" to "시력 보호",
        "nav_ai" to "AI 코치",
        "nav_settings" to "설정",
        "home_title" to "IrisFit",
        "home_subtitle" to "눈 건강 만보기 & 휴식 습관",
        "battery_card_title" to "IrisFit 눈 건강 배터리",
        "battery_card_sub" to "실시간 안구 피로도 측정 및 맞춤 휴식 가이드",
        "tap_mascot" to "마스코트를 탭하여 즉시 휴식 시작!",
        "screen_time_label" to "오늘 화면 응시 시간",
        "rest_completed" to "휴식 완료",
        "eye_exercise" to "눈 운동",
        "fmt_rest_completed" to "%d회 (%d초)",
        "fmt_eye_exercise" to "%d회",
        "tap_to_rest" to "탭하여 30초 휴식",
        "tap_to_exercise" to "탭하여 시선 추적",
        "legend_danger" to "위험 (<45%)",
        "legend_warning" to "경고 (45-64%)",
        "legend_caution" to "주의 (65-84%)",
        "legend_optimal" to "최상 (85%+)",
        "trend_weekly_title" to "주간 눈 건강 배터리 트렌드",
        "trend_weekly_sub" to "최근 7일간 일자별 평균 눈 컨디션 추이",
        "trend_monthly_title" to "월간 눈 건강 배터리 트렌드",
        "trend_monthly_sub" to "최근 4주간 주차별 평균 눈 컨디션 추이",
        "trend_daily_title" to "일간 눈 건강 배터리 트렌드",
        "trend_daily_sub" to "오늘 24시간 시간대별 눈 컨디션 변화 추세선",
        "tab_daily" to "일간",
        "tab_weekly" to "주간 (7일)",
        "tab_monthly" to "월간 (4주)",
        "btn_refresh_chart" to "최신 데이터 재조회",
        "cheer_header" to "눈 건강 응원 릴레이",
        "cheer_sub" to "1,248명 동료 함께 휴식 중",
        "cheer_send" to "나도 응원 보내기 (+10P)",
        "cheer_done" to "응원 전달 완료!",
        "goals_header" to "오늘의 눈 건강 목표 & 트로피",
        "goals_sub" to "목표 달성 시 축하 마일리지 자동 지급",
        "goal_1" to "1. 눈 배터리 85% 이상 유지",
        "goal_2" to "2. 하루 30초 눈 휴식 3회 달성",
        "goal_3" to "3. 7일 연속 휴식 스트릭 성공",
        "goal_achieved" to "달성",
        "goal_progress" to "진행 중",
        "bluelight_title" to "삼성 갤럭시 내장 '시력 보호 화면'",
        "bluelight_desc" to "스마트폰 디스플레이 하드웨어 수준에서 청색광(블루라이트)을 완벽히 차단하여 안구 건조증과 망막 피로를 예방합니다.",
        "bluelight_banner_title" to "삼성 갤럭시 내장 '시력 보호 화면(Eye Comfort Shield)'",
        "bluelight_banner_desc" to "소프트웨어 앱 오버레이 필터는 상태바나 시스템 UI를 완벽히 커버하지 못하며 기기 보안 정책에 의해 차단될 수 있습니다. 스마트폰 디스플레이 하드웨어 수준에서 블루라이트를 완벽히 차단하는 '시력 보호 화면'을 시스템 설정에서 켜시는 것이 가장 효과적입니다.",
        "bluelight_btn_settings" to "시스템 '시력 보호 화면' 설정 바로가기",
        "bluelight_tip_title" to "시력 보호 화면 가이드",
        "bluelight_tip_desc" to "• 야간 스마트폰 사용 시 '시력 보호 화면'을 켜면 수면 유도 호르몬(멜라토닌) 분비를 보호할 수 있습니다.\n• 색온도를 따뜻하게 설정하면 색상 왜곡 없이 가장 편안하게 시력을 보호할 수 있습니다.",
        "ai_title" to "IrisFit AI 습관 코치",
        "ai_desc" to "현재 내 눈 건강 배터리 잔량과 실제 화면 사용 시간을 바탕으로 맞춤 진단을 제공합니다.",
        "ai_analysis_title" to "실제 화면 사용 & 배터리 분석",
        "ai_screen_time" to "오늘 실제 화면 응시 시간",
        "ai_battery_remain" to "눈 건강 배터리 잔량",
        "ai_compliance" to "휴식 패턴 실측 준수율",
        "ai_permission_btn" to "실제 화면 사용 시간 측정 권한 허용하기",
        "settings_header" to "설정 및 관리",
        "settings_title" to "설정 및 관리",
        "settings_version_title" to "앱 정보 및 버전",
        "settings_data_title" to "개인정보 및 데이터 관리",
        "settings_data_desc" to "기기에 저장된 모든 눈 건강 마일리지 및 완료 기록을 초기화합니다.",
        "settings_delete_btn" to "기기의 앱 기록 전체 삭제",
        "lang_settings" to "스마트폰 언어 자동 연동",
        "lang_desc" to "스마트폰 시스템 언어를 감지하여 실시간 자동 번역합니다.",
        "lang_current" to "현재 스마트폰 시스템 언어",
        "lang_select_btn" to "언어 선택",
        "lang_dialog_title" to "언어 선택 (Select Language)",
        "noti_settings_title" to "상태 변경 알림 수신",
        "noti_settings_desc" to "눈 배터리 피로도 단계가 바뀔 때 스마트폰 푸시 알림을 수신합니다.",
        "noti_on_toast" to "상태 변경 알림이 켜졌습니다",
        "noti_off_toast" to "상태 변경 알림이 꺼졌습니다",
        "noti_status_title" to "IrisFit 안구 피로도 변경 알림",
        "btn_close" to "닫기",
        "btn_cancel" to "취소",
        "btn_delete" to "삭제",
        "sns_share" to "나의 눈 건강 배터리 SNS 공유하기",
        "gym_header" to "눈 배터리 충전소",

        // Relay & Challenge Keys
        "relay_title" to "진행 중인 릴레이",
        "relay_explore" to "탐색/생성 ➔",
        "relay_baton" to "바통 소지",
        "relay_touch" to "1초 바통 터치",
        "relay_waiting" to "대기 중",
        "guestbook_title" to "친구 응원 방명록 (실시간 롤링)",
        "guestbook_more" to "전체보기",
        "global_challenge_title" to "공개 글로벌 릴레이",
        "global_challenge_sub" to "전국 유저들과 함께 달리는 오픈 챌린지",
        "global_challenge_join" to "참여하기",
        "global_challenge_joined" to "참여 완료",

        // Pose Detector & 4-Tier Fatigue Status
        "pose_handheld" to "손에 쥐고 사용",
        "pose_flat" to "책상 위 놓임",
        "pose_facedown" to "폰 뒤집힘",
        "status_optimal" to "최상 (행복)",
        "status_caution" to "주의 (졸림)",
        "status_warning" to "경고 (불안)",
        "status_danger" to "위험 (패닉)",
        "desc_optimal" to "완벽하게 안전하고 좋은 상태입니다",
        "desc_caution" to "주의 깊게 관찰해야 하는 초기 신호입니다",
        "desc_warning" to "위험으로 번지기 직전의 고조된 상태입니다",
        "desc_danger" to "즉각적인 조치가 필요한 비상 상태입니다!",

        // Gym Tab
        "gym_sub" to "스마트폰 사용으로 닳아버린 눈 건강 배터리를 안구 운동 미니게임으로 급속 충전하세요.",
        "gym_card_banner" to "미니게임 완료 시 배터리 급속 회복",
        "gym_card_banner_sub" to "미니게임을 완료할 때마다 눈 건강 배터리 게이지가 즉시 충전되고 포인트가 쌓입니다.",
        "gym_focus_title" to "1. 원근 조절 모양근 스트레칭 (Focus & Shift)",
        "gym_focus_sub" to "20초 동안 커지는 원을 따라보며 초점을 조절합니다. (+200 Steps / 배터리 충전)",
        "gym_focus_btn" to "원근 조절 미니게임 시작",
        "gym_pursuit_title" to "2. 부드러운 시선 추적 훈련 (Infinity Pursuit)",
        "gym_pursuit_sub" to "무한대(∞) 궤적을 따라 눈동자를 부드럽게 움직여 외안근을 이완합니다. (+250 Steps / 배터리 충전)",
        "gym_pursuit_btn" to "시선 추적 미니게임 시작",
        "gym_exam_title" to "안과 전문 시표 검사 & 리포트",
        "gym_exam_sub" to "란돌트 환, 크로스 실린더 난시, 적녹 다이크롬 검사 수행.",
        "gym_exam_btn" to "전문 시표 검사 시작하기",

        // Eye Gym Game screens
        "gym_screen_title" to "Eye Gym: 포커스 원근 훈련",
        "gym_back" to "← 뒤로",
        "gym_ready_title" to "원근 조절 모양근 스트레칭",
        "gym_ready_desc" to "화면의 커지는 원을 바라보며 멀리/가까이 초점을 조절하세요. 20초 훈련 완료 시 +200 눈 걸음과 +100 포인트를 드립니다!",
        "gym_start" to "훈련 시작하기",
        "gym_playing" to "초점을 맞추고 호흡하세요",
        "gym_gazing" to "응시 중...",
        "gym_stop" to "중단하기",
        "gym_finished" to "훈련 완료!",
        "gym_finished_desc" to "눈 모양근 긴장이 완화되었습니다.\n+200 눈 걸음 수 & +100 포인트 획득!",
        "gym_restart" to "다시 하기",
        "gym_home" to "홈으로 돌아가기",

        // Smooth Pursuit Game
        "pursuit_screen_title" to "Eye Gym: 부드러운 시선 추적 (Infinity)",
        "pursuit_ready_title" to "무한대(∞) 궤적 시선 추적",
        "pursuit_ready_desc" to "화면 속을 부드럽게 움직이는 별 모양 코스를 머리는 고정한 채 눈동자만 따라 움직이세요. 외안근 이완에 탁월하며 완료 시 +250 눈 걸음과 +120 포인트를 드립니다!",
        "pursuit_start" to "시선 추적 훈련 시작",
        "pursuit_playing" to "머리는 고정하고 눈으로만 마커를 쫓아가세요",
        "pursuit_finished" to "시선 추적 완료!",
        "pursuit_finished_desc" to "외안근 부드러운 추적 운동이 완료되었습니다.\n+250 눈 걸음 수 & +120 포인트 획득!"
    )

    private val stringsEn = mapOf(
        "nav_home" to "Home",
        "nav_gym" to "Eye Gym",
        "nav_bluelight" to "Eye Comfort",
        "nav_ai" to "AI Coach",
        "nav_settings" to "Settings",
        "home_title" to "IrisFit",
        "home_subtitle" to "Eye Health & Rest Habits",
        "battery_card_title" to "IrisFit Eye Health Battery",
        "battery_card_sub" to "Real-time eye fatigue tracking & rest guide",
        "tap_mascot" to "Tap mascot for instant rest!",
        "screen_time_label" to "Screen Time Today",
        "rest_completed" to "Rest Completed",
        "eye_exercise" to "Eye Exercise",
        "fmt_rest_completed" to "%d times (%ds)",
        "fmt_eye_exercise" to "%d times",
        "tap_to_rest" to "Tap for 30s rest",
        "tap_to_exercise" to "Tap for Pursuit game",
        "legend_danger" to "Danger (<45%)",
        "legend_warning" to "Warning (45-64%)",
        "legend_caution" to "Caution (65-84%)",
        "legend_optimal" to "Optimal (85%+)",
        "trend_weekly_title" to "Weekly Eye Health Battery Trend",
        "trend_weekly_sub" to "Average eye condition trend over the last 7 days",
        "trend_monthly_title" to "Monthly Eye Health Battery Trend",
        "trend_monthly_sub" to "Average eye condition trend over the last 4 weeks",
        "trend_daily_title" to "Daily Eye Health Battery Trend",
        "trend_daily_sub" to "Hourly 24-hour eye condition trend line today",
        "tab_daily" to "Daily",
        "tab_weekly" to "Weekly (7d)",
        "tab_monthly" to "Monthly (4w)",
        "btn_refresh_chart" to "Refresh Data",
        "cheer_header" to "Eye Health Cheer Relay",
        "cheer_sub" to "1,248 peers resting together",
        "cheer_send" to "Send Cheer (+10P)",
        "cheer_done" to "Cheer Sent!",
        "goals_header" to "Today's Eye Health Goals & Trophies",
        "goals_sub" to "Automatic mileage reward upon goal completion",
        "goal_1" to "1. Maintain 85%+ Eye Battery",
        "goal_2" to "2. Complete 30s Eye Rest 3 Times",
        "goal_3" to "3. 7-Day Consecutive Rest Streak",
        "goal_achieved" to "Achieved",
        "goal_progress" to "In Progress",
        "bluelight_title" to "Galaxy Eye Comfort Shield",
        "bluelight_desc" to "Blocks blue light at the hardware display level to prevent dry eyes and retinal fatigue.",
        "bluelight_banner_title" to "Galaxy Eye Comfort Shield",
        "bluelight_banner_desc" to "Software overlay filters cannot fully cover system status bars and may be blocked by security policies. Turning on 'Eye Comfort Shield' in system settings at the display hardware level is most effective.",
        "bluelight_btn_settings" to "Go to System Eye Comfort Settings",
        "bluelight_tip_title" to "Eye Comfort Guide",
        "bluelight_tip_desc" to "• Turning on 'Eye Comfort Shield' at night protects sleep hormone (melatonin) secretion.\n• Setting warm color temperature protects your vision comfortably.",
        "ai_title" to "IrisFit AI Habit Coach",
        "ai_desc" to "Provides customized diagnosis based on your remaining eye health battery and screen time.",
        "ai_analysis_title" to "Screen Time & Battery Analysis",
        "ai_screen_time" to "Screen Time Today",
        "ai_battery_remain" to "Eye Battery Remaining",
        "ai_compliance" to "Rest Pattern Compliance Rate",
        "ai_permission_btn" to "Allow Screen Time Permission",
        "settings_header" to "Settings & Management",
        "settings_title" to "Settings & Management",
        "settings_version_title" to "App Info & Version",
        "settings_data_title" to "Privacy & Data Management",
        "settings_data_desc" to "Resets all stored eye health mileage and completion records.",
        "settings_delete_btn" to "Delete All Local Records",
        "lang_settings" to "Auto Phone Language Sync",
        "lang_desc" to "Detects phone system language and translates in real time.",
        "lang_current" to "Current Phone System Language",
        "lang_select_btn" to "Select Language",
        "lang_dialog_title" to "Select Language",
        "noti_settings_title" to "Status Change Push Notifications",
        "noti_settings_desc" to "Receive push notifications when eye battery fatigue tier changes.",
        "noti_on_toast" to "Notifications Enabled",
        "noti_off_toast" to "Notifications Disabled",
        "noti_status_title" to "IrisFit Eye Battery Status Update",
        "btn_close" to "Close",
        "btn_cancel" to "Cancel",
        "btn_delete" to "Delete",
        "sns_share" to "Share My Eye Health Battery SNS",
        "gym_header" to "Eye Battery Charging Station",

        // Relay & Challenge Keys
        "relay_title" to "Ongoing Relays",
        "relay_explore" to "Explore/Create ➔",
        "relay_baton" to "Baton Holder",
        "relay_touch" to "1s Baton Touch",
        "relay_waiting" to "Waiting",
        "guestbook_title" to "Friend Cheer Board",
        "guestbook_more" to "View All",
        "global_challenge_title" to "Public Global Challenge",
        "global_challenge_sub" to "Open challenge with users nationwide",
        "global_challenge_join" to "Join",
        "global_challenge_joined" to "Joined",

        // Pose Detector & 4-Tier Fatigue Status
        "pose_handheld" to "In Hand",
        "pose_flat" to "On Desk",
        "pose_facedown" to "Face Down",
        "status_optimal" to "Optimal (Happy)",
        "status_caution" to "Caution (Sleepy)",
        "status_warning" to "Warning (Anxious)",
        "status_danger" to "Danger (Panicked)",
        "desc_optimal" to "Completely safe and optimal condition",
        "desc_caution" to "Early signal needing careful observation",
        "desc_warning" to "Elevated state right before risk",
        "desc_danger" to "Emergency state needing immediate action!",

        // Gym Tab
        "gym_sub" to "Recharge your drained eye health battery with eye exercise mini-games.",
        "gym_card_banner" to "Fast Battery Recovery on Mini-Game Completion",
        "gym_card_banner_sub" to "Completing mini-games recharges your battery gauge instantly and earns points.",
        "gym_focus_title" to "1. Focus & Shift Muscle Stretching",
        "gym_focus_sub" to "Focus on the growing circle for 20s. (+200 Steps / Battery Charge)",
        "gym_focus_btn" to "Start Focus Mini-Game",
        "gym_pursuit_title" to "2. Infinity Pursuit Training",
        "gym_pursuit_sub" to "Follow infinity (∞) path to relax extraocular muscles. (+250 Steps / Battery Charge)",
        "gym_pursuit_btn" to "Start Pursuit Mini-Game",
        "gym_exam_title" to "Professional Visual Chart Exam & Report",
        "gym_exam_sub" to "Perform Landolt C, Cross Cylinder Astigmatism, and Red-Green exams.",
        "gym_exam_btn" to "Start Professional Exam",

        // Eye Gym Game screens
        "gym_screen_title" to "Eye Gym: Focus & Shift",
        "gym_back" to "← Back",
        "gym_ready_title" to "Focus & Shift Stretching",
        "gym_ready_desc" to "Focus on the growing circle on screen. Complete 20s for +200 steps & +100 points!",
        "gym_start" to "Start Training",
        "gym_playing" to "Focus and breathe",
        "gym_gazing" to "Gazing...",
        "gym_stop" to "Stop",
        "gym_finished" to "Training Complete!",
        "gym_finished_desc" to "Eye muscle tension relieved.\n+200 eye steps & +100 points earned!",
        "gym_restart" to "Try Again",
        "gym_home" to "Back to Home",

        // Smooth Pursuit Game
        "pursuit_screen_title" to "Eye Gym: Infinity Pursuit",
        "pursuit_ready_title" to "Infinity (∞) Pursuit",
        "pursuit_ready_desc" to "Follow the smooth moving marker with eyes. Great for extraocular muscle relaxation. (+250 steps, +120 points)",
        "pursuit_start" to "Start Pursuit Training",
        "pursuit_playing" to "Keep head fixed and follow marker",
        "pursuit_finished" to "Pursuit Complete!",
        "pursuit_finished_desc" to "Smooth pursuit exercise complete.\n+250 eye steps & +120 points earned!"
    )

    fun resolveEffectiveLang(selectedCode: String = ""): String {
        val primaryLocale = try {
            android.content.res.Resources.getSystem().configuration.locales.get(0)
        } catch (_: Exception) {
            java.util.Locale.getDefault()
        }

        val sysLang = primaryLocale.language.lowercase()
        val tag = primaryLocale.toLanguageTag().lowercase()

        return when {
            sysLang.startsWith("en") || tag.startsWith("en") -> "en"
            sysLang.startsWith("ja") || tag.startsWith("ja") -> "ja"
            sysLang.startsWith("es") || tag.startsWith("es") -> "es"
            else -> "ko"
        }
    }

    /**
     * Universal Automatic Pattern & Suffix Auto-Translator Engine
     * Dynamically translates unmapped dynamic Korean strings into target language in real-time.
     */
    fun autoTranslate(text: String, targetLang: String): String {
        if (targetLang == "ko" || text.isEmpty()) return text

        var result = text

        when (targetLang) {
            "en" -> {
                result = result
                    // Home & Battery
                    .replace("IrisFit 눈 건강 배터리", "IrisFit Eye Health Battery")
                    .replace("실시간 안구 피로도 측정 및 맞춤 휴식 가이드", "Real-time eye fatigue tracking & custom rest guide")
                    .replace("마스코트를 탭하여 즉시 휴식 시작!", "Tap mascot for instant rest!")
                    .replace("오늘 화면 응시 시간", "Today's Screen Time")
                    .replace("실제 실측", "Live Measured")
                    .replace("권한 필요", "Permission Required")
                    .replace("미측정", "Not Measured")
                    .replace("휴식 완료", "Rest Completed")
                    .replace("눈 운동", "Eye Exercise")
                    .replace("탭하여 30초 휴식", "Tap for 30s rest")
                    .replace("탭하여 시선 추적", "Tap for gaze tracking")
                    .replace("위험", "Danger")
                    .replace("경고", "Warning")
                    .replace("주의", "Caution")
                    .replace("최상", "Optimal")
                    .replace("행복", "Happy")
                    .replace("졸림", "Sleepy")
                    .replace("불안", "Anxious")
                    .replace("패닉", "Panicked")
                    .replace("완벽하게 안전하고 좋은 상태입니다", "Completely safe and optimal condition")
                    .replace("주의 깊게 관찰해야 하는 초기 신호입니다", "Early signal needing careful observation")
                    .replace("위험으로 번지기 직전의 고조된 상태입니다", "Elevated state right before risk")
                    .replace("즉각적인 조치가 필요한 비상 상태입니다!", "Emergency state needing immediate action!")
                    .replace("손에 쥐고 사용", "Handheld Use")
                    .replace("책상 위 놓임", "On Desk")
                    .replace("폰 뒤집힘", "Phone Face Down")
                    .replace("나의 눈 건강 배터리 SNS 공유하기", "Share My Eye Battery on SNS")

                    // Goals
                    .replace("오늘의 눈 건강 목표 & 트로피", "Today's Eye Health Goals & Trophies")
                    .replace("목표 달성 시 축하 마일리지 자동 지급", "Automatic mileage reward upon goal completion")
                    .replace("1. 눈 배터리 85% 이상 유지", "1. Maintain 85%+ Eye Battery")
                    .replace("2. 하루 30초 눈 휴식 3회 달성", "2. Complete 30s Eye Rest 3 Times")
                    .replace("3. 7일 연속 휴식 스트릭 성공", "3. 7-Day Consecutive Rest Streak")
                    .replace("현재 잔량", "Current Battery")
                    .replace("현재 진행", "Current Progress")
                    .replace("연속 7일째 달성 중!", "7-Day Streak Active!")
                    .replace("달성", "Achieved")

                    // Gym & Exam
                    .replace("눈 배터리 충전소", "Eye Battery Charging Station")
                    .replace("스마트폰 사용으로 닳아버린 눈 건강 배터리를 안구 운동 미니게임으로 급속 충전하세요.", "Recharge your drained eye battery with eye exercise mini-games.")
                    .replace("미니게임 완료 시 배터리 급속 회복", "Fast Battery Recovery on Mini-Game Completion")
                    .replace("미니게임을 완료할 때마다 눈 건강 배터리 게이지가 즉시 충전되고 포인트가 쌓입니다.", "Completing mini-games recharges your battery gauge instantly and earns points.")
                    .replace("1. 원근 조절 모양근 스트레칭 (Focus & Shift)", "1. Focus & Shift Muscle Stretching")
                    .replace("20초 동안 커지는 원을 따라보며 초점을 조절합니다. (+200 Steps / 배터리 충전)", "Focus on the growing circle for 20s. (+200 Steps / Battery Charge)")
                    .replace("원근 조절 미니게임 시작", "Start Focus Mini-Game")
                    .replace("2. 부드러운 시선 추적 훈련 (Infinity Pursuit)", "2. Infinity Pursuit Training")
                    .replace("무한대(∞) 궤적을 따라 눈동자를 부드럽게 움직여 외안근을 이완합니다. (+250 Steps / 배터리 충전)", "Follow the infinity (∞) path to relax extraocular muscles. (+250 Steps / Battery Charge)")
                    .replace("시선 추적 미니게임 시작", "Start Pursuit Mini-Game")
                    .replace("안과 전문 시표 검사 & 리포트", "Professional Visual Chart Exam & Report")
                    .replace("란돌트 환, 크로스 실린더 난시, 적녹 다이크롬 검사 수행.", "Perform Landolt C, Cross Cylinder Astigmatism, and Red-Green exams.")
                    .replace("전문 시표 검사 시작하기", "Start Professional Exam")

                    // Blue Light
                    .replace("삼성 갤럭시 내장 '시력 보호 화면(Eye Comfort Shield)'", "Galaxy Eye Comfort Shield")
                    .replace("삼성 갤럭시 내장 '시력 보호 화면'", "Galaxy Eye Comfort Shield")
                    .replace("소프트웨어 앱 오버레이 필터는 상태바나 시스템 UI를 완벽히 커버하지 못하며 기기 보안 정책에 의해 차단될 수 있습니다. 스마트폰 디스플레이 하드웨어 수준에서 블루라이트를 완벽히 차단하는 '시력 보호 화면'을 시스템 설정에서 켜시는 것이 가장 효과적입니다.", "Software overlay filters cannot fully cover system status bars and may be blocked by security policies. Turning on 'Eye Comfort Shield' in system settings at the display hardware level is most effective.")
                    .replace("시스템 '시력 보호 화면' 설정 바로가기", "Go to System Eye Comfort Settings")
                    .replace("시력 보호 화면 가이드", "Eye Comfort Shield Guide")
                    .replace("• 야간 스마트폰 사용 시 '시력 보호 화면'을 켜면 수면 유도 호르몬(멜라토닌) 분비를 보호할 수 있습니다.\n• 색온도를 따뜻하게 설정하면 색상 왜곡 없이 가장 편안하게 시력을 보호할 수 있습니다.", "• Turning on 'Eye Comfort Shield' at night protects melatonin sleep hormone secretion.\n• Setting warm color temperature protects your vision comfortably without color distortion.")

                    // AI Coach
                    .replace("IrisFit AI 습관 코치", "IrisFit AI Habit Coach")
                    .replace("현재 내 눈 건강 배터리 잔량과 실제 화면 사용 시간을 바탕으로 맞춤 진단을 제공합니다.", "Provides customized diagnosis based on your remaining eye health battery and screen time.")
                    .replace("실제 화면 사용 & 배터리 분석", "Screen Time & Battery Analysis")
                    .replace("오늘 실제 화면 응시 시간", "Today's Actual Screen Time")
                    .replace("눈 건강 배터리 잔량", "Eye Battery Remaining")
                    .replace("휴식 패턴 실측 준수율", "Rest Pattern Compliance Rate")
                    .replace("실제 화면 사용 시간 측정 권한 허용하기", "Allow Screen Time Permission")
                    .replace("IrisFit PRO 차세대 유료 플랜 혜택", "IrisFit PRO Next-Gen Benefits")
                    .replace("시선 추적(Gaze Tracking) 기술 기반의 독보적인 프리미엄 기능:", "Exclusive premium features powered by Gaze Tracking technology:")
                    .replace("1. Gaze-Nav 시선 스크롤 지도", "1. Gaze-Nav Hands-Free Map Scroll")
                    .replace("손대지 않고 눈동자 이동만으로 지도 스크롤 & 확대/축소", "Scroll & zoom maps hands-free with eye movements alone")
                    .replace("시선 스크롤 체험하기", "Try Gaze Scroll Demo")
                    .replace("2. Eye-Blink 힐링 사운드", "2. Eye-Blink Healing Sound")
                    .replace("내 깜빡임 주기와 동기화되어 안구 근육을 이완시키는 오디오", "Synchronized ASMR audio relaxing eye muscles to your blink rate")
                    .replace("힐링 사운드 체험하기", "Try Healing Sound Demo")
                    .replace("3. Gaze-AI 머무름 감지 요약", "3. Gaze-AI Dwell Detection Summary")
                    .replace("오랫동안 쳐다보는 화면 영역을 LLM이 실시간 감지 및 브리핑", "LLM detects dwell areas in real-time and provides smart summaries")
                    .replace("AI 머무름 요약 테스트", "Test AI Dwell Summary")
                    .replace("PRO 7일 무료 체험 시작하기", "Start PRO 7-Day Free Trial")

                    // Settings & CS & Battery Optimization
                    .replace("스마트폰 언어 자동 연동", "Auto Phone Language Sync")
                    .replace("현재 스마트폰 시스템 언어", "Current Phone System Language")
                    .replace("실시간 자동 번역 중", "Live Auto-Translating")
                    .replace("상태 변경 알림 수신", "Status Change Push Notifications")
                    .replace("눈 배터리 피로도 단계가 바뀔 때 스마트폰 푸시 알림을 수신합니다.", "Receive push notifications when eye battery fatigue tier changes.")
                    .replace("유저 피드백 & 버그 제보 (CS 센터)", "User Feedback & Bug CS Center")
                    .replace("앱 사용 중 버그가 발생하거나 개선 아이디어가 있으시면 언제든지 의견을 남겨주세요!", "If you encounter bugs or have suggestions, please leave your feedback anytime!")
                    .replace("개발자에게 피드백 보내기 (Email)", "Send Email Feedback to Developer")
                    .replace("백그라운드 강제종료 방지 (배터리 최적화 예외)", "Background Termination Protection (Battery Exemption)")
                    .replace("삼성, 샤오미 등 일부 기기에서 백그라운드 실시간 눈 건강 모니터링이 멈추지 않도록 [배터리 최적화 예외 앱]으로 등록해 주세요.", "Register as [Battery Optimization Exempted App] to ensure background eye monitoring runs uninterrupted on Samsung/Xiaomi devices.")
                    .replace("배터리 최적화 예외 설정 열기", "Open Battery Optimization Settings")
                    .replace("앱 정보 및 버전", "App Info & Version")
                    .replace("개인정보 및 데이터 관리", "Privacy & Data Management")
                    .replace("기기에 저장된 모든 눈 건강 마일리지 및 완료 기록을 초기화합니다.", "Resets all stored eye health mileage and completion records.")
                    .replace("기기의 앱 기록 전체 삭제", "Delete All Local Records")
                    .replace("개인정보 처리방침 및 이용약관", "Privacy Policy & Terms of Use")
                    .replace("구글 플레이 스토어 등록 필수 법적 문서 및 로컬 데이터 보호 정책", "Mandatory Google Play legal documents & local data protection policy")
                    .replace("개인정보 처리방침 전문 보기", "View Full Privacy Policy")

                    // Relay & Challenge
                    .replace("개)", ")")
                    .replace("개", " Relays")
                    .replace("명 참여 중", " Participants")
                    .replace("명", " Members")
                    .replace("하기", "")
                    .replace("+50 눈 걸음", "+50 Eye Steps")
                    .replace("+100 눈 걸음", "+100 Eye Steps")
                    .replace("+200 눈 걸음", "+200 Eye Steps")
                    .replace("+250 눈 걸음", "+250 Eye Steps")
                    .replace("눈 걸음", "Eye Steps")
                    .replace("눈 휴식", "Eye Rest")
                    .replace("진행 중인 릴레이", "Ongoing Relays")
                    .replace("진행 중", "In Progress")
                    .replace("주간공식", "Weekly Official")
                    .replace("스트릭", "Streak")
                    .replace("릴레이", "Relay")
                    .replace("바통", "Baton")
                    .replace("소지", "Holder")
                    .replace("차례입니다", "'s turn")
                    .replace("차례", "'s turn")
                    .replace("응원 보드에 응원 메시지를 남겼습니다!", "Cheer message added!")
                    .replace("님에게 응원을 전달했습니다!", " cheer sent!")
                    .replace("#오늘도30초휴식 공개 챌린지 🌍", "#30sRestEveryday Public Challenge 🌍")
                    .replace("#오늘도30초휴식 공개 챌린지", "#30sRestEveryday Public Challenge")
                    .replace("공개 글로벌 릴레이", "Public Global Challenge")
                    .replace("참여하기", "Join")
                    .replace("참여 완료", "Joined")
                    .replace("참여 중", "Active")
                    .replace("하루 3회 30초 눈 휴식 완오!", "Complete 30s Eye Rest 3 times daily!")
                    .replace("완오!", "Done!")
                    .replace("탭하여 즉시 동참 및 보상 받기", "Tap to join & get reward")
                    .replace("나의 기여: 눈 휴식 4회 완료 (달성률 80%)", "My Contribution: 4 Eye Rests (80%)")
                    .replace("🎉 #오늘도30초휴식 공개 챌린지에 정식 참여했습니다! (+50 눈 걸음 적립)", "🎉 Officially joined #30sRestEveryday Public Challenge! (+50 Eye Steps)")
                    .replace("공개 챌린지 참여를 취소했습니다.", "Cancelled public challenge participation.")
                    .replace("공개 챌린지", "Public Challenge")
                    .replace("나의 기여", "My Contribution")
                    .replace("달성률", "Achievement")
                    .replace("완료", "Completed")
                    .replace("파이팅", "Cheers")
                    .replace("맑은눈토끼", "Clear Eye Rabbit")
                    .replace("체리공주", "Cherry Princess")
                    .replace("벚꽃요정", "Blossom Fairy")
                    .replace("곰돌이버디", "Bear Buddy")
                    .replace("민트여우", "Mint Fox")
            }
            "ja" -> {
                result = result
                    .replace("IrisFit 눈 건강 배터리", "IrisFit 目の健康バッテリー")
                    .replace("오늘 화면 응시 시간", "本日の画面注視時間")
                    .replace("휴식 완료", "休憩完了")
                    .replace("눈 운동", "眼の운동")
                    .replace("위험", "危険")
                    .replace("경고", "警告")
                    .replace("주의", "注意")
                    .replace("최상", "最適")
                    .replace("눈 배터리 충전소", "目のバッテリー充電所")
                    .replace("설정 및 관리", "設定と管理")
                    .replace("개)", "個)")
                    .replace("개", "個")
                    .replace("명 참여 중", "人参加中")
                    .replace("명", "人")
                    .replace("하기", "する")
                    .replace("+50 눈 걸음", "+50 歩数")
                    .replace("눈 걸음", "歩数")
                    .replace("눈 휴식", "目の休憩")
                    .replace("진행 중인 릴레이", "進行中のリレー")
                    .replace("진행 중", "進行中")
                    .replace("주간공식", "週間公式")
                    .replace("스트릭", "ストリーク")
                    .replace("릴레이", "リレー")
                    .replace("바통", "バトン")
                    .replace("소지", "所持")
                    .replace("차례입니다", "さんの番です")
                    .replace("차례", "さんの番")
                    .replace("공개 챌린지", "公開チャレンジ")
                    .replace("탭하여 즉시 동참 및 보상 받기", "タップして即座に参加 & 報酬獲得")
                    .replace("나의 기여", "私の貢献")
                    .replace("달성률", "達成率")
                    .replace("완료", "完了")
                    .replace("파이팅", "ファイト")
            }
            "es" -> {
                result = result
                    .replace("IrisFit 눈 건강 배터리", "Batería Ocular IrisFit")
                    .replace("오늘 화면 응시 시간", "Tiempo de pantalla hoy")
                    .replace("휴식 완료", "Descanso completado")
                    .replace("눈 운동", "Ejercicio ocular")
                    .replace("위험", "Peligro")
                    .replace("경고", "Advertencia")
                    .replace("주의", "Precaución")
                    .replace("최상", "Óptimo")
                    .replace("설정 및 관리", "Ajustes y gestión")
                    .replace("개)", ")")
                    .replace("개", " Relevos")
                    .replace("명 참여 중", " Participantes")
                    .replace("명", " Personas")
                    .replace("하기", "")
                    .replace("+50 눈 걸음", "+50 Pasos Oculares")
                    .replace("눈 걸음", "Pasos Oculares")
                    .replace("눈 휴식", "Descanso Ocular")
                    .replace("진행 중인 릴레이", "Relevos en Curso")
                    .replace("진행 중", "En Curso")
                    .replace("주간공식", "Oficial Semanal")
                    .replace("스트릭", "Racha")
                    .replace("릴레이", "Relevo")
                    .replace("바통", "Testigo")
                    .replace("소지", "Portador")
                    .replace("차례입니다", "es su turno")
                    .replace("공개 챌린지", "Desafío Público")
                    .replace("탭하여 즉시 동참 및 보상 받기", "Toca para unirte y obtener recompensa")
                    .replace("완료", "Completado")
                    .replace("파이팅", "¡Ánimo!")
            }
        }
        return result
    }

    fun get(langCode: String, key: String): String {
        val effectiveLang = resolveEffectiveLang(langCode)
        val raw = mapLookup(key, effectiveLang)
        return autoTranslate(raw, effectiveLang)
    }

    private fun mapLookup(key: String, effectiveLang: String): String {
        return when (effectiveLang) {
            "en" -> stringsEn[key] ?: stringsKo[key] ?: key
            else -> stringsKo[key] ?: key
        }
    }
}
