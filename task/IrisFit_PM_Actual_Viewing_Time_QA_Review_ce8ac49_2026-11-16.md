# IrisFit 실제 시청시간 추정 — PM 16차 검토

- **검토 기준 commit:** `ce8ac49` (`fix(MainScreen): refine AI relay fallback recommendation text to neutral rest prompt on unmeasured state`)
- **PM 판정:** **PARTIAL PASS — 잔여 UI 검증 재개방, Release Hold 유지**
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 성공. XML 결과 합계 **63 tests, 0 failures, 0 errors, 0 skipped**.

## 검토 결과

| 항목 | 판정 | PM 확인 |
|---|---|---|
| 미측정 AI relay 추천 제목 | **PASS — 소스 확인** | `!isMeasured || score < 0`이면 눈 상태를 단정하지 않는 “눈 건강을 위한 30초 휴식 릴레이” 제목을 사용한다. 점수 기반 danger/positive 제목과 퍼센트 토스트는 이 분기에서 발생하지 않는다. |
| 미측정 AI relay Toast 문구 | **PARTIAL** | 제목은 중립화됐지만 토스트가 여전히 “맞춤 릴레이 주제”라고 표현한다. 해당 경로는 점수/상태와 무관한 일반 추천이므로 개인화된 상태 기반 추천으로 오해되지 않게 바꿔야 한다. |
| Wellness 게이지/범례 | **PARTIAL — 소스 확인** | UNMEASURED는 중립 회색, score `-1`은 미측정 라벨, 게이지 progress는 0, 네 범례는 회색/비강조다. 단, 미측정 상태의 `mascotRes`가 `ic_mascot_happy`로 선택된다. 측정되지 않은 상태를 긍정 컨디션처럼 보이게 할 수 있어 중립 자산/표시 여부를 검토해야 한다. |
| Goal/AiCoach 및 센서 가드 | **기존 코드 유지** | 이번 커밋에서 해당 경로의 변경은 없다. 실제 sensor callback/freshness 경계 테스트는 확인되지 않았다. |
| 자동화/실기기 | **PARTIAL / NOT RUN** | 63개 JVM 테스트는 통과. 이번 커밋은 MainScreen 문구만 변경했고 UI 분기 자동 테스트는 없다. Compose 실제 렌더링, 기기 UI, viewing/posture accuracy 및 battery/heat는 이번 검토에서 실행하지 않았다. |

QA가 보고한 문구 개선은 relay 제목의 소스 변경으로 확인된다. 그러나 QA의 화면 렌더링 PASS는 자동 UI 테스트나 이번 PM 실기기 실행으로 뒷받침되지 않으므로 코드 경로 확인으로만 인정한다. 사용자가 본 표현의 정확성 및 실제 렌더링은 별도 검증 대상이다.

## 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 미측정일 때 일반 권장 사항을 상태 기반 개인화로 오인하거나, 행복한 컨디션 상태를 나타내는 시각 요소로 오해하지 않게 한다.
* **상세 요구사항 (What):**
  - 기능 1: AI relay 미측정 분기의 토스트에서 “맞춤” 등 측정값 기반 개인화를 암시하는 표현을 일반 휴식 권장 안내로 바꾼다. 측정된 상태의 토스트 동작은 유지한다.
  - 기능 2: Wellness 미측정 화면에서 `ic_mascot_happy`가 표시되는지 확인하고, 상태를 암시하지 않는 중립 표시 또는 미측정 안내를 사용한다.
  - 기능 3: 미측정/점수 음수 조합의 relay, Wellness, Goal 및 AiCoach UI 분기를 자동화 테스트로 검증한다. 측정된 상태의 기존 표시도 회귀 검증한다.
  - 기능 4: SmartDevicePoseDetector의 payload/range 검증 실패 및 freshness 갱신 경계 테스트를 추가한다.
* **예외 처리 및 엣지 케이스:** `screenTimeMeasured=false`와 nonnegative score가 함께 들어오는 불일치 입력, permission 거부, query 실패, 음수 점수 및 센서 malformed/out-of-range callback을 검증한다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 미측정 UI/토스트가 실제 눈 상태나 개인화를 암시하지 않는다. 미측정과 정상 측정 케이스의 UI/relay 테스트 및 sensor freshness 경계 테스트가 자동화되고 통과한다. 실기기 정확도·배터리 시험은 승인된 절차 전까지 NOT RUN으로 관리한다.

## 최종 판정

`ce8ac49`는 relay 제목을 중립화한 점을 수용한다. 하지만 같은 경로의 Toast가 여전히 “맞춤”을 주장하고 Wellness 미측정 상태가 happy mascot을 사용한다. UI 렌더링 및 sensor 경계 테스트도 없다. 따라서 **PARTIAL PASS**, **Release Hold 유지**, 내부 테스트/베타/Play 배포 승인 없음. viewing/posture accuracy와 battery/heat는 **NOT RUN**이다.
