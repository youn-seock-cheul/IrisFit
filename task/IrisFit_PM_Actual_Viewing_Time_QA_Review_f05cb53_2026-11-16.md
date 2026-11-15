# IrisFit 실제 시청시간 추정 — PM 14차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `f05cb53` (`fix(ui-consumers): enforce unmeasured state check in GoalAchievementCard and AiCoachTab UI`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD `f05cb53`. `GoalAchievementCard`는 score가 measured이고 0 이상일 때만 백분율을 표시하며, Goal 1 달성도 `screenTimeMeasured && score >= 85`일 때만 true가 된다. `AiCoachTab` battery row도 권한 및 0 이상 score 확인 후 점수/상태를 보여주고 나머지는 미측정 문구를 표시한다. 이 코드 변경은 해당 두 위치에 한정해 수용한다.
2. 해당 UI 조건을 직접 검증하는 Compose/UI 테스트는 `app/src/test`에 없다. 이번 commit의 변경 파일에도 test source가 포함되지 않았다. 전체 JVM 63/0/0은 통과했지만 QA 보고서의 두 화면 렌더링 PASS를 자동 테스트 증거로 뒷받침하지는 않는다.
3. MainScreen relay 추천은 별도 소비처다. 미측정 sentinel `-1`은 `score < 65`를 만족해 “눈 피로 위험” 추천을 만들고 Toast에 `-1%`가 노출될 수 있다. Goal 및 AI Coach 수정으로 이 경로까지 해결되지는 않았다.
4. `WellnessHome.kt` 상단은 미측정 점수/상태 문구를 표시하지만 gauge는 `-1`을 0으로 clamp하며 danger legend가 활성 강조될 수 있다. 따라서 미측정 상태가 위험 측정값처럼 보일 수 있다.
5. 이전 PM 검토에서 확인한 주간/월간 trend의 activity-count 임의 수식은 제거된 상태이며 과거 측정 데이터가 없는 기간은 placeholder로 취급된다. 실제 historical measurement source가 구현된 것은 아니다.
6. 센서 proximity 상한 guard는 이전 코드에 있으며 freshness 갱신 전 `values[0] <= maximumRange`를 검사한다. 실제 callback/freshness boundary test는 여전히 없다.
7. Gradle XML 결과는 **63 tests, 0 failures/errors, 0 skipped**. 실기기 UI, sensor freshness accuracy, viewing/posture accuracy 및 battery/heat 검증은 이번 PM 검토에서 실행하지 않았으며 **NOT RUN**이다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | GoalAchievementCard 미측정 처리 | **PASS (코드 범위)** — 미측정 점수/달성 여부 guard 확인. Compose 렌더 테스트 없음 |
| 2 | AiCoachTab 미측정 처리 | **PASS (코드 범위)** — 음수 score 미측정 문구 처리 확인. UI 테스트 없음 |
| 3 | 모든 active score consumer | **FAIL** — relay 추천이 -1을 위험 점수로 처리하고 `-1%`를 노출할 수 있음 |
| 4 | Wellness gauge/legend | **FAIL** — 미측정 score를 0으로 clamp해 gauge/위험 강조를 표시할 수 있음 |
| 5 | 자동 검증 | **PARTIAL PASS** — 전체 JVM 63/0/0; 이번 변경의 UI 분기나 detector callback 검증 없음 |
| 6 | 센서 상한 | **PASS (정적 코드 범위)** — maxRange 상한 확인 뒤 freshness 갱신; 직접 callback test 없음 |
| 7 | 실기기 정확도/전력 | **NOT RUN** — 별도 승인된 실험 프로토콜/임계값 없음 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 모든 활성 점수 consumer가 동일한 `UNMEASURED` 의미를 지키도록 해 미측정을 위험 점수, 목표 성취, AI 추천으로 오인시키지 않는다.
* **상세 요구사항 (What):**
  - 기능 1: relay AI 주제 추천과 Toast에서 측정 여부 및 score 유효성을 확인한다. 미측정이면 점수 기반 위험/최적 추천을 생성하지 않고 점수 백분율을 표시하지 않는다.
  - 기능 2: Wellness gauge는 미측정일 때 0%처럼 그리지 않고 중립 placeholder를 사용한다. 4-tier legend는 측정 상태가 확인된 점수에서만 강조한다.
  - 기능 3: MainScreen, WellnessHome, BatteryTrendCard 및 기타 score consumers를 다시 탐색해 일관된 측정-state 분기를 적용한다.
  - 기능 4: Goal/AiCoach/relay/Wellness/trend 각 measured/unmeasured UI 분기와 sensor callback freshness 경계를 자동 테스트한다.
* **예외 처리 및 엣지 케이스:** `screenTimeMeasured=false`인데 score가 0 이상인 입력, measured=true인데 score가 음수인 입력, UsageStats 권한 거부/query error, chart 과거 데이터 없음, `-1` sentinel 및 사용자 설정 relay 문구를 구분한다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. 앱 사용시간/자세를 실제 시선이나 실제 시청시간으로 과장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 미측정 상태에서 어떤 화면/추천에서도 백분율, 위험 등급, 목표 성공, 점수 기반 추천이 생성되지 않는다. Wellness gauge/legend는 미측정을 중립 상태로 표시한다. 각 핵심 UI 분기 및 detector boundary가 자동 테스트로 입증되고 전체 JVM 테스트가 통과한다. 정확도/배터리는 승인된 프로토콜 전까지 **NOT RUN**으로 보고한다.

## 4. PM 판정

`f05cb53`의 GoalAchievementCard 및 AiCoachTab 미측정 guard는 코드 범위에서 수용한다. 그러나 relay는 `-1`을 위험 점수로 처리할 수 있고 Wellness gauge/legend도 미측정 상태를 위험처럼 시각화할 수 있어 전체 소비처 요구사항은 미완료다. 전체 JVM **63/0/0**은 확인했으나 이번 커밋의 UI 변경은 자동 테스트로 입증되지 않았다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
