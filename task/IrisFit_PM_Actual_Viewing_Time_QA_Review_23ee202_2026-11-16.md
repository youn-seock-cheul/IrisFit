# IrisFit 실제 시청시간 추정 — PM 15차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `23ee202` (`fix(ui-consumers): handle unmeasured state in AI relay theme recommendation and Wellness gauge/legend UI`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 검증 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 확인: **63 tests, 0 failures/errors, 0 skipped**

## 1. 검증 증거와 한계

1. 저장소 HEAD `23ee202`. AI relay 추천은 `!screenTimeMeasured || score < 0`에서 점수 기반 위험 추천과 score Toast를 피하고 별도 일반 휴식 주제를 만든다. 해당 코드 개선은 수용한다. 다만 “맑은 눈 유지” 문구는 미측정 시에도 긍정 상태처럼 읽힐 수 있어, 더 중립적인 데이터 비의존 문구가 바람직하다.
2. Wellness gauge/범례는 `isGaugeMeasured`가 false이면 progress 0, 중립 `statusColor`, 범례 모두 회색/비강조로 처리한다. 상단에 미측정 텍스트도 표시된다. 코드를 통해 확인했으나 Compose UI 렌더링 테스트는 추가되지 않았다.
3. GoalAchievementCard는 미측정 시 잔량을 `미측정`으로 표시하고 목표 달성 판단을 차단한다. AiCoachTab은 점수 음수 또는 UsageStats 권한 미허용 시 점수 대신 미측정 문구를 표시한다. 이 변경들도 소스 조건 확인 범위에서 수용한다.
4. Proximity `maximumRange` finite/positive 및 `values[0] <= maximumRange` 경계검사는 freshness 갱신 전에 실행된다. 실제 callback/freshness boundary test는 확인되지 않았다.
5. JVM 테스트를 실행했고 XML 합계는 **63 tests, 0 failures/errors, 0 skipped**다. 이번 커밋의 변경 파일에는 테스트 소스가 없다. 테스트 성공은 새 relay/Wellness UI를 렌더링하거나 sensor freshness를 검증했다는 뜻이 아니다.
6. 실기기 UI, relay 동작, Wellness gauge 렌더링, sensor freshness, viewing/posture accuracy 및 battery/heat는 이번 PM 검토에서 실행하지 않아 **NOT RUN**이다. 임계값과 hardware 시험 프로토콜도 아직 승인되지 않았다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | AI relay 미측정 동작 | **PASS (코드 범위)** — 미측정 시 score 기반 위험 분기/점수 Toast 대신 일반 주제 사용. 표시 문구는 더 중립적으로 정리 권장 |
| 2 | Wellness gauge/legend 미측정 상태 | **PASS (코드 범위)** — 중립 색상/비강조 및 0 progress 처리, 상단 미측정 문구 확인. Compose 렌더 테스트 없음 |
| 3 | Goal/AiCoach 미측정 처리 | **PASS (코드 범위)** — 앞선 `f05cb53` guard 유지 확인. UI 테스트 없음 |
| 4 | sensor freshness boundary | **PARTIAL PASS** — proximity 상한 코드가 freshness 이전에 확인됨. callback 자동 테스트 없음 |
| 5 | 자동 검증 | **PARTIAL PASS** — JVM 전체 63/0/0. 이번 변경의 UI 분기나 detector callback 경계를 직접 확인하는 테스트 없음 |
| 6 | 실기기/정확도/전력 | **NOT RUN** — 별도 승인된 절차/기준에 따른 시험 미실시 |

## 3. 남은 개발 및 QA 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 미측정 상태에서 위험 또는 긍정적인 눈 상태를 측정 결과처럼 암시하지 않고, 안전한 상태 표시가 실제 렌더링/분기에서도 유지됨을 입증한다.
* **상세 요구사항 (What):**
  - 기능 1: relay 미측정 fallback의 “맑은 눈 유지” 문구를 score/eye condition을 단정하지 않는 중립적 휴식 권장 주제로 검토한다. 미측정이면 점수 기반 경고/최적화 추천 및 score Toast가 생성되지 않아야 한다.
  - 기능 2: Wellness 미측정 gauge/legend가 중립색과 명시적 미측정 문구를 함께 표시하는 UI 테스트를 추가한다. measured 위험/보통/최상 상태의 기존 강조는 보존한다.
  - 기능 3: GoalAchievementCard, AiCoachTab 및 relay 미측정 분기 테스트를 추가한다. `screenTimeMeasured=false`와 음수 점수의 독립 조합도 검증한다.
  - 기능 4: SmartDevicePoseDetector의 정상 proximity 경계값, 범위 초과, malformed/non-finite payload, 잘못된 timestamp 및 unknown type가 freshness에 미치는 결과를 테스트한다.
* **예외 처리 및 엣지 케이스:** 측정 flag와 score가 불일치하는 입력, 권한 거부/query error, 센서 등록 실패/중지 후 callback, proximity maximumRange 경계와 초과 값을 처리한다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 미측정 상태에서 UI나 relay에 백분율·위험 등급·목표 성공·점수 근거의 긍정 상태 주장이 나타나지 않는다. measured 상태의 기존 표현은 회귀 없이 유지된다. UI 및 sensor freshness 경계 테스트가 자동화되고 전체 JVM 테스트가 통과한다. 실기기 정확도/전력 결과는 별도 승인 절차 전까지 **NOT RUN**으로 남긴다.

## 4. PM 판정

`23ee202`는 relay 추천의 -1 위험 처리/score Toast 누출을 막고 Wellness gauge/범례의 중립 미측정 경로를 추가했다. Goal/AiCoach guard와 함께 코드 수준 개선은 수용한다. 그러나 relay fallback 문구가 눈 상태를 긍정적으로 암시하지 않는지 사용자 문구 검토가 필요하고, 이번 변경에 UI/sensor 경계 테스트가 없다. JVM **63/0/0**은 확인했으나 UI 렌더링이나 hardware 정확도를 입증하지 않는다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
