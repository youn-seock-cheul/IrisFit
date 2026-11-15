# IrisFit 현재 구현 상태 감사

- 작성일/시각: 2026-11-15 11:32 KST부터 감사 수행
- 역할: Developer — 읽기 전용 구현·Release 감사
- 감사 ID: `AUD-01`; 기준점 작업 ID: `AUD-02`
- 저장소: `youn-seock-cheul/IrisFit`
- 기준 branch/HEAD: `master` / `1abd6d2402c1a63f62954efa8b333a8778d44f8d`
- 앱 설정 기준: `com.eyeoverlay.app`, version `1.0.0` (`versionCode=1`)
- 문서 상태: **Developer 감사 완료 — QA 재검토 완료, PM 결정 대기**
- 출시 상태: **Release Hold 유지**

## 1. 결론 요약

핵심 휴식·운동 세션과 앱 내부 완료 기록은 실제 로컬 구현이 확인됐고, Debug unit test 48개, Release Kotlin 컴파일, Release boundary 검사, signing-config 검사 및 Release lint 작업이 실행됐다. 단, 이 증거는 실제 기기 동작이나 Play 승인 증거가 아니다.

현재 Release는 베타 진입 승인을 내릴 상태가 아니다. 특히 Android 13+ 알림 런타임 권한 요청 부재, 배터리 최적화 제외 요청의 Play 정책 위험, 위젯에서 미측정 데이터를 100%처럼 보이는 문제, 커뮤니티의 데모 데이터를 실제 참여자처럼 표시하는 문제, 사용자 데이터 삭제 범위 불일치가 남아 있다. Eye Battery 산식/의미, AI 노출·데이터 고지, 베타 범위는 PM 결정이 필요하다. 아래 지적은 감사 단계 finding이며 코드·정책은 변경하지 않았다.

## 2. 감사 전 기준점 및 변경 보존

감사 전 `git status --porcelain=v1 -uall`에서 staged 변경은 없었고, **추적 파일 24개 수정, 미추적 파일 34개**가 있었다. `git diff --stat`: 24 files changed, 1,142 insertions(+), 699 deletions(-). 본 감사는 저장소 파일을 수정하거나 기존 변경을 정리하지 않았다. 테스트/lint 생성물은 기존 `app/build` 아래에만 생성됐다.

### 감사 시작 당시 수정 파일 (모두 unstaged)

```text
.gitignore
.idea/deploymentTargetSelector.xml
PRIVACY_POLICY.md
README.md
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/myapplication/ai/GeminiNanoCoachManager.kt
app/src/main/res/layout/iris_fit_widget.xml
app/src/main/res/values-es/strings.xml
app/src/main/res/values-ja/strings.xml
app/src/main/res/values-ko/strings.xml
app/src/main/res/values/strings.xml
app/src/production/kotlin/com/example/myapplication/MainActivity.kt
app/src/production/kotlin/com/example/myapplication/data/SmartDevicePoseDetector.kt
app/src/production/kotlin/com/example/myapplication/service/BlueLightFilterService.kt
app/src/production/kotlin/com/example/myapplication/session/AppLocalization.kt
app/src/production/kotlin/com/example/myapplication/session/EyeBatteryCalculator.kt
app/src/production/kotlin/com/example/myapplication/session/GuidedSessionViewModel.kt
app/src/production/kotlin/com/example/myapplication/ui/EyeGymGameScreen.kt
app/src/production/kotlin/com/example/myapplication/ui/MainScreen.kt
app/src/production/kotlin/com/example/myapplication/ui/SmoothPursuitGameScreen.kt
app/src/production/kotlin/com/example/myapplication/ui/WellnessHome.kt
app/src/production/kotlin/com/example/myapplication/widget/IrisFitWidgetProvider.kt
docs/WALKTHROUGH_P0_LATEST.md
```

### 감사 시작 당시 미추적 파일

```text
.github/prompts/qa-review.prompt.md
.idea/appInsightsSettings.xml
AUDIT_REPORT_KO.md
WIDGET_UPDATE_NOTES.md
Walkthrough.md
app/google-services.json
app/src/main/java/com/example/myapplication/service/BlueLightFilterService.kt
app/src/main/res/drawable/ic_nav_eye_comfort.xml
app/src/main/res/drawable/widget_preview_1.xml
app/src/main/res/drawable/widget_preview_4.xml
app/src/main/res/drawable/widget_preview_6.xml
app/src/main/res/layout/iris_fit_widget_1.xml
app/src/main/res/layout/iris_fit_widget_2.xml
app/src/main/res/layout/iris_fit_widget_3.xml
app/src/main/res/layout/iris_fit_widget_4.xml
app/src/main/res/layout/iris_fit_widget_5.xml
app/src/main/res/layout/iris_fit_widget_6.xml
app/src/main/res/layout/iris_fit_widget_large.xml
app/src/main/res/xml/iris_fit_widget_info_1.xml
app/src/main/res/xml/iris_fit_widget_info_2.xml
app/src/main/res/xml/iris_fit_widget_info_3.xml
app/src/main/res/xml/iris_fit_widget_info_4.xml
app/src/main/res/xml/iris_fit_widget_info_5.xml
app/src/main/res/xml/iris_fit_widget_info_6.xml
app/src/production/kotlin/com/example/myapplication/data/PoseClassifier.kt
app/src/production/kotlin/com/example/myapplication/session/WorkoutCompletion.kt
app/src/production/kotlin/com/example/myapplication/ui/AiCoachChatDialog.kt
app/src/production/kotlin/com/example/myapplication/ui/UsageAccessDisclosureDialog.kt
app/src/production/kotlin/com/example/myapplication/widget/IrisFitWidget2Provider.kt
app/src/production/kotlin/com/example/myapplication/widget/IrisFitWidgetProviders.kt
app/src/test/java/com/example/myapplication/EyeBatteryCalculatorTest.kt
app/src/test/java/com/example/myapplication/IrisFitQaRegressionTest.kt
app/src/test/java/com/example/myapplication/PoseClassifierTest.kt
app/src/test/java/com/example/myapplication/WorkoutCompletionTest.kt
```

`app/google-services.json`는 미추적 상태만 확인했으며, 내용은 열람·복사하지 않았다. 감사 이후 task 폴더에 추가한 이 보고서는 저장소 밖에 있으므로 저장소 변경이 아니다.

## 3. 기능 인벤토리

| 기능 | 정적 조사 결과 | 실행 검증 상태 및 근거 |
|---|---|---|
| 첫 실행/홈 | **부분 구현** — 홈에서 점수, 휴식/운동, 목표, 커뮤니티 카드로 이동한다. 첫 설치·권한 거부 흐름은 기기에서 실행하지 않았다. | 실제 UI 흐름 **NOT RUN** |
| Usage Access | **구현 확인** — 설명/취소/설정 이동이 있고 거부 시 측정값을 미측정으로 표시하려는 경로가 있다. 로컬 날짜 시작 시각부터 `UsageStatsManager`의 집계를 조회한다. | 실제 허용·거부·철회 및 기기별 집계 **NOT RUN** |
| Eye Battery | **계산 구현, 제품 의미 미승인/미검증** — 화면 사용 15분당 1점 차감, 휴식당 12점/미니게임당 20점 회복 후 20–100으로 제한한다. 시력·눈 상태 측정은 아니다. | 경계값·회복 단위 단위 테스트 포함. 사용자 효용/임상 타당성은 **미확인** |
| 30초 휴식 세션 | **구현 확인** — 단조 시계 기반 30초, 일시정지/재개, 완료 확인 후 저장을 갖는다. 진행 중 세션은 프로세스 재시작 후 복구하지 않는다. | 상태 기계 단위 테스트 PASS; 실제 백그라운드·프로세스 종료 **NOT RUN** |
| 눈 운동 미니게임 | **구현 확인** — 완료 ID와 routine을 완료 저장소에 중복 방지하며 기록한다. | 저장/중복 방지 단위 테스트 PASS; 실제 시각·접근성·기기 테스트 **NOT RUN** |
| 로컬 기록 | **구현 확인** — 앱 전용 `AtomicFile`, 최대 100개 기록, 완료 시 local date 저장, 저장 실패를 UI 오류로 전달한다. 앱 자체 암호화는 없다. | 저장소 단위 테스트 PASS; 앱 데이터 초기화/OS 백업 동작은 기기에서 **NOT RUN** |
| 센서 | **부분 구현** — 가속도계/자이로스코프와 선택적 근접/조도 센서로 기기 자세를 분류한다. 눈 시선/휴식 여부를 감지하지 않는다. 데이터는 현재 코드상 메모리에서 처리된다. 센서 누락/오래된 값은 기본 `HAND_HELD_ACTIVE`로 표시된다. | 분류기 단위 테스트 PASS; 센서 조합별 실기기 확인 **NOT RUN** |
| 알림 | **부분 구현/릴리스 결함 의심** — manifest에 `POST_NOTIFICATIONS`가 있고 알림 코드는 권한을 확인한다. Release 코드에서 런타임 권한 요청 진입점은 찾지 못했다. Android 13+에서 사용자 허가 없이 알림이 전달된다고 보장할 수 없다. | 권한 허용/거부/채널 설정 기기 테스트 **NOT RUN** |
| 위젯 | **부분 구현** — merged Release manifest에는 `IrisFitWidget1Provider` 한 개만 등록되어 있다. 위젯 코드에는 사용시간 미측정과 기록 읽기 실패가 각각 0분/빈 기록으로 대체되어 표시 점수를 만들 수 있다. | 위젯 설치·크기 조정·업데이트/권한 없는 표시 **NOT RUN** |
| AI Coach | **Release UI에서 비활성/미완성** — 탭은 “검토 중” 문구와 개인정보 안내를 보인다. `AiCoachChatDialog`/GenAI 코드 및 SDK는 컴파일 대상이지만 Release UI의 호출 지점은 확인하지 못했다. | 실제 모델 지원·다운로드·AICore 처리·네트워크/보관 동작 **NOT RUN/미확인** |
| PRO/결제 | **데모** — PRO 영역은 기능 데모 문구를 노출하며 결제 SDK/구독 흐름은 확인되지 않았다. | 결제 기능 **미구현 확인(정적 조사)** |
| 커뮤니티/릴레이 | **데모/로컬 상태** — 기본 참가자·릴레이·응원 데이터가 정적 값이며 사용자의 프로필/응원 수는 SharedPreferences에 저장된다. Release runtime dependency에 커뮤니티 서버 SDK가 없고 조사한 흐름에서 네트워크 전송을 찾지 못했다. | 실제 사용자 간 동기화 **미구현 확인(정적 조사)** |
| 한국어/영어/일본어/스페인어 | **부분 구현** — 앱 문자열 맵과 언어별 대체 경로가 있으나 키 누락 시 한국어 fallback이 가능하고 위젯에 하드코딩 문자열이 있다. | 핵심 사용자 여정 4개 언어의 화면 검수 **NOT RUN** |

주요 근거: `app/src/production/kotlin/com/example/myapplication/session/EyeBatteryCalculator.kt:26-53`, `GuidedSession.kt`, `GuidedSessionViewModel.kt:77-93,332-342`, `CompletionRepository.kt`, `FileCompletionStorage.kt`, `ScreenTimeManager.kt:36-55`, `SmartDevicePoseDetector.kt`, `PoseClassifier.kt:56-68`, `MainScreen.kt:968-1030,1159-1182,1226-1234,1318-1325`, `AiCoachChatDialog.kt:38`, `CommunityCheerCard.kt:32-40,248,571-573`, `RelayPreferences.kt`, `IrisFitWidgetProviders.kt:30-35,75-86,424`.

## 4. 개인정보 및 데이터 흐름 증거표

| 데이터 | 수집/조회·처리 | 앱 자체 저장 | 외부 전송·보관 | 고지/삭제 상태 |
|---|---|---|---|---|
| UsageStats 앱 사용시간 | 사용자가 Usage Access를 허용하면 Android `UsageStatsManager` 집계값을 조회해 당일 지표를 계산한다. 앱별 원본을 자체 완료 ledger에 기록하지 않는다. | 완료 ledger에는 저장하지 않음 | 이 데이터 경로의 앱 자체 전송은 확인하지 못함. Android/타 SDK의 별도 통신은 이 사실만으로 배제 불가 | 사전 설명 및 거부 대체 흐름이 코드에 있음. 실제 설정 이동/철회는 기기 검증 필요 |
| 완료 기록/점수 | 루틴 ID, 생성 UUID, 완료 시각·로컬 날짜, 확인값을 저장한다. 점수는 기록과 현재 사용시간으로 재계산한다. | 앱 전용 `filesDir`의 `AtomicFile`, 앱 암호화 없음, 최대 100행. manifest는 `allowBackup=false` | 조사한 저장소 경로는 로컬. 앱 삭제/시스템 백업은 실제 기기 미검증 | 삭제 버튼은 완료 ledger와 marker 설정만 지운다. 커뮤니티 SharedPreferences 등은 남는다 |
| 센서값/자세 | 센서 원시값으로 기기 자세를 분류한다. 눈이나 눈의 휴식을 관찰하지 않는다. | 조사한 코드에서 원시 센서값의 지속 저장 없음 | 조사한 센서 흐름에서 외부 전송 없음. SDK/시스템 전체 네트워크 동작을 일반화하지 않음 | 개인정보 안내에 메모리 처리/자세 추정이 기재됨 |
| 프로필/응원 | 별칭, 이모지, 한 줄 목표, 응원 수 | `iris_relay_prefs_v1` SharedPreferences | 조사한 코드에서 서버 동기화 없음 | 전체 삭제 동작에서 삭제되지 않음. 다른 사용자와 공유되는 실시간 커뮤니티처럼 보이는 UI 문구는 실제와 불일치 |
| AI 질문/응답 | 구현된 dialog 코드의 프롬프트에는 질문(최대 600자)과 현재 점수가 포함됨 | 대화 기록의 앱 파일 저장은 확인하지 못함 | ML Kit GenAI Prompt → Android AICore SDK가 release runtime에 포함되고 merged manifest에 AICore 서비스 바인딩 및 `INTERNET`이 존재한다. SDK/시스템의 통신·보관·학습 처리 근거는 확인하지 못함 | UI는 AI 기능을 검토 중이라 표시. 실제 호출/동의/공급자 고지·보관정책은 미확인 |
| 분석/진단/광고/결제 | `AnalyticsManager`는 placeholder 상태. runtime graph에 Firebase Analytics/Crashlytics 및 결제 SDK를 확인하지 못함 | 확인된 수집 없음 | ML Kit 의존성으로 Firebase encoders 및 Google Play services 공통 모듈은 포함. 이는 Analytics/Crashlytics 포함의 증거가 아니다. | Play Data safety 제출 상태/Play Console은 접근하지 않아 **BLOCKED** |

기준 자료 `PRIVACY_POLICY.md`는 UsageStats·센서·로컬 저장과 GenAI SDK 미확인 사항을 일부 구분한다. 단, 공개 정책 URL의 배포/연락처(`support@irisfit.app`) 유효성, Play Data safety 폼, AICore의 구체적인 전송·보관·학습 조건은 검증하지 않았다. 해당 파일의 존재만으로 정책 게시나 Play 신고 완료를 주장하지 않는다.

## 5. 결함 및 PM 결정 필요 항목

아래 `AUD-01-Fxx`는 감사 finding ID다. PM이 범위를 승인하기 전에는 임의로 `IRIS-REQ-*`나 개발 지시로 전환하지 않는다.

| ID | 우선순위 | 기대/실제 및 영향 | 재현/근거 | 제안 및 최소 검증 |
|---|---|---|---|---|
| AUD-01-F01 | P1 | 알림 스위치를 켜도 Android 13+ 런타임 권한을 요청하지 않아 `POST_NOTIFICATIONS`가 허가되지 않은 설치에서 상태 알림/휴식 알림이 전달되지 않을 수 있다. | 설정에서 알림 켜기 → 권한 미허가 상태에서 상태 변경/알림 예약. Release 코드에는 요청 UI가 없고 scheduler는 권한 미허가 시 반환한다. 실제 기기 재현은 미실행. | PM이 알림을 베타 기능으로 유지할지 결정. 유지 시 명시적 설명·런타임 요청·거부 대체 경험을 구현하고 Android 13+ 허용/거부/재허용을 검증. |
| AUD-01-F02 | P1 | 미측정 사용시간을 위젯이 0분으로 처리하고 `screenTimeMeasured=false`를 전달하지 않는다. 기록 읽기 예외도 빈 목록으로 대체한다. 이 조합은 점수가 100%로 계산되어 정상/측정 완료처럼 보일 수 있다. | Usage Access 거부 또는 파일 읽기 오류에서 위젯 갱신. `IrisFitWidgetProviders.kt:75-85`, `EyeBatteryCalculator.kt:26-53`. 실제 기기 재현은 미실행. | 위젯에 미측정/오류 상태를 표시하고 성공값처럼 표시하지 않도록 설계. 권한 거부·저장 오류·허용 상태의 위젯 결과를 검증. |
| AUD-01-F03 | P1 | “기기 기록 전체 삭제” 동작은 완료 ledger와 marker 설정만 지운다. nickname/emoji/goal/cheer count는 별도 `RelayPreferences`에 남아 데이터 삭제 기대와 어긋날 수 있다. | 커뮤니티 프로필/응원 값을 저장 → 설정에서 전체 삭제 → 앱 재진입. 실제 기기 재현은 미실행. `GuidedSessionViewModel.kt:332-342`, `RelayPreferences.kt`. | PM은 삭제 대상에 프로필/커뮤니티 값을 포함할지, 아니면 버튼·안내가 해당 설정을 보존한다고 명확히 할지 결정. 모든 저장 위치를 검사하는 테스트 추가. |
| AUD-01-F04 | P1 | 릴레이/글로벌 챌린지에서 예시 참가자·참가자 수가 실제 동기화된 사용자 데이터처럼 노출된다. 현재 확인된 수치와 참가자는 하드코딩/로컬 상태로 서버 연동이 아니다. 실제 사용자 커뮤니티로 오인될 수 있다. | 홈에서 호출되는 `CommunityCheerWidget`의 샘플 친구/릴레이/방명록 및 `PublicGlobalChallengeCard.kt:24,69-110`의 고정 참가자 수를 정적으로 확인했다. `CommunityCheerCard.kt`의 별도 선언/문자열 맵은 이 finding의 실제 노출 근거로 사용하지 않는다. | 베타에서 기능을 제외하거나 “데모/기기 내 예시”로 재설계. 실제 온라인 기능으로 광고하지 않음. |
| AUD-01-F05 | P1 | Release 설정 화면에서 배터리 최적화 제외 설정 진입을 제공한다. lint `BatteryLife`가 `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` 사용을 Play 정책 위험으로 표시했다. | Release lint 결과 및 `MainScreen.kt:1226`. lint는 해당 권한 요청을 정책상 허용되는 용도로 사용해야 한다고 경고했다. | 핵심 기능 요건 및 허용 사용 사례가 입증되지 않으면 해당 설정 유도 제거를 우선 검토. 제거/유지 결정과 Play 정책 근거를 PM이 승인. |
| AUD-01-F06 | P1 — PM 결정 | 점수 산식과 “눈 건강 배터리/피로도” 표시가 눈 건강을 측정한 것처럼 읽힐 여지가 있다. 구현은 screen-time/완료 건수 가중치로 만든 임의 웰니스 지표이며 검증된 건강 측정 근거가 확인되지 않았다. | 산식 `EyeBatteryCalculator.kt:26-53`; 사용자-facing 제목/설명은 `WellnessHome.kt` 및 `AppLocalization.kt`. 계산 단위 테스트는 임상·사용성 검증이 아님. | PM은 산식, 점수 이름/단계 설명, 베타 노출 문구와 측정 여부 표현을 승인해야 한다. 증거 전 의료/진단 효능을 주장하지 않음. |
| AUD-01-F07 | P2 — 활성화 전 PM 결정 | AI 대화 UI는 비활성으로 표시되고 Release UI 호출 지점은 확인되지 않았지만 GenAI SDK 및 prompt 코드가 Release runtime에 포함된다. 공급자/OS 처리 조건, 네트워크·보관·학습은 미확인이다. 현재 사용자의 질문이 전송되는 활성 흐름은 확인되지 않아 즉시 노출된 데이터 결함으로 단정하지 않는다. | `AiCoachTab`은 “검토 중” 상태; dialog 정의는 있으나 호출 지점 미확인. Release merged manifest에는 `INTERNET` 및 AICore binding. | 현재처럼 비활성 상태를 유지하거나 SDK/코드를 제외한다. AI 기능을 활성화하기 전 처리·고지·동의·기기 지원을 확인하고 별도 승인을 받는다. 온디바이스 추론 코드만으로 시스템/SDK 데이터 흐름을 단정하지 않음. |
| AUD-01-F08 | P2 | `gradlew app:tasks --all`은 Compose Preview task 생성 시 “Android resources are excluded from unit tests” 오류로 실패한다. 특정 테스트/lint task는 실행된다. 작업 목록 조회 및 환경 재현성이 저하된다. | 실행 명령과 원문 실패 로그를 감사 세션에서 확인. 프로젝트에서 `includeAndroidResources` 명시 설정은 찾지 못함. | 개발 승인 후 Gradle/Compose Preview 구성 호환성을 조사. 우선 단위 테스트 및 Release lint 직접 실행 경로는 사용 가능함을 확인. |
| AUD-01-F09 | P2 | Release lint 결과에 총 195건(192 warning, 3 note)이 있고 앱 소스 경로 결과는 169건(86 UseKtx, 64 UnusedResources, 12 HardcodedText, 나머지 7건)이다. widget 문자열 하드코딩과 언어 관련 경고가 있다. | `lint-results-release.sarif`; `AppBundleLocaleChanges`는 `LanguageManager.kt:29`, `HardcodedText`는 widget layout. lint task 자체는 성공. | 베타 진입 전에 사용자 노출 하드코딩 문자열 및 실제 4개 언어 여정을 우선 검토하고, 불필요 리소스/스타일 경고를 분리해 정리. |

## 6. Release 설정 및 실행 결과

### 설정/manifest

- `compileSdk=36`, `minSdk=33`, `targetSdk=36`, `applicationId=com.eyeoverlay.app`, `versionCode=1`, `versionName=1.0.0` (`app/build.gradle.kts:16,52-56`).
- 2026-08-31부터 신규 앱/업데이트는 Android 16/API 36 이상을 target해야 한다는 Android Developers 공식 문서 기준으로 targetSdk 36은 현재 API 최소 요건을 만족한다. 이는 Play 정책 전체 준수나 승인 증거가 아니다: https://developer.android.com/google/play/requirements/target-sdk
- Release source set은 `src/production/kotlin`과 공통 data/reminder/theme/AI 경로를 포함하고 카메라 연구용 서비스/vision 코드는 debug source set에 둔다.
- Release merged manifest에서 확인된 주요 권한: `POST_NOTIFICATIONS`, `VIBRATE`, `PACKAGE_USAGE_STATS`, `INTERNET`, `ACCESS_NETWORK_STATE`, AICore binding 및 AndroidX/ML Kit 관련 자동 병합 항목.
- Merged manifest에는 `MainActivity`, 10개 launcher alias, 내부 reminder receiver, exported app-widget receiver 1개가 있다. camera, overlay, install-package 권한/연구 overlay service/CameraX/Face Detection이 없다는 Release boundary 검사는 통과했다.
- Release runtime 의존성은 ML Kit GenAI Prompt `1.0.0-beta4`, Play services/ML Kit 공통 모듈 등을 포함한다. Firebase Analytics, Crashlytics, Billing 라이브러리는 runtime graph에서 확인되지 않았다.
- `keystore.properties`와 설정된 keystore 존재를 값 출력 없이 확인했고 `verifyReleaseSigningConfig`가 통과했다. 어떤 비밀번호/키 파일 내용도 읽거나 기록하지 않았다.

### 검증 상태

| 명령/검증 | 상태 | 결과 |
|---|---|---|
| `.\gradlew.bat app:testDebugUnitTest --no-configuration-cache` | **PASS** | 48 tests, 0 failures, 0 errors, 0 skipped |
| `.\gradlew.bat app:lintRelease app:verifyReleaseSigningConfig --no-configuration-cache` | **PASS (warnings 있음)** | Release Kotlin 컴파일, `verifyReleaseBoundary`, signing config, lint task 완료. lint SARIF 195건: 192 warning, 3 note; `BatteryLife` 정책 주의 1건 포함 |
| `.\gradlew.bat app:tasks --all --no-configuration-cache` | **FAIL** | Compose Preview의 `generateDebugComposePreviewRunfiles` task 구성에서 unit test Android resources 제외 오류 |
| 기존 APK `apksigner verify --verbose --print-certs` | **PASS** | 기존 APK 서명 구조 확인, APK 서명 검증 성공 |
| 기존 AAB `jarsigner -verify` | **PASS with warnings** | jar 서명 확인. 인증서가 self-signed이며 신뢰 chain/타임스탬프 없음 경고. `-strict`는 인증서 신뢰 오류로 exit 4; 서명 설정/Play App Signing과 일치하는지 확인되지 않음 |
| Release AAB/APK 재생성 또는 업로드 | **NOT RUN** | signing/deployment 작업 금지 범위. Play Console/실기기 접근도 하지 않음 |
| 실제 기기, 권한, 알림, 센서, 화면 크기, 4개 언어, Play Data safety | **NOT RUN / BLOCKED** | 이 감사에서 기기/Play Console 증거를 확보하지 않음 |
| 기존 APK/AAB 16 KB native alignment | **PASS (기존 파일만 읽기 전용 검사)** | APK와 AAB 각각의 arm64-v8a/x86_64 ELF 2개를 검사해 모든 PT_LOAD가 16 KB 정렬 조건을 만족했다. 새 package 생성이나 서명은 수행하지 않음 |

`lintRelease` 성공은 lint warning이 없다는 뜻이 아니다. 발견된 정책 위험은 finding `AUD-01-F05`에 기록했다.

### 현재 존재하던 Release 산출물

다음 파일은 감사 전에 이미 존재했다. 날짜가 감사일 전이므로 현재 작업 트리의 HEAD/변경분에서 생성된 것인지 입증되지 않는다. 감사를 위해 재빌드하지 않았다.

| 파일 | 크기 | 수정 시각 (UTC) | SHA-256 | 서명 확인 |
|---|---:|---|---|---|
| `app/build/outputs/apk/release/app-release.apk` | 3,400,396 bytes | 2026-11-14 02:41:42.5426930Z | `EC102B04F9AA34CC50E40A33BB6C2633E54DF0687FC2455A2CAE05E75047DFF1` | apksigner PASS |
| `app/build/outputs/bundle/release/app-release.aab` | 4,547,683 bytes | 2026-11-14 12:13:46.6898417Z | `17619E423F5698706FF1503E1C0A8F49AA07938EE29C78852C80124ADB16049E` | jarsigner verified; self-signed chain/time-stamp 경고 |

Play Console의 업로드 키/Play App Signing 설정 및 현재 등록 versionCode는 확인하지 못했다. 따라서 이 산출물이나 signing config로 업로드 가능성을 확정할 수 없다.

## 7. 베타 MVP 권고 (PM 승인 아님)

1. 베타 후보: 홈/Usage Access의 명시적 거부 대체 경로, 30초 휴식과 실제 동작이 확인된 눈 운동, 로컬 완료 기록. 모든 건강 표현은 PM이 승인한 비의료적 의미에 한정한다.
2. 베타에서 우선 숨기거나 명확히 데모 표시: 온라인처럼 보이는 community/relay/global challenge, 구현 연결이 확인되지 않은 AI chat, PRO/유료 기능.
3. 베타 승인 전 게이트: F01 알림 동작/권한 정책, F02 미측정 위젯 표시, F03 삭제 범위, F05 배터리 최적화 요청의 Play 적합성, F06 점수 의미/산식 결정, 공개 개인정보 처리방침과 Data safety 검증.
4. 별도 기기/Play 검증: Android 13+ permission 허용·거부, 날짜/시간대 변경 및 프로세스 종료, 지원·미지원 센서, 위젯 설치/갱신, 한국어·영어·일본어·스페인어 핵심 여정, 업로드 대상 AAB·인증서·Play 트랙.

이 권고는 기본설계 승인, 개발 승인, Release Hold 해제 또는 배포 승인을 의미하지 않는다.
