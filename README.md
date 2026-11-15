# 👁️ IrisFit - 개발자 가이드 및 아키텍처 백서 (Developer Guide)

**IrisFit**은 듀오링고(Duolingo)와 애플 피트니스(Apple Fitness)의 게이미피케이션 철학을 결합한 상용 수준의 안드로이드 안구 건강 관리 애플리케이션입니다.

---

## 🏗️ 1. 프로젝트 아키텍처 및 모듈화 구조 (Architecture & Decoupling)

IrisFit은 코드 결합도(Coupling)를 낮추고 단일 책임 원칙(SRP)을 철저히 준수하기 위해 **MVVM 패턴**과 **기능별 컴포넌트 모듈화 구조**를 채택하고 있습니다.

```
com.example.myapplication/
├── data/                  # 데이터 관리, 센서 감지, SharedPreferences (RelayPreferences, AppSessionTimer)
├── session/               # 비즈니스 로직, 배터리 계산 엔진, 범용 자동 번역 엔진 (GuidedSessionViewModel, AppLocalization, EyeBatteryCalculator)
├── service/               # 눈 운동 오버레이 등 앱 동작 서비스
├── ui/                    # Jetpack Compose UI 컴포넌트 (모듈화 완료)
│   ├── CommunityCheerCard.kt        # 친구 릴레이 & 방명록 카드 (독립 모듈)
│   ├── PublicGlobalChallengeCard.kt # 공개 글로벌 릴레이 카드 (독립 모듈)
│   ├── BatteryTrendCard.kt          # 주간/월간/일간 트렌드 차트 (독립 모듈)
│   ├── GuidedSessionCard.kt         # 30초 눈 휴식 및 충전 다이얼로그
│   ├── SmoothPursuitGameScreen.kt   # 무한대 시선 추적 미니게임
│   ├── EyeExamScreen.kt             # 안과 전문 정밀 자가 진단 및 AI 리포트
│   └── MainScreen.kt                # 메인 라우터 및 네비게이션 컨테이너
└── widget/                # 홈 화면 위젯 1종(오늘의 눈 건강 요약, 4x2)
```

---

## 🔋 2. 눈 건강 배터리 소모 & 회복 산출 수식 (Eye Battery Calculation Formula)

IrisFit은 기기 센서(가속도계, 자이로스코프, 조도, 근접 센서) 및 스크린타임을 결합한 **안정화 배터리 수식 (`EyeBatteryCalculator.kt`)**을 사용합니다.  
자세를 바꿨을 때 과거 누적 페널티가 한번에 곱해져 점수가 급변(20% ↔ 91%)하는 현상을 방지하기 위해, **선형 기본 차감 + 미세 자세 가중치(Relative Posture Delta)** 수식을 정밀하게 적용했습니다.

### 📐 배터리 점수 산출 공식
$$\text{Score} = \text{clamp}\left(100 - \text{BaseDrain} + \text{RestRecovery} + \text{GameRecovery} + \text{PoseDelta}, \ 20, \ 100\right)$$

#### 1) 화면 응시 기본 차감량 ($\text{BaseDrain}$)
- 화면 사용 시간 15분당 **-1.0%** 차감 (실시간 분 단위 연속 감쇄):
$$\text{BaseDrain} = \left(\frac{\text{ScreenTimeMinutes}}{15}\right) \times 1.0\%$$

#### 2) 안구 휴식 및 미니게임 회복량 ($\text{RestRecovery}, \text{GameRecovery}$)
- **30초 눈 휴식 완료**: 회복량 $+12\%$ / 회
- **안구 미니게임 완료**: 회복량 $+20\%$ / 회

#### 3) 실시간 기기 자세 가중치 ($\text{PoseDelta}$)
누적 역사 전체를 곱하는 대신, 현재 기기 자세에 따라 미세하고 자연스러운 보정치만을 가산/감산하여 수치의 급변을 방지합니다:
- **책상 위 휴식 (`FLAT_ON_DESK`) / 엎어둠 (`FACE_DOWN`)**: $+3\%$ (안구 이완 혜택)
- **누워서 보기 (`LYING_DOWN_VIEWING`)**: $-3\%$ (안구 가중 피로 페널티)
- **손에 쥐고 일반 사용 (`HAND_HELD_ACTIVE`)**: $0\%$ (기본 유지)

---

## 🌐 3. 듀얼 파이프라인 다국어 번역 아키텍처 (Dual-Pipeline Translation Architecture)

IrisFit은 정적 다국어 관리의 한계(누락 및 높은 유지보수 비용)와 동적 번역의 유연성을 동시에 만족하기 위해 **정적 맵(Static Localization Maps)과 자동 번역 엔진(Dynamic Auto-Translator Engine)을 결합한 듀얼 파이프라인(`AppLocalization.kt`)**을 채택하고 있습니다.

### ⚙️ 번역 처리 흐름 (`AppLocalization.get`)
1. **OS 시스템 언어 감지 (`resolveEffectiveLang`)**:
   - 안드로이드 OS의 최상위 활성 로케일(`Resources.getSystem().configuration.locales.get(0)`)을 실시간 감지하여 한국어(`ko`), 영어(`en`), 일본어(`ja`), 스페인어(`es`)를 판별합니다.
2. **1단계: 정적 다국어 맵 조회 (Static Maps)**:
   - 네비게이션 바, 주요 타이틀 등 고정된 UI 키(`"nav_home"`, `"settings_header"`, `"eye_comfort_title"`)는 `stringsEn` / `stringsJa` / `stringsEs` 정적 맵에서 O(1) 속도로 즉시 조회합니다.
3. **2단계: 범용 자동 번역 엔진 (Dynamic Auto-Translator)**:
   - 정적 맵에 직접 정의되지 않은 동적 문구(`"참여자 3421명 참여 중 • 탭하여 즉시 동참 및 보상 받기"`, `"오늘도 맑은 눈 피로 싹 날리고..."`)는 `autoTranslate()` 파이프라인을 통과하여 조사, 숫자, 접미사, 고유 명칭이 실시간 타겟 언어로 완벽히 자동 대치됩니다.

---

## 🧩 4. 홈 화면 위젯 (Widget 1 only)

현재 제공하는 홈 화면 위젯은 **Widget 1: 오늘의 눈 건강 요약(4x2)** 한 종류입니다. 위젯 2~6은 사용자 요청에 따라 Manifest 등록, Provider 클래스, 설정 XML, 레이아웃 및 미리보기 리소스에서 제거했습니다.

- `IrisFitWidget1Provider`: 단일 위젯 Provider
- `iris_fit_widget_1.xml`: 단일 위젯 RemoteViews 레이아웃
- 앱에서 휴식/운동 기록이 변경되면 Widget 1만 갱신합니다.
- 위젯은 실제 안구 상태를 진단하지 않으며 앱 내 활동 기록과 스크린타임 기반의 참고 지표를 표시합니다.

---

## 🧪 5. 테스트 및 빌드 가이드 (Testing & Build)

- **단위 테스트 실행**:
  ```bash
  ./gradlew app:testDebugUnitTest
  ```
- **디버그 APK 빌드 및 실기기 배포**:
  ```bash
  ./gradlew app:assembleDebug
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```
- **Release 서명**: 프로젝트 루트의 Git 미추적 `keystore.properties`만 사용합니다.
  `storeFile`, `storePassword`, `keyAlias`, `keyPassword`가 필요하며,
  `storeFile`의 상대 경로는 `app` 디렉터리 기준입니다. 해당 설정이 없어도
  Debug 빌드는 가능하지만 Release 서명 작업은 실패합니다. 키와 비밀번호는
  저장소에 추가하지 말고 안전한 별도 경로에 백업하세요.
- **자세 센서 폴백**: 가속도계 또는 자이로스코프가 없거나 마지막 이벤트가
  3초보다 오래되면 현재 자세 조회는 `HAND_HELD_ACTIVE`를 반환합니다.
  화면 상태는 500ms 주기 점검으로 갱신되므로 이벤트 중단 후 갱신까지
  대략 3.5초가 걸릴 수 있습니다. 이 동작은 실기기 확인이 필요합니다.

---

## 📜 라이선스 (License)
Copyright © 2026 IrisFit Inc. All rights reserved.

---

## 6. 구현 상태 정합성 메모 (2026-11-12)

- Gradle `namespace`는 `com.example.myapplication`, 실제 설치 앱의 `applicationId`는 `com.eyeoverlay.app`입니다.
- 현재 `GeminiNanoCoachManager.kt`는 Gemini Nano 모델을 호출하지 않고 키워드 기반 로컬 문구를 반환하는 샘플 로직입니다. production의 AI 탭도 실시간 GPT/Gemini 채팅 기능이 아닙니다. 실제 AI 연동 완료로 홍보하지 않습니다.
- 완료 기록은 `FileCompletionStorage.kt`의 앱 내부 `AtomicFile` 파일에 저장됩니다. 암호화 SharedPreferences 또는 Room DB를 사용한다고 설명하지 않습니다.
- 최신 정적 점검/수정 내역은 `AUDIT_REPORT_KO.md`를 참고하세요. 과거 Walkthrough의 빌드·기기 테스트 결과는 해당 시점의 기록이며, 현재 소스의 재검증 결과가 아닙니다.


## 🔧 6. 2026-11-13 기능 정리 및 검증 메모

- 홈 화면 위젯은 Widget 1(오늘의 눈 건강 요약, 4x2)만 유지합니다.
- 앱 자체 화면 오버레이 방식의 블루라이트 필터 구현(`BlueLightFilterService`)은 사용자 요청에 따라 삭제했습니다. 관련 Kotlin 서비스 소스와 호출/import를 제거했습니다.
- 기존 '시력 보호' 화면은 삭제하지 않았습니다. 이 화면은 휴대폰의 디스플레이 설정(`Settings.ACTION_DISPLAY_SETTINGS`)을 여는 안내/바로가기이며, IrisFit이 필터를 직접 적용하거나 시스템의 Eye Comfort Shield 상태를 제어하지 않습니다.
- 디스플레이 설정의 명칭과 제공 기능은 제조사 및 Android 버전에 따라 다를 수 있습니다. UI 문구에서 블루라이트 차단이 안구 질환을 예방한다는 식의 단정적 표현을 제거했습니다.
- 오버레이 권한은 눈 운동 오버레이 서비스에서 여전히 사용되므로, 블루라이트 기능 제거만을 이유로 권한 전체를 삭제하지 않았습니다.
- 이전 Walkthrough에 기재된 `BlueLightFilterService`의 Service 상속 변경은 중간 단계의 기록이며, 최신 소스에서는 해당 서비스 자체가 제거되었습니다.
- 검증은 정적 소스/참조 검사 기준입니다. 실제 Android 빌드와 단위 테스트/Lint/실기기 동작은 사용자 환경에서 다시 확인해야 합니다.

```powershell
.\gradlew.bat app:testDebugUnitTest
.\gradlew.bat app:lintRelease
.\gradlew.bat app:assembleRelease
```


## Gemini Nano 온디바이스 코치 (2026-11-13)

- `GeminiNanoCoachManager`는 Google ML Kit GenAI Prompt API (`com.google.mlkit:genai-prompt:1.0.0-beta4`)의 `Generation.getClient()`를 사용해 Android AICore의 Gemini Nano를 호출합니다. API 키나 자체 클라우드 추론 서버는 사용하지 않습니다.
- AI 탭에 실제 대화 UI를 연결했습니다. 앱은 `checkStatus()`로 `AVAILABLE / DOWNLOADABLE / DOWNLOADING / UNAVAILABLE` 상태를 확인하고, 다운로드 가능한 기기에서는 사용자가 최초 모델 다운로드를 시작할 수 있습니다.
- 미지원 기기에서는 Gemini Nano가 동작한다고 가장하지 않습니다. 지원되지 않음/준비 실패 상태를 사용자에게 표시합니다.
- 답변은 일반 눈 건강 생활습관 안내이며 진단·치료·약 처방을 금지하는 프롬프트를 사용합니다. 갑작스러운 시력 변화, 심한 통증, 외상·화학물질 노출 등은 생성형 답변보다 우선하는 로컬 안전 안내로 처리합니다.
- Prompt API는 베타이며 지원 기기, AICore 버전, 모델 다운로드 상태, 지역/언어 구성에 따라 사용 가능 여부가 달라집니다. 실제 기기에서 상태 확인과 첫 추론을 반드시 검증해야 합니다.
- Release 검증 가드는 GenAI Prompt 및 GenAI Common 모듈만 허용하고, 카메라/비전 연구 SDK 차단은 유지합니다.
- **검증 유의:** 이 수정 ZIP은 소스 정적 점검만 수행했습니다. `app:testDebugUnitTest`, `app:lintRelease`, `app:assembleRelease`는 새 ZIP에서 다시 실행해야 합니다.
