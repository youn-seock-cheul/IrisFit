# 👁️ IrisFit - 게이미피케이션 기반 안구 건강 & 휴식 습관 플랫폼

**IrisFit**은 듀오링고(Duolingo)와 애플 피트니스(Apple Fitness)의 게이미피케이션 철학을 결합한 상용 수준의 안드로이드 안구 건강 관리 애플리케이션입니다. 스마트폰 사용 시간에 따라 실시간으로 소모되고, 30초 눈 휴식 및 안구 운동 미니게임을 완료하면 즉시 충전되는 **'역발상 눈 건강 배터리(Eye Health Battery)'** 모델을 제공합니다.

---

## 🏗️ 핵심 아키텍처 및 데이터 흐름 (Core Architecture)

```mermaid
graph TD
    A["UsageStatsManager 및 기기 센서"] -->|실시간 화면 응시 시간 & 자세 수집| B["GuidedSessionViewModel"]
    B -->|단일 진실 공급원 - Single Source of Truth| C["EyeBatteryCalculator"]
    C -->|EyeBatteryStats (점수 및 피로도 상태)| D["Jetpack Compose UI (MainScreen & WellnessHome)"]
    D -->|24시간 실시간 주식형 트렌드 커브| E["DailyStockChartView"]
    D -->|안과 전문 정밀 자가 진단 및 리포트| F["EyeExamScreen"]
```

### 주요 모듈 설명:
1. **눈 건강 배터리 계산 엔진 (`EyeBatteryCalculator.kt`)**:
   - 스크린타임 페널티, 완료된 휴식 세션, 미니게임 회수, 기기 자세 계수를 종합하여 안구 피로도 점수(15%~100%)를 실시간 산출합니다.
   - 앱 전반에 걸쳐 **단일 진실 공급원(Single Source of Truth)**으로 동작하여 대시보드와 트렌드 차트 간의 100% 데이터 동기화를 보장합니다.
2. **스마트 센서 포즈 감지기 (`SmartDevicePoseDetector.kt`)**:
   - 가속도, 자이로스코프, 근접, 조도 센서를 융합하여 기기 사용 자세를 4단계(`손에 쥐고 사용`, `책상 위 놓임`, `누워서 시청`, `폰 뒤집힘`)로 정밀 판정합니다.
3. **안과 전문 정밀 검사 스위트 (`EyeExamScreen.kt`)**:
   - 실제 안과 진료실 수준의 3단계 자가 진단을 제공합니다:
     - **시력 검사**: 무작위 란돌트 고리 인터랙티브 테스트 + 5m 표준 안과 시력검사표 뷰어.
     - **난시 검사**: 12방향 방사선 시표(Clock Dial) 및 크로스실린더(JCC) 테스트.
     - **적녹 및 색각 검사**: 적녹 굴절 균형(Duochrome) 및 이시바시 색각 이상 판독(Ishihara Plate) 테스트.
     - **AI 주치의 리포트**: 측정 결과 기반 맞춤 진단 소견, 평균 기준 비교표, 시력 프로그레스 바 그래프 제공.
4. **소셜 릴레이 & 글로벌 챌린지 (`CommunityCheerCard` & `PublicGlobalChallengeCard`)**:
   - 3대 핵심 홈 카드 구조(목표 카드, 친구 릴레이 카드, 공개 글로벌 챌린지 카드).
   - 내 차례(`isMyTurn`) 조건부 바통 터치 버튼(`[🏃 1초 바통 터치]`), 푸시 알림 시뮬레이션, 3.5초 세로 자동 롤링 방명록 바너.
5. **다국어 및 운영 인프라 (`AppLocalization.kt` & `AnalyticsManager.kt`)**:
   - 한국어, 영어, 일본어, 스페인어 실시간 다국어 반응형 동기화.
   - Firebase Analytics / Crashlytics 로그 뼈대 및 구글 플레이 스토어 심사 방어(Prominent Disclosure) 완비.

---

## 🚀 시작하기 (Getting Started)

1. **Android Studio** (Koala / Ladybug 또는 최신 버전)에서 프로젝트를 엽니다.
2. Gradle 파일 동기화를 진행합니다 (`app/build.gradle.kts`).
3. 안드로이드 실기기 또는 에뮬레이터에서 빌드 및 실행합니다 (`./gradlew app:assembleDebug`).

---

## 📜 라이선스 (License)
Copyright © 2026 IrisFit Inc. All rights reserved.
