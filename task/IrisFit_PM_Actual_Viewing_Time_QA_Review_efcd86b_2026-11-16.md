# IrisFit 실제 시청시간 추정 — PM 10차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `efcd86b` (`fix(trend-ui): prevent unmeasured score leak in BatteryTrendCard chart and daily view labels`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `efcd86b`. `BatteryTrendCard.kt`에 일간 차트용 `isUnmeasured = !screenTimeMeasured || currentScore < 0` 조건이 추가되어 미측정 placeholder를 표시하며, 하단 라벨은 음수 점수를 `미측정`으로 표시한다.
2. `:app:testDebugUnitTest`는 성공했고 XML 결과 합계는 **63/0/0**이다. 이번 커밋에 관련 테스트 파일 변경은 없으며, `app/src/test`에서 `DailyStockChartView`, `BatteryTrendCard`, Compose UI 미측정 렌더링 또는 `SmartDevicePoseDetector` callback freshness를 직접 검증하는 테스트는 확인되지 않았다. 따라서 QA 보고서의 실제 화면 렌더링 `PASS`는 제출된 자동화 테스트 결과로 뒷받침되지 않는다.
3. 현재 trend card에는 일간 이외에 주간/월간 보기 모드가 있다. 이 두 모드는 휴식/운동 완료 횟수만으로 `70 + rests * 15 + miniGames * 25` 방식의 30–100 점수를 만들어 그린다. 실제 화면 사용시간/측정 상태와 연결되지 않으므로 미측정이거나 기록이 없더라도 그 값은 눈 배터리 점수처럼 노출된다. 이번 `isUnmeasured` 처리는 DailyStockChartView 범위라 주간/월간 누출은 해결되지 않았다.
4. `MainScreen.kt`의 Goal/AI Coach 및 relay 제안은 미측정 sentinel 점수를 백분율 또는 점수 기반 추천으로 사용할 수 있다. `WellnessHome.kt` 상단 점수/상태는 미측정 처리 경로가 있지만 게이지는 -1을 0으로 clamp하며 danger legend가 강조될 수 있다.
5. 이전 커밋에서 확인된 proximity `maximumRange` 값 상한 검증은 유지된다. 실제 detector boundary 검증 테스트는 추가되지 않았다.
6. 최신 Debug APK 설치 및 MainActivity 실행을 시작했으나 이번 UI dump에서 점수/추세 영역을 확인하지 못했다. 센서 freshness, posture/actual-viewing accuracy, battery/heat는 별도 승인된 절차로 측정하지 않았으며 **NOT RUN**이다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 일간 차트 미측정 표시 | **PASS (코드 경로 한정)** — 미측정 placeholder와 하단 `미측정` 라벨 확인. 실제 Compose 렌더링 자동 테스트는 없음 |
| 2 | 주간/월간 추세의 데이터 타당성 | **FAIL** — 휴식/운동 기록으로 임의 점수를 만들고, 실제 측정 여부와 무관하게 배터리 추세처럼 표시 |
| 3 | 나머지 UI 소비처 | **FAIL** — MainScreen AI/Goal/relay와 Wellness gauge/legend가 미측정값을 점수처럼 소비할 수 있음 |
| 4 | 단위 테스트 | **PARTIAL PASS** — 전체 JVM 63/0/0. 이번 변경에 UI/sensor 경계 자동 테스트 추가 없음 |
| 5 | proximity bounds | **PASS (이전 구현의 코드 범위)** — 상한 검사 후 freshness 갱신. 직접 callback 테스트는 없음 |
| 6 | 실기기/실제 정확도/전력 | **PARTIAL PASS / NOT RUN** — Debug 설치 및 Activity 실행. 차트 렌더링, sensor freshness, viewing/posture accuracy, battery/heat는 미실시 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 측정되지 않은 구간을 배터리 점수처럼 표시하지 않고, 실제 데이터 근거가 없는 그래프/추천을 제거해 사용자 신뢰를 보호한다.
* **상세 요구사항 (What):**
  - 기능 1: 일간·주간·월간 모든 trend 기간에서 실제 측정 여부를 표현한다. 활동 기록 횟수만으로 배터리 점수를 생성하지 않는다. 근거 데이터가 없으면 해당 구간을 `미측정`으로 표시하거나 값이 있는 구간만 차트에 표시한다.
  - 기능 2: MainScreen Goal, AI Coach, relay 추천 및 Wellness gauge/legend에서 `UNMEASURED`를 독립 상태로 처리한다. 미측정이면 점수 백분율, 위험 등급, 달성 여부, 점수 기반 메시지를 노출하지 않는다.
  - 기능 3: 일간·주간·월간의 측정/미측정 혼합 구간, 전체 미측정, 빈 데이터와 1개 데이터 렌더링을 테스트한다.
  - 기능 4: 센서 이벤트 정상/비정상 payload 및 timestamp별 freshness 동작 테스트를 추가한다.
* **예외 처리 및 엣지 케이스:** 현재만 미측정, 과거 구간만 미측정, 전 구간 미측정, 기록은 있으나 UsageStats 실패, 그래프 데이터 0/1개를 구별한다. 정상 측정값을 임의 placeholder 점수로 바꾸지 않는다.
* **Google Play 정책 체크포인트:** 권한 및 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 과장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 모든 기간과 활성 화면에서 미측정이 점수·백분율·위험 등급·목표 성공·가짜 추세로 표시되지 않는다. 주간/월간 값은 명확히 정의된 실측/계산 데이터 출처를 사용하고 휴식/운동 횟수만으로 배터리 점수를 만들지 않는다. 주요 미측정·측정 UI 및 sensor freshness 경계 테스트가 자동화되고 전체 JVM 테스트가 통과한다. 실기기 accuracy/battery는 승인된 별도 절차가 없으면 **NOT RUN**으로 보고한다.

## 4. PM 판정

`efcd86b`는 일간 차트에서 미측정 placeholder와 하단 라벨을 추가한 점은 수용한다. 그러나 주간/월간 차트의 임의 점수 생성, MainScreen 및 Wellness 소비처의 미측정 처리는 여전히 남아 있다. 63개 JVM 테스트는 통과했지만 이번 커밋에 추가된 UI 경계 테스트는 없고, QA의 렌더링 PASS 주장은 독립 재현되지 않았다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
