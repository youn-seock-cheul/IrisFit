# IrisFit UI 후속 개발 기록 — 2026-11-18

- 최종 갱신: 2026-11-19 KST. 11월 18일 요청의 후속 작업, 운동 재시작 결함 수정과 동작 개선을 기록한다.
- 상태: **요청된 코드 수정 및 대상 단위 테스트·기기 UI 회귀 검증 완료 / 모션 시각 QA는 사용자 확인 대기 / 미커밋 / Release Hold 유지**.

## 기준 및 승인

- 시작 HEAD: `9f9642b7e864ca1a21defeac98bb843806a18778`.
- 시작 상태: 이전 UI 개편의 수정 파일 25개 및 공통 컴포넌트/벡터 아이콘이 미커밋 상태. 기존 변경을 보존한다. 커밋·배포 승인은 없으며 Release Hold를 유지한다.
- 사용자 지정 순서: 고정 가짜 데이터 → 의료처럼 들리는 표현 → 원근 조절 훈련 겹침 → 홈 팁 → 배터리 제목 → 번역.
- 추가 승인: 개발 내용의 저장소 문서 보존, 설정에 색톤 선택 추가.
- 색톤 상세 승인: 초록(기본)·블루·라벤더·피치·그레이 5종 프리셋. 즉시 반영하고 재실행 후 유지한다. 시스템 라이트/다크 설정은 유지한다.
- 연속 휴식 상세 승인: 실제 `screen_rest` 완료 날짜만 하루로 집계한다. 오늘 휴식 전에는 어제까지의 연속 기록을 유지하고, 하루가 끊기면 0일이다. 중복·훈련·미래 날짜로 일수를 늘리지 않는다.
- 참여자 수 등 원본 데이터가 없는 수치는 표시하지 않는다.
- 의료 표현 상세 승인: 시표 체험은 유지하되 화면 자가 확인과 직접 선택한 응답 요약으로 변경한다. 미응답을 정상으로 간주하지 않고 `아직 확인 안 함`으로 표시한다.

## 이전 대화에서 승인·구현한 UI 개편

- 눈 건강 배터리 명칭 통일(한국어·영어·일본어·스페인어); 미니게임을 눈동자 훈련으로 변경.
- 개별 훈련: 원근 조절 훈련 / 시선 추적 훈련. 보상 숫자는 제거하고 면책 안내는 유지.
- 설정은 한 화면의 그룹형 목록, 일반 카드는 공통 카드와 큰 버튼으로 통일.
- Material Symbols Rounded 벡터 아이콘 사용. 선택된 하단 탭은 채움 아이콘. SNS 공유문구의 이모지는 사용자 결정에 따라 유지.
- `IrisComponents.kt`, MainScreen/WellnessHome/기록/릴레이/휴식/훈련/검사 화면, 위젯·알림·토스트 용어를 수정.
- 초록 테마, 알림 동작 명칭, 상태바 아이콘도 이전 작업에 포함.
- 이전 세션 기록상 Debug 컴파일·단위 테스트·APK 조립 및 에뮬레이터 화면 확인 완료. 이는 이번 후속 변경의 검증을 대신하지 않는다.

## 진행 기록

| 요구사항 | 구현/결정 | 상태 |
|---|---|---|
| 실제 데이터 | `RestHistory.currentRestStreak`로 연속 일수 계산. 로딩/오류를 목표 달성으로 표시하지 않음. 참여자 수 제거 | 완료 |
| 비의료 표현 | 화면 자가 확인·응답 요약으로 변경. 정상 기본값·시력 수치·진단·처방 로직 제거 | 완료 |
| 훈련 겹침 | 원근 조절 4개 모드에서 안내를 타겟 영역 아래로 분리. 작은 화면은 스크롤 가능 | 완료 |
| 홈 팁 | `desc_unmeasured`를 4개 언어로 제공. 미측정 원인을 무조건 권한 없음으로 단정하지 않음 | 완료 |
| 제목 대비 | 배터리 제목은 흰색, 설명은 밝은 회색. 상태별 어두운 배경 유지 | 완료 |
| 번역 | 한국어·영어·일본어·스페인어 각 302개 키, 형식 인자 일치. 자가 확인/공유/오류/시간/알림 보완 | 완료 |
| 색톤 | 설정 → 일반 → 앱 색톤. 5종, 즉시 반영·원자적 파일 저장·재실행 유지 | 완료 |

## 상세 변경과 복구 기준

- **기록:** `RestHistory.kt`, `MainScreen.kt`. 7일 이상일 때만 연속 목표 달성. 오늘 이전에 휴식을 했어도 어제 날짜가 없으면 연속 일수는 끊긴다. 완료 기록이 아직 없거나 읽기 오류가 있으면 확인 불가 상태로 표시한다. `GuidedSessionViewModel.eyeBatteryStats`도 기록이 준비되지 않으면 미측정으로 처리한다.
- **위젯:** 기존 제품은 Widget 1만 등록되어 있다. `IrisFitWidgetProviders.kt`의 사용하지 않는 Widget 2–6 렌더링을 제거하여 주간 가짜 점수·+20%·7일째·무조건 완료 표시를 없앴다. 비활성 위젯을 다시 제품에 추가하지 않았다. Widget 1의 실제 기록 기반 표시와 갱신 경로는 유지했다.
- **자가 확인:** `EyeExamScreen.kt`는 고리, 방사형/십자 선, 빨강/초록 바탕과 숫자 예시를 유지한다. 보정된 시력표·5m 검사·JCC/Ishihara 검사라는 표현은 제거했다. 양안 시력 1.0, 정상/이상, 근시/난시 판정, AI 주치의, 처방전, 의료 평균 비교를 생성하지 않는다.
- **응답:** 고리의 마지막 진행 단계는 시력 수치가 아니다. 미응답은 `null`이며 선택한 선/색/숫자만 요약한다. 화면 이동·Activity 재생성에는 유지하고, 다시 체험하면 모두 초기화한다. 영구 완료 기록에는 저장하지 않는다. 고리 위/아래 방향과 Canvas의 열린 부분이 뒤집혀 있던 문제도 `RingDirection.kt`로 수정하고 검증했다.
- **표현:** 하단의 시력 보호는 화면 편안함으로 변경. 면책 안내와 증상 지속 시 전문가 상담 안내는 유지한다.
- **원근 조절:** `EyeGymGameScreen.kt`에서 `gym_gazing` 텍스트를 Canvas 밖으로 옮겼다. `focus_target`, `focus_guidance` 태그로 실제 영역의 비겹침을 검증한다. 훈련 시간/완료 저장 산식은 바꾸지 않았다.
- **홈:** `WellnessHome.kt`의 배터리 제목/설명 대비와 팁을 수정했다. 상태 색 및 기존 배터리 구성은 유지한다.
- **번역:** `AppLocalization.kt`의 명시적 번역은 더 이상 부분 문자열 자동 치환을 거치지 않는다. 일본어/스페인어의 누락 키도 모두 채웠다. 숨겨진 레거시 화면용 자동 치환은 호환성을 위해 남겨두었다.
- **언어 경로:** `LanguageManager.effectiveLanguage`로 앱 언어와 기존 선택을 함께 처리하고 ViewModel·위젯·알림에 연결했다. 시스템 언어만 조회해서 Android 앱별 언어와 엇갈리던 경로를 보완했다.
- **시간/안내:** `ScreenTimeManager.formatScreenTime`을 실제 표시 함수로 분리해 시간·분·미측정을 번역했다. `MainActivity`, `GuidedSessionViewModel`, `BreakReminderScheduler`의 관련 토스트·오류·알림 동작도 번역한다. SNS 공유 이모지는 승인대로 유지한다.
- **색톤:** `AppColorTone.kt`, `Theme.kt`, `ColorToneDialog.kt`, `ColorTonePreferences.kt`, `GuidedSessionViewModel`, `MainActivity`, `SettingsTab` 연결. 기본 초록을 유지하고 블루·라벤더·피치·그레이의 밝은/어두운 테마를 제공한다. 배터리 상태 색과 자가 확인의 시표 색은 바꾸지 않는다. 시스템 전체 필터가 아니다.
- **색톤 저장:** 앱 내부 `appearance_v1.txt`에 `v1,green` 같은 안정적인 ID를 기존 `FileCompletionStorage`로 저장한다. 완료 기록 삭제와 분리되어 있다. 저장 실패 시 이전 색으로 복원하고 Toast/대화상자 오류와 로그를 남긴다. 잘못된 저장값을 읽으면 기본 초록 표시 사실과 재선택 안내를 알린다.
- **기록 진입점:** `Walkthrough.md`, PM 통합 Walkthrough 및 결정 로그에 본 문서를 연결했다. `.github\copilot-instructions.md`에 다음 세션의 기록 확인·갱신 규칙을 남겼다.

## 추가/보완 테스트

- `RestHistoryTest`: 빈 기록, 하루 중복, 오늘 휴식 전/후, 날짜 공백, 훈련 제외, 미래 날짜 제외, 월/연도/윤일 경계.
- `ColorTonePreferencesTest`: 기본값, 5종 모두 재생성 후 읽기, 잘못된 버전/ID, 저장 실패 전파.
- `AppLocalizationTest`: 4개 언어 키 집합·형식 인자 일치, 비한국어 번역의 한글 누락 검사, 배터리 상태/색톤 동적 키, 실제 연속 일수 포맷.
- `IrisColorSchemeTest`: 5종 × 라이트/다크의 주요 전경/배경 쌍 및 배터리 제목·설명 대비 **4.5:1 이상**.
- `RingDirectionTest`: Canvas 고리의 열린 방향과 선택 라벨 일치.
- `ScreenTimeFormattedTest`: 테스트 안에 복제한 산식 대신 실제 표시 함수를 호출. 0분/분/시간, 지원 언어, 권한 거부/쿼리 실패의 미측정.
- `UiFollowupTest`: 5종 선택/저장/Activity 재생성, 자가 확인의 미응답/선택 응답/재생성/초기화, 원근 조절 4개 모드의 안내 영역.

## 검증 및 한계

- 환경: Windows, 로컬 Debug APK, `emulator-5554` Android 17/API 37, 1080×2400. 연결된 개인 실기기는 설치/테스트 대상으로 사용하지 않았다.
- **PASS:** `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.RestHistoryTest --tests com.example.myapplication.ColorTonePreferencesTest --tests com.example.myapplication.AppLocalizationTest --tests com.example.myapplication.IrisColorSchemeTest --tests com.example.myapplication.RingDirectionTest --tests com.example.myapplication.ScreenTimeFormattedTest :app:assembleDebug :app:assembleDebugAndroidTest --console=plain -q`. 대상 JVM 테스트 **16 passed / 0 failed / 0 skipped**. 결과: `app\build\test-results\testDebugUnitTest`, `app\build\reports\tests\testDebugUnitTest`.
- **PASS:** `.\gradlew.bat :app:compileReleaseKotlin --console=plain -q`; Release boundary 검증도 통과. Release APK 서명·배포를 수행한 것은 아니다.
- **PASS:** 에뮬레이터에 최종 Debug/test APK 설치 후 `adb -s emulator-5554 shell am instrument -w -e timeout_msec 90000 -e class com.example.myapplication.UiFollowupTest com.eyeoverlay.app.test/androidx.test.runner.AndroidJUnitRunner`: **6 tests OK**, 최종 재실행 67.912초.
- **초기 실패와 수정:** 기존 Espresso 3.5.1은 API 37의 `InputManager.getInstance` 부재로 실행 전에 실패했다. 테스트 전용 의존성을 Espresso 3.7.0 / AndroidX JUnit 1.3.0으로 갱신했다. 이후 동일 문구를 가진 홈 카드/탭의 선택자를 구분했고, 애니메이션 시계와 Activity 재생성 이후 동작은 semantics action으로 검증하도록 조정했다. 중간 타임아웃/중단은 성공으로 계산하지 않는다.
- **PASS:** `git diff --check`; 4개 언어 사전 각 302개 키에 중복 없음. Android `strings.xml`의 언어별 리소스 키 누락 없음.
- **화면 확인:** 홈 제목과 팁, 설정과 5종 선택창, 블루 선택 후 프로세스 강제 종료/재실행 유지(`v1,blue`), 스페인어 자가 확인의 다크 블루 화면을 확인했다. 이후 에뮬레이터를 원래 앱 언어(자동), 라이트 모드, 초록 기본값으로 복원했다.
- 증거 디렉터리: `C:\Users\youns\.copilot\session-state\362de1b0-1a4f-4407-b740-f7b3839e7fe3\files`. 최종 UI 로그 `ui-followup-tests-final.log`; 화면 `followup-home.png`, `followup-settings.png`, `followup-tone-picker.png`, `followup-blue-restart.png`, `followup-selfcheck-es-dark.png`. 중간 실패 로그도 같은 디렉터리에 별도 보존했다.
- 실기기 정확도/센서/전력, Play 배포, 독립 QA: NOT RUN. 기존 출시 보류를 해제하지 않는다.
- 기존 전체 androidTest는 이 작업의 통과 범위가 아니다. 오래된 UI 문구에 의존하는 테스트, 숨겨진 AI/레거시 화면, 미지원 언어 정책 등은 별도 범위다.
- 배터리 카드의 기존 마스코트·자세 배지 구성과 시스템 바 표현은 이번 제목 대비 수정 외에는 재설계하지 않았다.
- 이 문서는 대화 초기화 후 복구를 위한 저장소 기록이다. 로컬 파일 기록은 원격 백업이나 Git 커밋을 의미하지 않는다.

## 추가 수정 (2026-11-19): 상단 바 안전 영역 · 앱 아이콘

- 문제: 실기기(Galaxy S23, Android 16)에서 눈동자 훈련 화면 상단 `뒤로` 버튼이 상태바 아래로 숨고, 가로 모드에서는 왼쪽 내비게이션 바에 가려졌다. targetSdk 36 Edge-to-edge 환경에서 공통 `IrisTopBar`가 시스템 인셋을 처리하지 않았기 때문이다.
- 수정: `ui\IrisComponents.kt`의 `IrisTopBar` 내용에 `WindowInsets.safeDrawing.only(Top + Horizontal)` 패딩을 적용했다. 배경은 상태바 뒤까지 유지된다. 원근 조절 훈련·시선 추적 훈련·화면 자가 확인 3개 화면에 함께 적용된다.
- 검증: `:app:assembleDebug` PASS, 폰 `adb install -r` 설치(데이터 유지) 후 가로 모드 원근 조절 화면에서 상태바·내비게이션 바와 겹치지 않음을 확인. 증거 `gym_topbar_fix2.png`(세션 files 폴더).
- 알림/앱 아이콘: 알림 작은 아이콘은 이미 흰색 벡터 `ic_stat_irisfit`였지만, One UI는 알림 왼쪽 큰 원에 앱 아이콘을 보여 주므로 초록 스마일 마스코트(`ic_mascot_happy`)가 보였다. 사용자 승인에 따라 기본 앱 아이콘 `ic_launcher_theme_1`(application + `MainActivityDefault`)을 같은 '눈 + 게이지 링' 벡터 마크로 교체했다. 새 리소스: `ic_launcher_irisfit_foreground.xml`(66dp 안전 영역 안, monochrome 겸용), `ic_launcher_irisfit_background.xml`(`#286452`, 앱 기본 초록). 홈 배터리 카드·위젯의 마스코트와 사용되지 않는 테마 2–10은 변경하지 않았다.
- 검증: `:app:assembleDebug` PASS, 폰 설치(데이터 유지) 후 시스템 '애플리케이션 정보'에서 새 아이콘 표시를 확인. 증거 `new_app_icon_preview.png`, `appinfo_new_icon2.png`. 새 알림은 배터리 상태가 바뀔 때만 발송되므로 알림 화면은 직접 캡처하지 않았다.

## 전수 확인 (2026-11-19): 운동 중단 후 재시작 시 앱 종료

- 사용자 재현 경로: 홈의 눈 건강 배터리 카드에서 시선 추적 훈련을 열고 시작 → `중단하기` → 홈으로 복귀 → 같은 훈련의 `훈련 시작하기`를 누르면 앱이 종료된다.
- 정적 전수 범위: `app/src/production/kotlin`에서 `prepareWorkoutSession` 및 운동 상태 전이를 검색하고, 홈 카드 진입점, 두 훈련 화면, `GuidedSessionViewModel`의 세션 시작/취소/완료 처리를 확인했다. 해당 세션 API를 호출하는 훈련 화면은 `EyeGymGameScreen`(원근 조절, `eye_gym`)과 `SmoothPursuitGameScreen`(시선 추적, `smooth_pursuit`) 두 곳이다.
- **확인된 공통 결함:** 두 화면 모두 `중단하기`에서 `gameState = "READY"`만 수행하고 `cancelWorkoutSession()`은 호출하지 않는다. 첫 시작에서 ViewModel의 `activeWorkoutRoutineId`가 설정된 뒤 남아 있으므로, 같은 훈련 또는 다른 훈련을 다시 시작하면 `prepareWorkoutSession()`의 `check(activeWorkoutRoutineId == null)`가 실패하고 `IllegalStateException("이미 진행 중인 훈련 세션이 있습니다.")`가 발생하는 코드 경로다.
- **추가 동일 경로:** 두 화면의 상단 뒤로 가기 및 완료 화면의 홈 이동도 화면만 홈으로 바꾸며 세션을 취소하지 않는다. 활성 세션 상태에서 홈으로 나간 뒤 어떤 훈련이든 다시 시작해도 같은 검사를 만난다. `PAUSED` 화면의 `훈련 취소` 버튼만 명시적으로 `cancelWorkoutSession()`을 호출한다. 성공 저장은 ViewModel에서 활성 ID를 해제한다.
- **테스트 공백:** 기존 UI 테스트는 원근 조절 화면의 중단 동작만 확인하며, 중단 후 재시작이나 시선 추적의 중단·재시작을 검증하지 않는다. 세션 준비의 활성 ID 가드도 ViewModel 단위 테스트로 다루지 않는다.
- 판정: 보고된 앱 종료와 일치하는 확정적인 코드 경로를 두 훈련에서 모두 확인했다. 다만 현재 ADB 기기가 연결되지 않고 IDE 디버거도 실행되지 않아 실기기 예외 로그 및 재현은 **NOT RUN**이다. 정적 코드 흐름 검토만 완료했으며 이번 확인에서 소스 코드는 변경하지 않았다.
- 근거 경로: `ui/EyeGymGameScreen.kt` 시작/중단/취소 버튼, `ui/SmoothPursuitGameScreen.kt` 시작/중단/취소 버튼, `session/GuidedSessionViewModel.kt` `prepareWorkoutSession`, `cancelWorkoutSession`, `completeWorkout`.

## 추가 수정 (2026-11-19): 훈련 동작 및 3D 위성 시선 추적

- 사용자 요청/승인: 원근 조절의 `빠른 점멸`은 타겟이 빠르게 나타났다 사라지도록, `파동 리플`은 동심원이 확산되도록 수정. 시선 추적의 기존 4개 모드를 우주에서 위성이 공전하는 3D 형상으로 표현한다. 기존 네 모드 이름과 선택 동작은 유지한다.
- 원근 조절: `EyeGymGameScreen.kt`에서 점멸은 100ms fade-in/out 및 짧은 표시/숨김 구간(약 0.6초 주기, 2Hz 미만)으로 만들고, 파동은 3개의 동심원이 겹쳐 반복 확산되도록 바꿨다. 두 모드별 설명을 훈련 전·중 모두 보여준다. 눈 깜빡임을 유도하는 안내만 추가했으며 안구 건조 완화/치료 효과를 보장하는 문구는 추가하지 않았다.
- 시선 추적: `SatelliteOrbitCanvas.kt`를 추가했다. 어두운 우주 배경과 별, 중심 행성, 앞·뒤 깊이에 따른 반투명 궤도와 가려짐, 작은 위성을 Canvas로 그린다. 기존 4개 동작(무한대, 8자, 원, 지그재그)의 궤도 함수를 3D 깊이 및 원근 투영으로 표현한다. 위성 안내 문구를 4개 지원 언어에서 현지화했다.
- 재시작 결함 수정: 이전 정적 조사에서 확인한 두 훈련의 `중단하기`가 화면 상태만 초기화하고 활성 세션 ID를 남기던 문제를 함께 수정했다. 두 화면 모두 중단하기·상단 뒤로 가기·Android 시스템 뒤로 가기·완료 후 홈 이동에서 `cancelWorkoutSession()`을 호출하고 로컬 세션 ID도 비운다. 일시정지 후 이어 하기는 세션을 유지한다.
- 변경 파일: `ui/EyeGymGameScreen.kt`, `ui/SmoothPursuitGameScreen.kt`, 신규 `ui/SatelliteOrbitCanvas.kt`, `session/AppLocalization.kt`, 신규 `test/SatelliteOrbitGeometryTest.kt`, `androidTest/UiFollowupTest.kt`.
- 검증 **PASS** (Windows, 2026-11-19): `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.SatelliteOrbitGeometryTest --tests com.example.myapplication.AppLocalizationTest :app:assembleDebugAndroidTest :app:assembleDebug --console=plain -q`. 궤도 기하 2/2 및 번역 2/2 테스트 통과, Debug APK 및 AndroidTest APK 조립 성공. 네 궤도 경로의 차이·주기성·캔버스 범위와 4개 언어의 번역 키 집합을 확인했다. `git diff --check`도 통과했다.
- **추가 기기 검증 PASS (2026-11-19):** Galaxy S23 (`SM-S916N`, Android 16/API 36)에 최신 Debug APK와 AndroidTest APK를 `adb install -r`로 설치하고 앱 데이터를 보존했다. `UiFollowupTest#pulseGuidanceDoesNotOverlapTarget` 및 `UiFollowupTest#pursuitCanRestartAfterStopAndUsesSatelliteOrbitCanvas` 모두 기기에서 **OK (1 test each)**. 따라서 원근 조절 중단→재시작과 시선 추적 중단→재시작, 시선 추적의 위성 Canvas 표시가 실기기 UI 회귀 테스트를 통과했다. 앱은 `target_screen=pursuit`로 다시 열어 사용자 점검을 위해 대기 중이다. 준비 화면 증거 `pursuit_ready_s23.png`(세션 files 폴더).
- 모션 그래픽의 실제 육안 품질, 네 궤도 각각의 플레이 중 표시, 점멸/리플 가독성, 장시간 성능·전력은 자동 회귀 테스트로 측정되지 않아 **사용자 확인/별도 QA 대기**. Release 빌드/배포 및 Play 배포는 하지 않았다.
- Debug APK: `app\build\outputs\apk\debug\app-debug.apk`. 변경 전체는 미커밋이며 Release Hold 유지.

## 추가 수정 (2026-11-19): 시선 추적 행성별 시각화

- 사용자 요청/승인: 기존 네 궤도 모드의 행성을 요청 순서대로 지구·목성·토성·금성으로 표현한다. 모드 이름, 궤도 동작, 운동 시간은 바꾸지 않는다.
- 매핑: 무한대=지구, 8자=목성, 원형=토성, 지그재그=금성. 플레이 화면에 해당 행성 이름을 표시하고 한국어·영어·일본어·스페인어 번역을 추가했다.
- 시각화: 지구는 청색 바다·녹색 대륙·구름, 목성은 적갈/크림색 대기 띠와 대적점, 토성은 황갈색 띠와 전·후면 고리, 금성은 황금색 두꺼운 구름층으로 구분했다. 벡터 Canvas 기반의 양식화된 그림이며 사진·과학 시뮬레이션 수준의 사실성을 주장하지 않는다.
- 변경 파일: `ui/SatelliteOrbitCanvas.kt`, `ui/SmoothPursuitGameScreen.kt`, `session/AppLocalization.kt`, `test/SatelliteOrbitGeometryTest.kt`, `androidTest/UiFollowupTest.kt`.
- **PASS (2026-11-19):** `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.SatelliteOrbitGeometryTest --tests com.example.myapplication.AppLocalizationTest :app:assembleDebugAndroidTest :app:assembleDebug --console=plain -q`. 궤도/행성 매핑 및 번역 테스트 통과, Debug와 AndroidTest APK 조립 성공.
- **PASS (2026-11-19, 실기기 UI):** Galaxy S23 (`SM-S916N`, Android 16/API 36)에 Debug APK와 AndroidTest APK를 `adb install -r`로 설치. `UiFollowupTest#pursuitCanRestartAfterStopAndUsesSatelliteOrbitCanvas` **OK (1 test)**; 위성 Canvas 표시, 현 모드 행성 이름 표시, 중단 후 재시작 경로 확인.
- `git diff --check` 통과. 첫 기기 테스트에서 홈 카드가 무작위 훈련 모드를 정하는 동작을 고려하지 않고 지구 이름만 가정한 assertion이 실패했다. 테스트를 네 행성 중 현재 모드의 이름을 확인하도록 바로잡아 동일 기기에서 재실행했고 통과했다. 앱의 기능 실패는 아니었다.
- 당시 미검증: 네 모드 각각의 행성 그림을 수동 비교하지 않았다. 이후 확인 결과는 아래 작은 위성/자전 기록에 구분해 남긴다. 시선 추적 외 기능, 장시간 성능·전력, 전체 androidTest suite는 이번 범위에서 실행하지 않았다. 커밋/원격 업로드/배포 없음; Release Hold 유지.

## 추가 수정 (2026-11-19): 작은 자연 위성 및 행성 자전

- 사용자 요청/승인: 태양전지판이 있는 인공위성 대신 작은 위성을 표시하고, 지구·목성·토성·금성이 훈련 중 자전하도록 한다.
- 작은 위성: 패널과 안테나 형태를 제거하고, 음영·표면 크레이터가 있는 작은 달 형태의 구체로 교체했다. 안내 문구도 한국어·영어·일본어·스페인어에서 작은 위성으로 맞췄다.
- 행성 자전: 행성별 표면/구름 패턴이 훈련 애니메이션 각도에 맞춰 회전한다. 20초 훈련 동안 약 한 바퀴 조금 넘게 도는 속도로 표시하고, 토성 고리는 행성 표면과 분리해 궤도면에 고정했다.
- 변경 파일: `ui/SatelliteOrbitCanvas.kt`, `session/AppLocalization.kt`. 이전 추가 작업의 매핑/화면 파일과 함께 동작한다.
- **PASS (2026-11-19):** `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.SatelliteOrbitGeometryTest --tests com.example.myapplication.AppLocalizationTest :app:assembleDebugAndroidTest :app:assembleDebug --console=plain -q`; 대상 단위 테스트 및 APK 조립 통과.
- **PASS (2026-11-19, 실기기):** Galaxy S23 (`SM-S916N`, Android 16/API 36)에 Debug/AndroidTest APK 설치 후 `UiFollowupTest#pursuitCanRestartAfterStopAndUsesSatelliteOrbitCanvas` **OK (1 test)**. 기기 화면을 약 2초 간격으로 캡처해 지구 표면 패턴의 회전 및 패널 없는 작은 위성 표시를 확인했다. 증거: 세션 파일 `planet-spin-a.png`, `planet-spin-b.png`.
- 전체 4개 행성 각각의 실제 기기 육안 비교는 미실시. 단위 테스트는 네 모드와 행성 이름 매핑을 확인한다. 별도 전체 androidTest suite 및 장시간 성능/전력은 NOT RUN. 커밋/원격 업로드/배포 없음; Release Hold 유지.

## 추가 수정 (2026-11-19): 지그재그를 부드러운 S자 경로로 교체

- 사용자 피드백: 지그재그 궤적이 자연스럽지 않아 다른 훈련으로 변경 요청. 대안으로 제안한 S자 경로가 무한대 궤적과 다르다는 설명 후 사용자가 적용을 승인했다.
- 변경: 지그재그의 삼각파형 방향 전환 경로를 부드럽고 비대칭인 S자 물결 궤적으로 바꿨다. 기존 무한대/8자/원형과 구별되는 4번째 궤도이며 행성 매핑(금성), 훈련 시간, 위성/자전 애니메이션은 유지한다. 모드 이름은 `부드러운 S자`로 변경하고 한국어·영어·일본어·스페인어에 반영했다.
- 변경 파일: `ui/SatelliteOrbitCanvas.kt`, `ui/SmoothPursuitGameScreen.kt`, `session/AppLocalization.kt`, `test/SatelliteOrbitGeometryTest.kt`.
- **PASS (2026-11-19):** `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.SatelliteOrbitGeometryTest --tests com.example.myapplication.AppLocalizationTest :app:assembleDebugAndroidTest :app:assembleDebug --console=plain -q`. 새 단위 검증은 S자 궤도의 주기적 연결과 360개 표본 사이 급격한 점프가 없는지 검사한다.
- **PASS (2026-11-19, 실기기):** Galaxy S23 (`SM-S916N`, Android 16/API 36)에 Debug/AndroidTest APK 설치 후 `UiFollowupTest#pursuitCanRestartAfterStopAndUsesSatelliteOrbitCanvas` **OK (1 test)**.
- 한계: 네 궤적 간 시각적 구별/사용자 체감은 실기기에서 별도 육안 평가하지 않았다. 전체 androidTest suite, 장시간 성능·전력은 NOT RUN. 미커밋이며 커밋/원격 업로드/배포 없음, Release Hold 유지.
