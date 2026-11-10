# 👁️ IrisFit - 개발자 가이드 및 아키텍처 백서 (Developer Guide)

**IrisFit**은 듀오링고(Duolingo)와 애플 피트니스(Apple Fitness)의 게이미피케이션 철학을 결합한 상용 수준의 안드로이드 안구 건강 관리 애플리케이션입니다.

---

## 🏗️ 1. 프로젝트 아키텍처 및 모듈화 구조 (Architecture & Decoupling)

IrisFit은 코드 결합도(Coupling)를 낮추고 단일 책임 원칙(SRP)을 철저히 준수하기 위해 **MVVM 패턴**과 **기능별 컴포넌트 모듈화 구조**를 채택하고 있습니다.

```
com.example.myapplication/
├── data/                  # 데이터 관리, 센서 감지, SharedPreferences (RelayPreferences, AppSessionTimer)
├── session/               # 비즈니스 로직, 배터리 계산 엔진, 범용 자동 번역 엔진 (GuidedSessionViewModel, AppLocalization, EyeBatteryCalculator)
├── service/               # 백그라운드 서비스 (BlueLightFilterService)
├── ui/                    # Jetpack Compose UI 컴포넌트 (모듈화 완료)
│   ├── CommunityCheerCard.kt        # 친구 릴레이 & 방명록 카드 (독립 모듈)
│   ├── PublicGlobalChallengeCard.kt # 공개 글로벌 릴레이 카드 (독립 모듈)
│   ├── BatteryTrendCard.kt          # 주간/월간/일간 트렌드 차트 (독립 모듈)
│   ├── GuidedSessionCard.kt         # 30초 눈 휴식 및 충전 다이얼로그
│   ├── SmoothPursuitGameScreen.kt   # 무한대 시선 추적 미니게임
│   ├── EyeExamScreen.kt             # 안과 전문 정밀 자가 진단 및 AI 리포트
│   └── MainScreen.kt                # 메인 라우터 및 네비게이션 컨테이너
└── widget/                # 홈 화면 앱 위젯 제공자 (IrisFitWidgetProvider)
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
   - 네비게이션 바, 주요 타이틀 등 고정된 UI 키(`"nav_home"`, `"settings_header"`, `"bluelight_title"`)는 `stringsEn` / `stringsJa` / `stringsEs` 정적 맵에서 O(1) 속도로 즉시 조회합니다.
3. **2단계: 범용 자동 번역 엔진 (Dynamic Auto-Translator)**:
   - 정적 맵에 직접 정의되지 않은 동적 문구(`"참여자 3421명 참여 중 • 탭하여 즉시 동참 및 보상 받기"`, `"오늘도 맑은 눈 피로 싹 날리고..."`)는 `autoTranslate()` 파이프라인을 통과하여 조사, 숫자, 접미사, 고유 명칭이 실시간 타겟 언어로 완벽히 자동 대치됩니다.

---

## 🧪 4. 테스트 및 빌드 가이드 (Testing & Build)

- **단위 테스트 실행**:
  ```bash
  ./gradlew app:testDebugUnitTest
  ```
- **디버그 APK 빌드 및 실기기 배포**:
  ```bash
  ./gradlew app:assembleDebug
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```

---

## 📜 라이선스 (License)
Copyright © 2026 IrisFit Inc. All rights reserved.
