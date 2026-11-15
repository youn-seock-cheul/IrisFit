# IrisFit 실제 시청시간 추정 — PM 11차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `ce6d320` (`fix(trend-ui): apply unmeasured placeholder check to weekly and monthly trend tabs in BatteryTrendCard`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `ce6d320`. `BatteryTrendCard`는 일간/주간/월간 탭을 공통 content box로 감싸고, 현재 `screenTimeMeasured == false` 또는 current score가 음수이면 placeholder를 보여준다. 해당 조건문은 소스에서 확인했다.
2. 일간/주간/월간 차트 본문은 미측정 시 공통 분기에서 렌더링되지 않는다. 다만 `BatteryTrendCard`의 탭 선택 UI는 계속 사용할 수 있고, chart period별로 실제 측정 데이터가 존재하는지와 과거 구간별 측정 가능성은 판정하지 않는다. 현재 전역 측정 상태가 measured면 주간/월간 점수는 여전히 휴식/운동 기록만으로 30–100을 계산한다. 이는 `EyeBatteryCalculator`나 검증된 screen-time 측정값에 기반하지 않는 임의의 배터리 점수다.
3. 현재 UI 조건은 `!screenTimeMeasured || currentScore < 0`만 확인한다. `screenTimeMeasured == true`이고 current score가 유효해도 과거 데이터 범위가 측정됐다는 뜻은 아니다. 따라서 주간/월간 각 구간에 측정된 값이 있는지 별도 추적되지 않는다.
4. `MainScreen.kt`의 Goal/AI Coach 및 relay 제안은 미측정 sentinel 점수를 백분율/추천에 사용할 수 있다. `WellnessHome.kt` 상단 점수/상태는 미측정 경로가 있으나 게이지는 -1을 0으로 clamp하며 danger legend가 강조될 수 있다.
5. Gradle 단위 테스트 실행 성공; XML 합계 **63 tests, 0 failures/errors, 0 skipped**. `app/src/test`에는 chart/tab UI, `BatteryTrendCard`, Compose 미측정 렌더링 또는 `SmartDevicePoseDetector` callback/freshness 경계 테스트가 없다. 따라서 QA 보고서의 모든 탭 UI 렌더링 `PASS`는 자동 테스트 결과가 아니다.
6. 이전 구현의 proximity `maximumRange` sample 상한 검사는 freshness 갱신 전에 이뤄진다. 직접 callback 검증은 여전히 미실시. 이번 검토에서 Debug 앱 설치/UI 표시와 실제 viewing/posture accuracy, sensor freshness 및 battery/heat 시험도 수행하지 않았다. 하드웨어 시험 결과는 **NOT RUN**이다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 일간 차트 미측정 placeholder | **PASS (코드 경로 한정)** — 전역 미측정 조건이면 일간 plot을 숨김. UI renderer 자동 테스트 없음 |
| 2 | 주간/월간 각 구간 측정 데이터 보존 | **PARTIAL / FAIL** — 전역 미측정이면 전체 placeholder. 전역 measured일 때는 개별 기간 자료가 없어도 활동기록 기반 임의 점수 bar를 노출 |
| 3 | 나머지 UI 소비처 | **FAIL** — MainScreen AI/Goal/relay 및 Wellness gauge/legend가 미측정값을 점수처럼 소비할 수 있음 |
| 4 | 단위 테스트 | **PARTIAL PASS** — 전체 JVM 63/0/0. chart tab/UI 및 detector freshness 경계 테스트는 없음 |
| 5 | proximity bounds | **PASS (정적 코드 범위)** — 상한 검사 뒤 freshness 갱신; 직접 callback 테스트는 없음 |
| 6 | 실기기/정확도/전력 | **NOT RUN** — 이번 QA 제출에 앱 UI 렌더링의 독립 재실행 증거 없음. viewing/posture accuracy, sensor freshness, battery/heat 미측정 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 실제 측정 데이터가 없는 기간에 임의 점수를 보여주지 않고, 측정 불가 상태를 모든 UI에서 일관되게 전달한다.
* **상세 요구사항 (What):**
  - 기능 1: 주간/월간 추세를 실제 score measurement data source에 연결한다. 활동 완료 횟수만으로 30–100의 배터리 점수를 만들지 않는다. 각 기간/일별 미측정은 해당 구간을 `미측정`으로 표시하거나 값이 있는 구간만 plot한다.
  - 기능 2: 각 기간별 데이터가 없을 때와 전체 current score가 미측정일 때를 분리해 다룬다. 현재 측정 상태를 과거 모든 기간의 측정 근거로 간주하지 않는다.
  - 기능 3: MainScreen Goal, AI Coach, relay 추천 및 Wellness gauge/legend에서 `UNMEASURED`를 독립 상태로 처리하고 점수 백분율·위험 상태·목표 성공·점수 기반 추천을 생성하지 않는다.
  - 기능 4: 일/주/월 탭 각각의 measured, unmeasured, mixed-data, no-history 렌더링 자동 테스트를 추가한다. sensor payload/freshness 경계 테스트도 추가한다.
* **예외 처리 및 엣지 케이스:** 현재만 측정, 과거 구간만 미측정, 일부 주간 날짜만 측정, 전 구간 미측정, 휴식 기록은 있지만 측정 기록은 없음, UsageStats query error, 데이터 0/1개인 차트 상태를 구분한다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. 앱 사용시간 및 자세 신호를 실제 시선/실제 시청시간으로 과장하지 않는다.
* **완료 조건 (Acceptance Criteria):** UI 어느 기간에서도 휴식/운동 횟수만으로 배터리 점수가 만들어지지 않는다. 실제 측정값이 있는 기간만 score로 표시하며, 그 외 기간은 명시적 `미측정` 상태로 처리된다. 모든 활성 소비처에서 미측정값이 백분율·위험 등급·목표 성공·추천 점수로 변환되지 않는다. chart/UI 및 sensor freshness 경계 테스트가 추가되고 전체 JVM 테스트가 통과한다. 실기기 accuracy/battery는 별도 승인된 절차가 없으면 **NOT RUN**으로 보고한다.

## 4. PM 판정

`ce6d320`은 전역 미측정일 때 주간/월간 임의 점수 bar를 숨기는 조건을 추가했으며 코드 수준 개선으로 수용한다. 그러나 전역 measured 상태만으로 주간/월간 개별 구간의 실제 측정을 증명할 수 없고, measured 상태에서는 활동기록 기반 임의 점수가 계속 표시된다. MainScreen/Wellness 미측정 소비처와 UI/sensor 경계 자동 테스트도 남았다. JVM **63/0/0**은 확인했으나 QA의 화면별 PASS를 뒷받침하는 UI 테스트는 없다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
