# IrisFit 실제 시청시간 추정 — PM 13차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `44c1606` (`fix(trend-ui): remove synthetic calculation from weekly and monthly trend tabs and enforce proximity maximumRange check`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `44c1606`. 변경된 주간/월간 trend 코드에서 임의 점수 산식은 제거됐다. 과거 날짜와 주는 현재 데이터 source가 아직 연결되지 않았으므로 `-1`로 처리되고, UI에는 `-` 및 중립 색상으로 표현된다. 현재 날짜/이번 주만 `eyeBatteryStats.eyeHealthScore`를 사용한다.
2. 공통 `isUnmeasured = !screenTimeMeasured || eyeHealthScore < 0` 조건으로 현재 상태가 미측정이면 일간/주간/월간 차트 본문이 placeholder로 대체된다. 이는 현재 상태 미측정 UX를 개선하지만, 현재 상태 measured일 때 과거의 개별 일자/주간 값은 실제 과거 측정 기록이 아니라 임의 placeholder `-1`이다. 따라서 과거 추세 데이터는 아직 제공되지 않는다.
3. proximity의 payload가 finite/양수 maximumRange를 만족하고 `values[0] <= maximumRange`일 때만 freshness가 갱신된다. 코드 변경은 확인했으나 `SmartDevicePoseDetector` 실제 callback/freshness 경계 테스트는 찾지 못했다.
4. `MainScreen.kt`의 Goal/AI Coach 및 relay 제안은 미측정 sentinel 점수를 백분율/점수 기반 추천으로 사용할 수 있다. `WellnessHome.kt` 상단 점수/상태는 미측정 처리 경로가 있으나 gauge는 -1을 0으로 clamp하며 danger legend가 강조될 수 있다.
5. `.\gradlew.bat :app:testDebugUnitTest`가 성공했고 Gradle XML 합계는 **63 tests, 0 failures/errors, 0 skipped**다. 이 commit은 테스트 파일을 수정하지 않았으며 `app/src/test`에 trend UI/Compose renderer 또는 sensor callback 경계 테스트가 없다. 따라서 QA 보고서의 화면별 렌더링 PASS는 자동화 테스트의 독립 증거가 아니다.
6. 이번 검토에서 trend tab UI를 직접 재현하지 않았다. 정답 라벨 기반 viewing/posture accuracy, 실기기 sensor freshness 및 battery/heat 비교는 승인된 protocol과 acceptance threshold가 없어 **NOT RUN**이다. 충전 여부만으로 배터리 비교 결과를 판정하지 않는다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 주간/월간 임의 점수 산식 제거 | **PASS (코드 범위)** — 활동 횟수 기반 synthetic score 산식은 제거됨 |
| 2 | 데이터 없는 과거 bucket 표시 | **PARTIAL PASS** — 현재 코드가 과거 날짜/주를 미측정 처리. 과거 측정값을 저장·조회하는 기능은 아직 없음 |
| 3 | 전체 trend 미측정 UX | **PASS (코드 경로 한정)** — 전역 미측정이면 placeholder 표시. Compose 렌더링 테스트는 없음 |
| 4 | 기타 UI 소비처 미측정 처리 | **FAIL** — MainScreen AI/Goal/relay 및 Wellness gauge/legend가 미측정값을 정상 점수처럼 소비할 수 있음 |
| 5 | sensor freshness 자동 검증 | **PARTIAL PASS** — 상한 guard는 코드에서 확인. 정상/비정상 callback 경계 테스트 없음 |
| 6 | 자동 테스트 | **PARTIAL PASS** — JVM 전체 63/0/0. 이번 변경에 UI/sensor 경계 테스트 없음 |
| 7 | 실기기 정확도/전력 | **NOT RUN** — 별도 승인된 시험 절차/기준이 없음 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 미측정 상태를 score consumer에서 명시적으로 처리하고, 현재와 과거 trend의 데이터 의미를 분리해 사용자가 실측으로 오인하지 않게 한다.
* **상세 요구사항 (What):**
  - 기능 1: MainScreen Goal, AI Coach, relay 추천 및 Wellness gauge/legend에서 `UNMEASURED`를 독립 상태로 처리한다. 미측정이면 백분율, 위험 등급, 달성 여부, 점수 기반 메시지를 만들지 않는다.
  - 기능 2: 실제 과거 score를 보존/조회하는 데이터 source가 승인·구현되지 않았다면 주간/월간은 현재처럼 미측정 placeholder로 유지한다. 향후 실제 기록을 추가할 때만 기간별 값을 표시하며 임의 산식으로 대체하지 않는다.
  - 기능 3: sensor callback 처리의 정상 proximity(범위 내/경계), 범위 초과, malformed/NaN/Infinity, 잘못된 timestamp 및 unknown type가 freshness를 갱신하는지 자동 테스트한다.
  - 기능 4: 일/주/월 trend와 핵심 score consumer의 measured/unmeasured 렌더링 테스트를 추가한다.
* **예외 처리 및 엣지 케이스:** 현재 데이터만 measured, 과거 이력 없음, 일부 기간만 기록됨, 전체 미측정, UsageStats query error, 빈 데이터 및 단일 point를 구분한다. 센서 미등록/리스너 중지/비정상 callback은 유효 측정으로 바꾸지 않는다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. UsageStats 및 자세 신호를 실제 시선/실제 시청시간으로 과장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 모든 활성 소비처에서 미측정이 점수·백분율·위험 등급·목표 성공·추천으로 표시되지 않는다. 과거 추세에는 실제 저장된 측정 데이터만 노출하며 데이터가 없으면 미측정으로 표시한다. UI/sensor boundary tests가 추가되고 전체 JVM 테스트가 통과한다. viewing accuracy/battery는 별도 승인된 protocol 전까지 **NOT RUN**으로 보고한다.

## 4. PM 판정

`44c1606`은 주간/월간의 activity-count 점수 공식을 제거했고, 데이터가 없는 과거 기간은 placeholder로 표시한다. 이 제한된 코드 개선은 수용한다. 다만 실제 과거 측정 데이터는 제공되지 않으며, MainScreen 및 Wellness consumer의 미측정 처리와 UI/sensor 자동 경계 테스트가 남아 있다. **63 JVM 테스트 통과만으로 화면 렌더링이나 실제 정확도가 입증되지 않는다. PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
