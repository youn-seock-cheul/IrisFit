package com.example.myapplication.session

object AppLocalization {

    private val stringsKo = mapOf(
        "nav_home" to "홈",
        "nav_gym" to "눈 운동 존",
        "nav_eye_comfort" to "시력 보호",
        "nav_ai" to "AI 코치",
        "nav_settings" to "설정",
        "home_title" to "IrisFit",
        "home_subtitle" to "눈 건강 만보기 & 휴식 습관",
        "battery_card_title" to "IrisFit 눈 건강 배터리",
        "battery_card_sub" to "앱 활동 기반 웰니스 습관 지표입니다. 의료 측정이나 진단이 아닙니다.",
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
        "eye_comfort_title" to "휴대폰 화면 편안하게 보기",
        "eye_comfort_desc" to "휴대폰에 내장된 화면 색상 및 색온도 설정을 이용할 수 있습니다. IrisFit은 화면 필터를 직접 적용하지 않습니다.",
        "eye_comfort_card_title" to "휴대폰 디스플레이 설정",
        "eye_comfort_card_desc" to "설정 화면에서 기기에 제공되는 편안하게 화면 보기, 블루라이트 감소 또는 야간 모드 기능을 직접 켜고 조절하세요. 메뉴 이름과 제공 기능은 제조사 및 Android 버전에 따라 다를 수 있습니다.",
        "eye_comfort_open_settings" to "휴대폰 디스플레이 설정 열기",
        "eye_comfort_open_failed" to "디스플레이 설정을 열 수 없습니다.",
        "ai_title" to "IrisFit AI 습관 코치",
        "ai_desc" to "휴식 기록과 허용된 화면 사용 통계를 바탕으로 습관을 돌아보는 안내입니다. 의료 진단이 아닙니다.",
        "ai_analysis_title" to "화면 사용 및 휴식 기록 요약",
        "ai_screen_time" to "오늘 화면 사용 시간",
        "ai_battery_remain" to "눈 건강 배터리 잔량",
        "ai_compliance" to "휴식 완료 기록",
        "ai_permission_btn" to "실제 화면 사용 시간 측정 권한 허용하기",
        "usage_access_title" to "사용 정보 접근 권한 안내",
        "usage_access_body" to "Usage Access를 허용하면 Android가 제공하는 오늘 자정부터 현재까지의 앱 전경 사용시간 통계를 조회하여 화면 사용 기반 웰니스 지표를 계산합니다.\n\n이 값은 눈 피로도나 시력을 측정하거나 진단하지 않습니다. 앱은 원본 앱별 사용 기록을 완료 기록 파일에 저장하지 않습니다. 권한을 허용하지 않으면 화면 사용시간은 측정되지 않고 관련 점수 차감도 적용되지 않습니다. 30초 휴식 등 나머지 기능은 계속 사용할 수 있습니다.",
        "usage_access_open_settings" to "계속하고 설정 열기",
        "not_measured" to "미측정",
        "screen_time_unmeasured" to "화면 사용시간 미측정 — 권한이 없어 사용시간 차감은 적용되지 않습니다.",
        "pro_demo_status" to "PRO 기능은 현재 제공되지 않는 데모입니다.",
        "pro_unavailable" to "PRO 기능 준비 중",
        "privacy_policy_title" to "IrisFit 개인정보 및 데이터 안내",
        "privacy_usage" to "사용 정보: 권한을 허용한 경우 Android UsageStats 통계를 조회해 오늘의 사용시간 지표를 계산합니다. 원본 앱별 사용 기록은 완료 기록 파일에 저장하지 않습니다.",
        "privacy_sensor" to "센서: 가속도계와 자이로스코프 등의 값은 기기 자세를 추정하는 동안 메모리에서 처리됩니다. 자세는 실제 눈 휴식 여부를 확인하지 않습니다.",
        "privacy_storage" to "완료 기록: 휴식 및 운동 완료 정보는 앱 전용 내부 파일에 저장됩니다. 해당 파일은 앱 자체 암호화를 적용하지 않습니다.",
        "privacy_ai" to "AI 코치 기능은 데이터 처리 검토가 끝날 때까지 사용할 수 없습니다.",
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
        "pose_tilted" to "기기가 기울어짐",
        "pose_flat" to "책상 위 놓임",
        "pose_facedown" to "폰 뒤집힘",
        "status_optimal" to "최상 (행복)",
        "status_caution" to "주의 (졸림)",
        "status_warning" to "경고 (불안)",
        "status_danger" to "관심 (피로)",
        "desc_optimal" to "앱 기록상 휴식 습관 지표가 양호합니다. 의료 측정값은 아닙니다.",
        "desc_caution" to "화면 사용 기록을 확인하고 잠시 쉬는 것을 고려해 보세요.",
        "desc_warning" to "화면 사용 기록을 바탕으로 휴식 습관을 점검해 보세요.",
        "desc_danger" to "앱의 습관 지표가 낮습니다. 필요하면 화면을 멈추고 휴식해 보세요.",

        // Gym Tab
        "gym_sub" to "눈 운동 미니게임을 이용해 휴식 시간을 가져 보세요.",
        "gym_card_banner" to "미니게임 완료 기록",
        "gym_card_banner_sub" to "미니게임 완료 시 앱 내 포인트가 기록됩니다. 건강 효과를 보장하지 않습니다.",
        "gym_focus_title" to "1. 원근 조절 미니게임 (Focus & Shift)",
        "gym_focus_sub" to "20초 동안 화면의 원을 따라보는 미니게임입니다. (+200 Steps)",
        "gym_focus_btn" to "원근 조절 미니게임 시작",
        "gym_pursuit_title" to "2. 부드러운 시선 추적 훈련 (Infinity Pursuit)",
        "gym_pursuit_sub" to "무한대(∞) 궤적을 따라보는 20초 미니게임입니다. (+250 Steps)",
        "gym_pursuit_btn" to "시선 추적 미니게임 시작",
        "gym_exam_title" to "안과 전문 시표 검사 & 리포트",
        "gym_exam_sub" to "란돌트 환, 크로스 실린더 난시, 적녹 다이크롬 검사 수행.",
        "gym_exam_btn" to "전문 시표 검사 시작하기",

        // Eye Gym Game screens
        "gym_screen_title" to "Eye Gym: 포커스 원근 훈련",
        "gym_back" to "← 뒤로",
        "gym_ready_title" to "원근 조절 모양근 스트레칭",
        "gym_ready_desc" to "화면의 움직이는 표식을 20초 동안 따라보는 미니게임입니다. (+200 눈 걸음, +100 포인트)",
        "gym_start" to "훈련 시작하기",
        "gym_playing" to "초점을 맞추고 호흡하세요",
        "gym_gazing" to "응시 중...",
        "gym_stop" to "중단하기",
        "gym_finished" to "훈련 완료!",
        "gym_finished_desc" to "미니게임 완료가 기록되었습니다.\n+200 눈 걸음 수 & +100 포인트",
        "gym_restart" to "다시 하기",
        "gym_home" to "홈으로 돌아가기",

        // Smooth Pursuit Game
        "pursuit_screen_title" to "Eye Gym: 부드러운 시선 추적 (Infinity)",
        "pursuit_ready_title" to "무한대(∞) 궤적 시선 추적",
        "pursuit_ready_desc" to "화면의 움직이는 표식을 20초 동안 따라보는 미니게임입니다. (+250 눈 걸음, +120 포인트)",
        "pursuit_start" to "시선 추적 훈련 시작",
        "pursuit_playing" to "머리는 고정하고 눈으로만 마커를 쫓아가세요",
        "pursuit_finished" to "시선 추적 완료!",
        "pursuit_finished_desc" to "미니게임 완료가 기록되었습니다.\n+250 눈 걸음 수 & +120 포인트",
        "workout_paused_title" to "훈련 일시정지",
        "workout_resume" to "이어서 하기",
        "workout_cancel" to "훈련 취소"
    )

    private val stringsEn = mapOf(
        "nav_home" to "Home",
        "nav_gym" to "Eye Gym",
        "nav_eye_comfort" to "Eye Comfort",
        "nav_ai" to "AI Coach",
        "nav_settings" to "Settings",
        "home_title" to "IrisFit",
        "home_subtitle" to "Eye Health & Rest Habits",
        "battery_card_title" to "IrisFit Eye Health Battery",
        "battery_card_sub" to "A wellness habit indicator based on app activity; not a medical measurement.",
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
        "eye_comfort_title" to "Phone display comfort",
        "eye_comfort_desc" to "Use the screen color and color-temperature options built into your phone. IrisFit does not apply its own screen filter.",
        "eye_comfort_card_title" to "Phone display settings",
        "eye_comfort_card_desc" to "Open your phone settings to enable or adjust Eye Comfort, blue-light reduction, or Night mode if available. Names and options vary by manufacturer and Android version.",
        "eye_comfort_open_settings" to "Open phone display settings",
        "eye_comfort_open_failed" to "Could not open display settings.",
        "ai_title" to "IrisFit AI Habit Coach",
        "ai_desc" to "A habit summary based on rest records and permitted screen-time statistics. Not a medical diagnosis.",
        "ai_analysis_title" to "Screen-Time and Rest Summary",
        "ai_screen_time" to "Screen Time Today",
        "ai_battery_remain" to "Eye Battery Remaining",
        "ai_compliance" to "Recorded Rest Completions",
        "ai_permission_btn" to "Allow Screen Time Permission",
        "usage_access_title" to "Usage Access Permission",
        "usage_access_body" to "If granted, IrisFit queries Android's app foreground-usage statistics from local midnight until now to calculate an optional screen-time wellness indicator.\n\nThis does not measure or diagnose eye fatigue or vision. The app does not save raw per-app usage history in its completion-record file. Without permission, screen time is not measured and no screen-time deduction is applied. Other features, including the 30-second rest timer, remain available.",
        "usage_access_open_settings" to "Continue to Settings",
        "not_measured" to "Not measured",
        "screen_time_unmeasured" to "Screen time not measured — no usage deduction is applied without permission.",
        "pro_demo_status" to "PRO features are a demo and are not currently available.",
        "pro_unavailable" to "PRO is not available yet",
        "privacy_policy_title" to "IrisFit Privacy and Data Information",
        "privacy_usage" to "Usage information: With permission, IrisFit queries Android UsageStats to calculate today's usage indicator. Raw per-app usage history is not saved in the completion-record file.",
        "privacy_sensor" to "Sensors: Accelerometer and gyroscope values are processed in memory to estimate device posture. Posture does not confirm that your eyes are resting.",
        "privacy_storage" to "Completion records: Rest and exercise completion details are stored in an app-private file. This file is not encrypted by the app.",
        "privacy_ai" to "The AI Coach is unavailable until its data handling review is complete.",
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
        "pose_tilted" to "Device tilted",
        "pose_flat" to "On Desk",
        "pose_facedown" to "Face Down",
        "status_optimal" to "Optimal (Happy)",
        "status_caution" to "Caution (Sleepy)",
        "status_warning" to "Warning (Fatigued)",
        "status_danger" to "Focus Needed (Rest Urged)",
        "desc_optimal" to "Your app-recorded rest habit indicator is favorable. This is not a medical measurement.",
        "desc_caution" to "Review your screen-time records and consider taking a short break.",
        "desc_warning" to "Consider reviewing your rest habits based on app activity.",
        "desc_danger" to "Your app habit indicator is low. Consider pausing screen use for a break.",

        // Gym Tab
        "gym_sub" to "Take a break with an eye-exercise mini-game.",
        "gym_card_banner" to "Mini-Game Completion Record",
        "gym_card_banner_sub" to "Mini-game completion adds in-app points. No health benefit is guaranteed.",
        "gym_focus_title" to "1. Focus & Shift Mini-Game",
        "gym_focus_sub" to "Follow the on-screen circle for 20 seconds. (+200 Steps)",
        "gym_focus_btn" to "Start Focus Mini-Game",
        "gym_pursuit_title" to "2. Infinity Pursuit Training",
        "gym_pursuit_sub" to "Follow the infinity (∞) path in a 20-second mini-game. (+250 Steps)",
        "gym_pursuit_btn" to "Start Pursuit Mini-Game",
        "gym_exam_title" to "Professional Visual Chart Exam & Report",
        "gym_exam_sub" to "Perform Landolt C, Cross Cylinder Astigmatism, and Red-Green exams.",
        "gym_exam_btn" to "Start Professional Exam",

        // Eye Gym Game screens
        "gym_screen_title" to "Eye Gym: Focus & Shift",
        "gym_back" to "← Back",
        "gym_ready_title" to "Focus & Shift Stretching",
        "gym_ready_desc" to "Follow the moving marker on screen for 20 seconds. (+200 steps, +100 points)",
        "gym_start" to "Start Training",
        "gym_playing" to "Focus and breathe",
        "gym_gazing" to "Gazing...",
        "gym_stop" to "Stop",
        "gym_finished" to "Training Complete!",
        "gym_finished_desc" to "Mini-game completion recorded.\n+200 eye steps & +100 points",
        "gym_restart" to "Try Again",
        "gym_home" to "Back to Home",

        // Smooth Pursuit Game
        "pursuit_screen_title" to "Eye Gym: Infinity Pursuit",
        "pursuit_ready_title" to "Infinity (∞) Pursuit",
        "pursuit_ready_desc" to "Follow the moving marker on screen for 20 seconds. (+250 eye steps, +120 points)",
        "pursuit_start" to "Start Pursuit Training",
        "pursuit_playing" to "Keep head fixed and follow marker",
        "pursuit_finished" to "Pursuit Complete!",
        "pursuit_finished_desc" to "Mini-game completion recorded.\n+250 eye steps & +120 points",
        "workout_paused_title" to "Training paused",
        "workout_resume" to "Resume",
        "workout_cancel" to "Cancel training"
    )

    private val stringsJa = mapOf(
        "battery_card_sub" to "アプリの活動記録に基づくウェルネス習慣指標です。医療測定・診断ではありません。",
        "ai_desc" to "休憩記録と許可された画面使用統計に基づく習慣の案内です。医療診断ではありません。",
        "ai_analysis_title" to "画面使用と休憩記録の概要",
        "ai_screen_time" to "本日の画面使用時間",
        "ai_compliance" to "記録された休憩回数",
        "usage_access_title" to "使用状況へのアクセス許可",
        "usage_access_body" to "許可すると、Androidが提供する本日0時から現在までのアプリ前面使用時間の統計を取得し、画面使用に基づくウェルネス指標を計算します。\n\nこれは目の疲れや視力を測定・診断するものではありません。アプリはアプリごとの生の使用履歴を完了記録ファイルに保存しません。許可しない場合、画面使用時間は測定されず、使用時間によるスコア減算も適用されません。30秒休憩など他の機能は引き続き利用できます。",
        "usage_access_open_settings" to "続行して設定を開く",
        "not_measured" to "未測定",
        "screen_time_unmeasured" to "画面使用時間は未測定です。許可がないため使用時間による減算は適用されません。",
        "pro_demo_status" to "PRO機能はデモであり、現在は利用できません。",
        "pro_unavailable" to "PRO機能は準備中です",
        "privacy_policy_title" to "IrisFitのプライバシーとデータについて",
        "privacy_usage" to "使用情報：許可された場合、Android UsageStatsを取得して本日の使用時間指標を計算します。アプリごとの生の使用履歴は完了記録ファイルに保存しません。",
        "privacy_sensor" to "センサー：加速度計やジャイロスコープの値をメモリ上で処理し、端末の姿勢を推定します。端末の姿勢から目が休んでいるとは判断できません。",
        "privacy_storage" to "完了記録：休憩・運動の完了情報はアプリ専用の内部ファイルに保存されます。このファイルはアプリによる暗号化を行っていません。",
        "privacy_ai" to "AIコーチはデータ処理の確認が完了するまで利用できません。",
        "desc_optimal" to "アプリに記録された休憩習慣の指標は良好です。医療測定値ではありません。",
        "desc_caution" to "画面使用記録を確認し、短い休憩を検討してください。",
        "desc_warning" to "アプリの活動記録を参考に休憩習慣を見直してください。",
        "desc_danger" to "アプリの習慣指標が低くなっています。画面を止めて休憩することを検討してください。",
        "gym_sub" to "目の運動ミニゲームで休憩時間を取りましょう。",
        "gym_card_banner" to "ミニゲーム完了記録",
        "gym_card_banner_sub" to "完了するとアプリ内ポイントが記録されます。健康効果を保証するものではありません。",
        "gym_focus_title" to "1. 遠近フォーカスのミニゲーム",
        "gym_focus_sub" to "画面上の円を20秒間追うミニゲームです。（+200 Steps）",
        "gym_pursuit_sub" to "20秒間、∞の軌跡を追うミニゲームです。（+250 Steps）",
        "gym_ready_desc" to "画面上の動くマーカーを20秒間追うミニゲームです。（+200歩、+100ポイント）",
        "gym_finished_desc" to "ミニゲームの完了を記録しました。\n+200歩、+100ポイント",
        "pursuit_ready_desc" to "画面上の動くマーカーを20秒間追うミニゲームです。（+250歩、+120ポイント）",
        "pursuit_finished_desc" to "ミニゲームの完了を記録しました。\n+250歩、+120ポイント",
        "pose_tilted" to "端末が傾いています",
        "workout_paused_title" to "トレーニング一時停止",
        "workout_resume" to "再開",
        "workout_cancel" to "トレーニングを中止"
    )

    private val stringsEs = mapOf(
        "battery_card_sub" to "Indicador de hábitos de bienestar basado en la actividad de la app; no es una medición médica.",
        "ai_desc" to "Resumen de hábitos basado en registros de descanso y estadísticas de pantalla autorizadas. No es un diagnóstico médico.",
        "ai_analysis_title" to "Resumen de uso de pantalla y descansos",
        "ai_screen_time" to "Tiempo de pantalla de hoy",
        "ai_compliance" to "Descansos registrados",
        "usage_access_title" to "Permiso de acceso al uso",
        "usage_access_body" to "Si lo permites, IrisFit consulta las estadísticas de uso en primer plano de las apps que Android proporciona desde la medianoche local hasta ahora para calcular un indicador de bienestar basado en el uso de pantalla.\n\nEsto no mide ni diagnostica la fatiga ocular o la visión. La app no guarda el historial de uso sin procesar por aplicación en el archivo de finalizaciones. Sin permiso, el tiempo de pantalla no se mide y no se aplica ninguna deducción por uso. Las demás funciones, incluido el descanso de 30 segundos, siguen disponibles.",
        "usage_access_open_settings" to "Continuar y abrir Ajustes",
        "not_measured" to "No medido",
        "screen_time_unmeasured" to "Tiempo de pantalla no medido: sin permiso no se aplica ninguna deducción por uso.",
        "pro_demo_status" to "Las funciones PRO son una demostración y no están disponibles actualmente.",
        "pro_unavailable" to "PRO aún no está disponible",
        "privacy_policy_title" to "Información de privacidad y datos de IrisFit",
        "privacy_usage" to "Información de uso: con permiso, IrisFit consulta UsageStats de Android para calcular el indicador de uso diario. El historial sin procesar por aplicación no se guarda en el archivo de finalizaciones.",
        "privacy_sensor" to "Sensores: los valores del acelerómetro y giroscopio se procesan en memoria para estimar la postura del dispositivo. La postura no confirma que tus ojos estén descansando.",
        "privacy_storage" to "Registros de finalización: los datos de descanso y ejercicio se guardan en un archivo privado de la app. La app no cifra este archivo.",
        "privacy_ai" to "El Coach de IA no está disponible hasta completar la revisión del tratamiento de datos.",
        "desc_optimal" to "El indicador de hábitos de descanso registrado por la app es favorable. No es una medición médica.",
        "desc_caution" to "Revisa tus registros de uso de pantalla y considera hacer una pausa breve.",
        "desc_warning" to "Considera revisar tus hábitos de descanso según la actividad registrada por la app.",
        "desc_danger" to "El indicador de hábitos de la app es bajo. Considera pausar la pantalla y descansar.",
        "gym_sub" to "Tómate un descanso con un minijuego de ejercicio ocular.",
        "gym_card_banner" to "Registro de finalización del minijuego",
        "gym_card_banner_sub" to "Al completar el minijuego se añaden puntos de la app. No se garantiza ningún beneficio para la salud.",
        "gym_focus_title" to "1. Minijuego de enfoque y distancia",
        "gym_focus_sub" to "Sigue el círculo en pantalla durante 20 segundos. (+200 pasos)",
        "gym_pursuit_sub" to "Sigue la trayectoria ∞ en un minijuego de 20 segundos. (+250 pasos)",
        "gym_ready_desc" to "Sigue el marcador en movimiento durante 20 segundos. (+200 pasos, +100 puntos)",
        "gym_finished_desc" to "Se registró la finalización del minijuego.\n+200 pasos y +100 puntos",
        "pursuit_ready_desc" to "Sigue el marcador en movimiento durante 20 segundos. (+250 pasos, +120 puntos)",
        "pursuit_finished_desc" to "Se registró la finalización del minijuego.\n+250 pasos y +120 puntos",
        "pose_tilted" to "Dispositivo inclinado",
        "workout_paused_title" to "Entrenamiento en pausa",
        "workout_resume" to "Reanudar",
        "workout_cancel" to "Cancelar entrenamiento"
    )

    fun resolveEffectiveLang(selectedCode: String = ""): String {
        selectedCode.lowercase().takeIf { it in setOf("ko", "en", "ja", "es") }?.let { return it }
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
                    .replace("실시간 안구 피로도 측정 및 맞춤 휴식 가이드", "A wellness habit indicator; not a medical measurement")
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
                    .replace("스마트폰 사용으로 닳아버린 눈 건강 배터리를 안구 운동 미니게임으로 급속 충전하세요.", "Take a break with an eye-exercise mini-game.")
                    .replace("미니게임 완료 시 배터리 급속 회복", "Mini-Game Completion Record")
                    .replace("미니게임을 완료할 때마다 눈 건강 배터리 게이지가 즉시 충전되고 포인트가 쌓입니다.", "Mini-game completion adds in-app points. No health benefit is guaranteed.")
                    .replace("1. 원근 조절 모양근 스트레칭 (Focus & Shift)", "1. Focus & Shift Muscle Stretching")
                    .replace("20초 동안 커지는 원을 따라보며 초점을 조절합니다. (+200 Steps / 배터리 충전)", "Focus on the growing circle for 20s. (+200 Steps / Battery Charge)")
                    .replace("원근 조절 미니게임 시작", "Start Focus Mini-Game")
                    .replace("2. 부드러운 시선 추적 훈련 (Infinity Pursuit)", "2. Infinity Pursuit Training")
                    .replace("무한대(∞) 궤적을 따라 눈동자를 부드럽게 움직여 외안근을 이완합니다. (+250 Steps / 배터리 충전)", "Follow the infinity (∞) path in a 20-second mini-game. (+250 Steps)")
                    .replace("시선 추적 미니게임 시작", "Start Pursuit Mini-Game")
                    .replace("안과 전문 시표 검사 & 리포트", "Professional Visual Chart Exam & Report")
                    .replace("란돌트 환, 크로스 실린더 난시, 적녹 다이크롬 검사 수행.", "Perform Landolt C, Cross Cylinder Astigmatism, and Red-Green exams.")
                    .replace("전문 시표 검사 시작하기", "Start Professional Exam")


                    // Eye Comfort Settings
                    .replace("휴대폰 화면 편안하게 보기", "Phone display comfort")
                    .replace("휴대폰 디스플레이 설정", "Phone display settings")
                    .replace("휴대폰에 내장된 화면 색상 및 색온도 설정을 이용할 수 있습니다. IrisFit은 화면 필터를 직접 적용하지 않습니다.", "Use the screen color and color-temperature options built into your phone. IrisFit does not apply its own screen filter.")
                    .replace("설정 화면에서 기기에 제공되는 편안하게 화면 보기, 블루라이트 감소 또는 야간 모드 기능을 직접 켜고 조절하세요. 메뉴 이름과 제공 기능은 제조사 및 Android 버전에 따라 다를 수 있습니다.", "Enable or adjust Eye Comfort, blue-light reduction, or Night mode in your phone settings if available. Names and options vary by manufacturer and Android version.")
                    .replace("휴대폰 디스플레이 설정 열기", "Open phone display settings")
                    .replace("디스플레이 설정을 열 수 없습니다.", "Could not open display settings.")

                    // AI Coach
                    .replace("IrisFit AI 습관 코치", "IrisFit AI Habit Coach")
                    .replace("현재 내 눈 건강 배터리 잔량과 실제 화면 사용 시간을 바탕으로 맞춤 진단을 제공합니다.", "A habit summary based on rest records and permitted screen-time statistics. Not a medical diagnosis.")
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
                    .replace("PRO 7일 무료 체험 시작하기", "PRO is not currently available")

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
                    .replace("랜덤 눈 운동 ➔", "Random Workout ➔")
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
                    .replace("눈 피로가 쌓여 30초 눈 감기 휴식이 필요한 단계입니다", "目の疲労が蓄積し、30秒間の目閉じ休憩が必要な段階です")
                    .replace("장시간 화면 사용으로 눈 휴식 및 안구 운동이 필요한 단계입니다", "長時間の画面使用により目の休憩および眼球運動が必要な段階です")
                    .replace("눈 습관 컨디션이 매우 활기차고 좋은 상태입니다", "目の習慣コンディションがとても良好な状態です")
                    .replace("화면 응시 시간이 누적되어 휴식이 권장되는 단계입니다", "画面注視時間が蓄積し、休憩が推奨される段階です")
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
            "ja" -> stringsJa[key] ?: stringsKo[key] ?: key
            "es" -> stringsEs[key] ?: stringsKo[key] ?: key
            else -> stringsKo[key] ?: key
        }
    }
}
