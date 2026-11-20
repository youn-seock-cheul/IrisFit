# IrisFit 베타 MVP/UI QA 후속 수정 및 재검증 — 2026-11-20

- 기준: [2026-11-19 베타 MVP/UI QA 재검수](IrisFit_Beta_MVP_UI_QA_Review_2026-11-19.md)의 QA-BETA-01, QA-BETA-02 FAIL 항목
- 판정: **대상 결함 2건 PASS; 전체 베타 UI QA는 PARTIAL PASS**
- 변경 기준: 기존 승인된 베타 범위와 실제 시청시간 목표/미측정 원칙을 적용했다. 새 제품 정책은 추가하지 않았다.
- Release Hold 유지. 이 기록 작성 시점에는 폰 설치를 하지 않았다. 2026-11-20 후속 사용자 요청에 따라 아래 Debug APK를 Galaxy S23에 로컬 설치했으며, 이는 Release/Play 배포가 아니다. commit, push 및 외부 배포는 하지 않았다.

## 수정 내용

| 항목 | 결과 | 변경 |
|---|---|---|
| QA-BETA-01 — 베타 범위 진입점 | **PASS** | 홈에서 친구 응원 릴레이·공개 글로벌 릴레이 카드를 제거하고 하단 AI 탭을 숨겼다. 설정 탭은 새 인덱스로 연결한다. 관련 기능 소스는 삭제하지 않았다. |
| QA-BETA-02 — 배터리 점수 의미 | **PASS** | 실제 시청시간 측정값이 연결되기 전까지 홈/목표/추세/위젯의 배터리 상태를 `UNMEASURED`로 유지한다. UsageStats를 점수에 사용하지 않으며, 이에 따른 피로 상태 변화 알림도 보내지 않는다. |

홈의 일반 앱 사용시간 표시와 UsageStats 접근 설정은 기존 사용량 표시 경로로 남아 있지만 눈 배터리 점수 계산에는 쓰이지 않는다. 실제 시청시간 evaluator, 정확도 및 전력 검증은 이번 범위에서 구현/실행하지 않았다.

## 변경 파일

- `app/src/production/kotlin/com/example/myapplication/ui/MainScreen.kt` — 홈 릴레이 카드와 AI 내비게이션 숨김, 설정 탭 인덱스 조정.
- `app/src/production/kotlin/com/example/myapplication/session/GuidedSessionViewModel.kt` — 실제 시청시간 미연결 상태를 `UNMEASURED`로 유지하고 proxy 기반 배터리 상태 알림 중지.
- `app/src/production/kotlin/com/example/myapplication/widget/IrisFitWidgetProvider.kt`, `IrisFitWidgetProviders.kt` — 위젯에서 UsageStats 점수 계산 제거.
- `app/src/androidTest/java/com/example/myapplication/UiFollowupTest.kt` — 홈 진입점 숨김 및 미측정 회귀 테스트 추가.
- `Walkthrough.md`, `task/IrisFit_PM_Documentation_Index_and_Decision_Log.md`, `task/IrisFit_PM_User_Walkthrough.md` — 최신 판정과 기록 링크 갱신.

## 최초 후속 검증 결과 — 2026-11-20

- `testDebugUnitTest`: **103/103 통과**.
- `assembleDebug`, `assembleDebugAndroidTest`, `compileReleaseKotlin`, `verifyReleaseBoundary`: **통과**.
- 새 홈 회귀 테스트(`betaHomeHidesUnreleasedEntryPointsAndKeepsBatteryUnmeasured`), Galaxy S23 / Android 16: **1/1 통과**. 릴레이/AI 진입점 부재와 ViewModel의 점수 `-1`, `UNMEASURED`를 확인했다.
- 전체 `UiFollowupTest`, 격리 Android 17 에뮬레이터: **8/8 통과**.
- 같은 전체 클래스의 S23 실행은 **2/8 통과, 6/8 실패**했다. 자가 확인 스크롤 테스트가 약 192초에 timeout된 뒤 나머지 테스트에서 Compose hierarchy를 찾지 못했다. 새 홈 회귀 테스트는 이 단독 S23 실행에서 통과했다. 전체 클래스의 기기 결과는 불안정이며 PASS로 처리하지 않는다.
- `git diff --check`: 통과.

## 폰 설치 — 2026-11-20

- 기기: Galaxy S23 (`SM-S916N`), Android 16.
- 설치물: `app/build/outputs/apk/debug/app-debug.apk` (Debug build, application ID `com.eyeoverlay.app`, version `1.0.0`, versionCode `1`).
- `adb install -r`: **Success**. MainActivity 실행 확인, 프로세스 PID 및 foreground/resumed activity 확인.
- 사용자가 요청한 기기 내 Debug 설치만 수행했다. 서명된 Release 빌드, 스토어/외부 배포, commit/push 및 Release Hold 변경은 하지 않았다.

## 최초 보고 시점의 잔여 QA 게이트

- 전체 AndroidTest suite, 운동 전체 완료·기록 저장의 end-to-end 흐름, 모든 테마/언어 조합의 수동 검수는 이번 후속에서 실행하지 않았다. 앞선 QA 보고서의 제한 결과는 이 날짜의 재검증 증거로 간주하지 않는다.
- 실제 시청시간 정확도, 센서 정확도, 배터리·발열·전력 및 Play 정책 검증은 **NOT RUN**.
- 따라서 QA-BETA-01/02에서 확인된 범위 위반은 수정됐지만 전체 UI QA 종료/PM 완료 판정은 보류한다. 배포 승인 없음.

## 잔여 QA 게이트 재검증 — 2026-11-20

- 범위: 기존 베타 화면과 승인된 사용자 흐름의 자동 QA. 제품 동작/정책 범위는 추가하지 않았고, 테스트는 개인 폰이 아닌 격리 에뮬레이터에 고정했다.
- `:app:compileDebugAndroidTestKotlin`: **통과**.
- `connectedDebugAndroidTest`, `ANDROID_SERIAL=emulator-5554`, `Medium_Phone_API_37.0` / Android 17: **18/18 통과, 0 실패·오류·건너뜀**. 원시 결과: `app/build/outputs/androidTest-results/connected/debug/TEST-Medium_Phone_API_37.0(AVD) - 17.xml`.
- `testDebugUnitTest --rerun-tasks`: **103/103 통과, 0 실패·오류·건너뜀**.
- `UiFollowupTest`: **10/10 통과**. 원근 조절·시선 추적 완료 기록 각각 1건 저장, 홈 베타 진입점/미측정 표시, 4개 언어 × 5개 색톤의 홈 사용성 자동 smoke를 확인했다. 이 20개 조합의 자동 UI 검사는 사람의 수동 시각 검수를 대체하지 않는다.
- `GuidedSessionUiTest`: **2/2 통과**. 휴식 완료의 사용자 확인/저장과 회전·백그라운드 후 수동 재개를 확인했다.
- `WellnessHomeTest`: 현재 production 화면 기준으로 다시 구성한 **2/2 통과**. 화면 내 휴식 흐름 취소, 완료 ledger/마커 설정 삭제와 기존 저장값 복원을 확인했다.
- `ExampleInstrumentedTest`는 실제 `BuildConfig.APPLICATION_ID`와 비교하도록 수정했고, 알림 테스트는 번역별 알림 제목/종료 액션 리소스를 사용하도록 수정했다. 두 테스트 모두 전체 suite에서 통과했다.
- 이전 `GuidedSessionUiTest`의 마커 데모/마커 슬라이더 검사는 production 베타 화면에서 접근 가능한 경로와 해당 컨트롤이 확인되지 않아 제거했다. 마커 데모 자체의 사용자 흐름은 **NOT RUN**이며, 이 조정은 해당 기능을 통과 판정했다는 뜻이 아니다.
- `git diff --check`: **통과** (줄바꿈 변환 안내 경고만 출력).
- 수동 시각 검수, Galaxy S23 전체 UI 클래스 재실행, 실제 센서/자세/시청시간 정확도, 배터리·발열·전력 및 Play 정책 검증은 **NOT RUN**. 정확도·전력 기준과 시험 절차는 승인되지 않아 임의 기준을 만들지 않았다.
- 최종 판정: 자동 QA 게이트는 **PASS**; 제품 전체 QA/PM 완료는 위 미검증 게이트 때문에 **PARTIAL PASS**. Release Hold 유지. 이 재검증에서 추가 폰 설치, commit, push 또는 배포는 하지 않았다.

### 이번 재검증 변경 파일

- `app/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.kt` — application ID assertion을 실제 Gradle application ID와 비교.
- `app/src/androidTest/java/com/example/myapplication/BreakReminderDeliveryTest.kt` — 기기 언어에 맞는 알림 제목/종료 액션 확인.
- `app/src/androidTest/java/com/example/myapplication/GuidedSessionUiTest.kt` — 현재 앱의 휴식 흐름·번역 문구를 기준으로 유지하고, 현재 production 화면에서 노출되지 않는 마커 데모 테스트를 제외.
- `app/src/androidTest/java/com/example/myapplication/WellnessHomeTest.kt` — 구형 단독 홈 화면 가정 대신 현재 production 화면의 휴식 취소와 기록 삭제/복원 검증.
