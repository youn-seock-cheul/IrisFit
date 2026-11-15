# IrisFit 실제 시청시간 추정 — PM 12차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `25fea7e` (`fix(BatteryTrendCard): prevent unmeasured fake score bars in weekly/monthly trend views`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `25fea7e`. Weekly(7일) chart에서 기록 없는 과거 일자는 `-` 및 중립 막대로 표현되고, 기록 있는 과거 일자는 `EyeBatteryCalculator`에 `screenTimeMinutes = 120`을 고정 전달해 계산한다. 따라서 “임의 수식 점수 제거”는 일부 개선이나 기록 있는 날짜의 값도 실제 측정 화면시간이 아니라 가정치다.
2. Monthly(4주) chart에는 기존 `70 + rests * 15 + miniGames * 25` 수식이 그대로 남아 있고, 실제 screen-time 측정값과 연결되지 않는다. QA 제출은 주간/월간 모두 해당 수식 제거를 주장하나 코드상 월간 view는 그 주장과 일치하지 않는다.
3. 현재 공통 `isUnmeasured` 조건은 현재 날짜의 `screenTimeMeasured` 및 현재 score만 확인한다. 이는 각 과거 날짜/주차 데이터의 실측 가능성을 나타내지 않으므로, 현재 측정됨을 과거 전체 구간의 실제 측정으로 해석해서는 안 된다.
4. `MainScreen.kt`의 Goal/AI Coach 및 relay 제안은 미측정 score를 백분율/점수 기반 추천으로 사용할 수 있다. `WellnessHome.kt` 상단 점수/상태는 미측정 처리 경로가 있으나 gauge는 -1을 0으로 clamp하고 danger legend를 강조할 수 있다.
5. `.\gradlew.bat :app:testDebugUnitTest` 성공 및 XML 합계 **63 tests, 0 failures/errors, 0 skipped**를 확인했다. `app/src/test`에는 weekly/monthly/monthly score source, Compose trend 렌더링 또는 sensor callback/freshness 경계 테스트가 없다. 따라서 주간/월간 UI PASS 주장은 테스트로 독립 확인되지 않았다.
6. 이번 검토에서 최신 trend tab UI를 직접 조작하지 않았으며, sensor freshness, posture/actual-viewing accuracy, battery/heat 시험도 수행하지 않았다. 해당 실기기 검증은 **NOT RUN**이다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 주간 과거 날짜 미측정 표시 | **PARTIAL PASS** — 기록 없는 날짜는 `-`; 기록 있는 날짜는 고정 120분 가정치로 점수화되어 실측이 아님 |
| 2 | 월간 차트의 임의 점수 제거 | **FAIL** — activity-count 공식이 코드에 잔존 |
| 3 | 전 기간별 measured/unmeasured 구분 | **FAIL** — 공통 조건은 현재 날짜 측정 상태만 확인; 과거 개별 bucket은 데이터 상태 없이 계산 |
| 4 | 기타 UI consumer 미측정 처리 | **FAIL** — MainScreen AI/Goal/relay 및 Wellness gauge/legend가 sentinel을 점수처럼 소비할 수 있음 |
| 5 | 자동 테스트 | **PARTIAL PASS** — JVM 전체 63/0/0. chart/UI 및 sensor callback boundary 테스트 없음 |
| 6 | 실기기/정확도/전력 | **NOT RUN** — trend UI 직접 검증, 실제 viewing/posture accuracy, sensor freshness, battery/heat 미실시 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 과거의 사용·휴식 기록을 실제 화면시간 측정값으로 오인하지 않게 하고, 데이터가 없는 기간에 눈 배터리 점수를 허위로 생성하지 않는다.
* **상세 요구사항 (What):**
  - 기능 1: 과거 일자/주차/월별 측정 score를 제공하는 실제 저장·조회 데이터 source를 확인하고 모델에 측정 가능 상태를 보존한다. 해당 데이터가 존재하지 않으면 `미측정`으로 표시한다.
  - 기능 2: Weekly에서 `screenTimeMinutes=120` 고정 주입을 제거한다. Monthly의 활동 횟수 기반 임의 수식을 제거한다. 현재 오늘의 measured flag를 과거 bucket 측정 여부로 재사용하지 않는다.
  - 기능 3: MainScreen Goal, AI Coach, relay 추천 및 Wellness gauge/legend에서 `UNMEASURED`를 독립 상태로 처리하고 점수 백분율·위험 상태·목표 성공·추천 점수를 표시하지 않는다.
  - 기능 4: 일/주/월별 measured, unmeasured, mixed, no-history 상태와 sensor payload/freshness 경계에 대한 자동 테스트를 추가한다.
* **예외 처리 및 엣지 케이스:** 현재만 측정, 과거 구간만 미측정, 일부 주간 날짜만 측정, 휴식 기록은 있지만 screen-time data가 없음, query error, 빈 데이터 및 단일 점포인트를 구분한다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. UsageStats 및 자세를 실제 시선/실제 시청시간으로 과장하지 않는다.
* **완료 조건 (Acceptance Criteria):** Weekly/Monthly score는 해당 기간의 실제 측정 data source와 측정 상태에서만 계산된다. 고정 사용시간/활동횟수 기반의 임의 점수는 없다. 측정 데이터가 없는 기간은 명시적 미측정으로 표시된다. 모든 활성 consumer 및 chart의 미측정/UI 경계가 테스트되고, sensor freshness boundary도 자동 검증된다. 실제 정확도/배터리는 별도 승인된 절차가 없으면 **NOT RUN**으로 유지한다.

## 4. PM 판정

`25fea7e`는 기록이 없는 주간 날짜에 가짜 bar 대신 중립 placeholder를 표시하는 점은 개선됐다. 하지만 기록 있는 날짜에 120분을 가정해 점수화하고 월간 view에는 activity-count 공식이 남아 있어 실제 측정 근거가 없는 점수가 계속 노출될 수 있다. QA가 주장한 수식 제거의 범위는 코드와 일치하지 않는다. 전체 JVM **63/0/0**은 확인했으나 이번 커밋에 추세 UI/sensor 경계 테스트 추가는 없다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
