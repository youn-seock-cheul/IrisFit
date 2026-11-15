# IrisFit 실제 시청시간 추정 — PM 9차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `59bb238` (`fix(trend-ui): prevent unmeasured score leak in BatteryTrendCard and enforce maximumRange proximity bounds`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** XML 결과 독립 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `59bb238`의 변경은 `SmartDevicePoseDetector.kt`, `BatteryTrendCard.kt` 및 관련 문서를 포함한다. `.\gradlew.bat :app:installDebug`가 성공했고 연결 기기에서 MainActivity 실행을 시작했다. 현재 UI dump에서는 이전 테스트 때처럼 유의미한 홈 화면 내용을 독립 확인하지 못했다. 이번 검토에서는 앱 설치/Activity 실행만 확인하며 제품 UI 수치나 정확도는 확인한 것으로 보지 않는다.
2. Gradle XML 결과를 독립 합산해 **63 tests, 0 failures/errors, 0 skipped**를 확인했다. 추가된/관련 S23+ 모델 테스트는 모델 탐지 경로를 확인하지만 센서 이벤트 및 UI 미측정 처리를 직접 검증하지 않는다.
3. proximity 유효성 검사에 `values[0] <= maximumRange`가 추가됐고, 이를 통과하기 전 freshness timestamp는 갱신되지 않는다. 주변 센서 callback을 직접 주입하는 경계 테스트는 여전히 확인되지 않았다.
4. `BatteryTrendCard`는 역사적 점수 계산에 `screenTimeMeasured`를 전달한다. 하지만 미측정이면 calculator가 `-1`을 반환하고, 현재 점수 역시 `-1`일 수 있다. 이후 차트는 모든 값을 20–100 범위로 clamp하여 선/점으로 그리고, 하단 라벨은 `$score%`를 출력한다. 따라서 변경은 계산 입력 상태를 전달했으나 미측정 표시 누출을 해결하지 못한다.
5. 확인한 다른 소비처에도 미측정 처리가 남아 있다: `MainScreen.kt`의 Goal/AI Coach 및 relay 제안은 `-1%` 표기나 점수 기반 문구를 만들 수 있다. `WellnessHome.kt` 상단 수치는 `미측정`으로 바뀌었으나 게이지는 -1을 0으로 clamp하며 범례는 위험 상태처럼 강조될 수 있다.
6. 기기의 현재 battery status는 discharging으로 관찰됐다. 그러나 승인된 배터리 비교 프로토콜/기준선/합격 기준이 없고 이번에는 전력 측정을 실행하지 않았으므로 battery/heat 결과는 **NOT RUN**이다. 정답 라벨 기반 자세 및 실제 시청시간 정확도 역시 **NOT RUN**이다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 실제 기기 모델 매핑 | **PASS (코드/단위 테스트 범위)** — 기존 S916 식별 경로 및 모델 테스트 확인 |
| 2 | proximity `maximumRange` 상한 | **PASS (정적 코드 범위)** — sample 값이 범위 이하인지 검사한 뒤 freshness 갱신. 직접 callback 경계 테스트는 없음 |
| 3 | BatteryTrendCard 미측정 처리 | **FAIL** — -1 점수를 그래프 점으로 clamp하고 하단 수치 라벨에 `-1%`로 표시할 수 있음 |
| 4 | 모든 점수 소비처 미측정 처리 | **FAIL** — MainScreen AI/Goal/relay 및 Wellness gauge/legend가 미측정 상태를 점수처럼 소비할 수 있음 |
| 5 | 자동 테스트 | **PARTIAL PASS** — 전체 JVM 63/0/0. Detector freshness 또는 화면의 미측정 렌더링을 직접 확인하는 테스트는 없음 |
| 6 | 실기기/정확도/전력 | **PARTIAL PASS / NOT RUN** — Debug 설치와 Activity 실행 확인. 실제 sensor freshness, viewing/posture accuracy, battery/heat는 미실시 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 측정 불가 상태를 정상 점수·위험 등급·추세 변화로 오인시키지 않고, 센서 범위 밖 데이터가 최신 측정으로 인정되지 않도록 한다.
* **상세 요구사항 (What):**
  - 기능 1: `BatteryTrendCard`에서 측정 불가 점은 path/point/수치 라벨에서 제외하고 명시적으로 `미측정`을 표시한다. 임의로 0 또는 20%로 치환하지 않는다.
  - 기능 2: MainScreen의 Goal, AI Coach, relay 추천 및 Wellness 게이지/범례 등 모든 활성 score consumer에서 `UNMEASURED`를 분기 처리한다. 미측정이면 백분율, 위험 등급, 달성 여부, 점수 기반 추천을 생성하지 않는다.
  - 기능 3: proximity 입력의 `maximumRange` 경계 안/초과, NaN/Infinity, malformed payload 및 invalid timestamp 각각이 freshness에 미치는 결과를 자동 테스트한다. 정상 값의 기존 동작도 회귀 검증한다.
  - 기능 4: 각 주요 UI 소비처의 measured/unmeasured 렌더링을 테스트하고, 전체 JVM 테스트 결과를 재보고한다.
* **예외 처리 및 엣지 케이스:** 현재 점수가 미측정이고 이전 구간은 측정됨(또는 반대)인 혼합 추세, 하루 전체 미측정, 그래프 점이 0개/1개인 경우를 다룬다. 측정 실패를 유효한 0으로 바꾸지 않는다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. UsageStats 및 자세 신호를 실제 시선/실제 시청시간으로 표현하지 않으며, 진단/의료 측정으로 오인될 수 있는 문구를 피한다.
* **완료 조건 (Acceptance Criteria):** 미측정값이 어떠한 점수·백분율·위험 구간·목표 성공·trend line으로도 보이지 않는다. 정상 측정값은 기존 방식대로 표시된다. 센서/UI 경계 자동 테스트가 추가되고 전체 단위 테스트가 통과한다. 실기기 accuracy/battery는 승인된 별도 절차가 없으면 **NOT RUN**으로 보고한다.

## 4. PM 판정

`59bb238`은 proximity 상한 검증을 보완했으나, QA가 보고한 BatteryTrendCard 미측정 누출 차단은 코드상 완료되지 않았다. AI/Goal/relay 및 Wellness gauge/legend에도 미측정 소비가 남아 있다. JVM 테스트 **63/0/0**은 확인했으나 센서 및 UI 경계 테스트의 증거는 아니다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
