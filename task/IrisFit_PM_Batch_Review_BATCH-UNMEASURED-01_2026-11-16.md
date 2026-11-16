# IrisFit 미측정 UI·센서 freshness — 배치 PM 검토

- **배치 ID:** `BATCH-UNMEASURED-01`
- **제출 기준:** QA 보고서의 base `ce8ac49` → final `67c4dd4`
- **최종 커밋 확인:** `67c4dd4` (`test: add detector initial freshness and calculator unmeasured unit tests`)
- **검토 시 worktree:** 기존 미추적 cadence 제안서 1개가 있었으며 보존함. 그 외 변경 없음.
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 성공. Gradle XML 13 suites에서 **65 tests, 0 failures, 0 errors, 0 skipped** 확인.
- **PM 판정:** **PARTIAL PASS — 부분 코드 개선 수용, sensor 오류 처리 및 경계 테스트 재작업; Release Hold 유지**

## QA 인계 독립 검토

| # | 항목 | 검토 결과 |
|---|---|---|
| 1 | relay 미측정 Toast | **PASS — 소스 확인**. 미측정 분기에서 “맞춤” 표현을 제거했다. |
| 2 | Wellness 미측정 아이콘 | **PASS — 소스 확인**. `UNMEASURED`는 `ic_nav_eye_comfort`를 사용하고 happy mascot과 구분된다. |
| 3 | 미측정 calculator 결과 | **PASS — JVM 단위 테스트**. `screenTimeMeasured=false`에서 점수 `-1`, `UNMEASURED` 상태를 확인하는 테스트가 있다. |
| 4 | 초기 sensor freshness | **LIMITED PASS**. 생성 직후 `isListening=false`와 `isFresh=false`를 확인하는 테스트는 유효하다. 그러나 리스너 등록/이벤트 callback을 거치지 않으므로 센서 입력의 허용/거부나 timestamp 갱신 경계를 검증하지 않는다. |
| 5 | sensor 이벤트 경계 검증 | **NOT TESTED**. malformed/non-finite, unknown type, proximity maximumRange 초과, invalid timestamp, valid callback 및 stop/unreliable 상태 후 freshness를 검사하는 테스트는 확인되지 않았다. |
| 6 | 센서 자원 초기화 오류 처리 | **REWORK REQUIRED**. `SmartDevicePoseDetector`가 `getSystemService` 및 `Handler` 생성에서 `Throwable`을 포착하고 `null`로 조용히 대체한다. 예외를 숨긴 채 listening/등록 상태와 실제 기능 불가를 구분하지 않아 실패 원인이 관측되지 않는다. 특히 Handler가 없는 경우 freshness polling이 시작되지 않는다. |
| 7 | 전체 JVM 검증 | **PASS — 해당 범위만**. Gradle 실행 성공, XML 합계 65/0/0. 테스트 통과는 UI 렌더링 또는 실제 sensor callback 동작의 증거가 아니다. |
| 8 | 실기기 정확도/전력 | **NOT RUN (이번 PM 재실행 없음)**. QA가 충전으로 배터리 검증을 하지 않았다고 보고했으나 이번 검토에서 기기 상태/소모를 독립 측정하지 않았다. |

QA가 보고한 65개 테스트 통과는 독립 실행과 XML 합산으로 확인했다. 단, 보고서의 “센서 초기 Freshness” PASS는 **시작 전 초기 상태 확인에 한정**한다. 배치의 센서 guard 자체 및 freshness callback 경계까지 통합 PASS라고 확대 해석할 수 없다. 또한 base→final 범위에는 앞선 UI 카피/아이콘 보완과 현재 센서 처리 및 테스트 변경이 함께 포함되어 있으므로, 이 배치 판정은 최종 코드 범위에 대한 판정이다.

## 개발 재작업 지시

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 센서 초기화/스케줄링 실패를 정상 동작처럼 숨기지 않고, 오직 검증된 이벤트만 freshness를 갱신한다는 핵심 신뢰성 기준을 자동 테스트로 입증한다.
* **상세 요구사항 (What):**
  - 기능 1: `SmartDevicePoseDetector`의 `Throwable` catch 및 조용한 null fallback을 검토한다. 예상 가능한 오류만 구체적으로 처리하고, 오류가 발생하면 프로젝트 표준 로그/오류 상태를 제공하며 detector를 사용 불가 또는 freshness false로 명확히 유지한다.
  - 기능 2: sensor event 검증을 테스트 가능한 경계로 분리하거나 기존 구조에 맞춰 테스트한다. 유효 accelerometer/gyro/light/proximity 이벤트만 갱신을 허용하고, 미지원 type과 잘못된 데이터는 갱신하지 않아야 한다.
  - 기능 3: proximity 값 `maximumRange` 이하/초과, NaN/Infinity/empty/짧은 payload, timestamp 0/미래 timestamp, 정상 callback, listener 중지 후 callback 및 unreliable accuracy 이후 freshness를 검증한다.
  - 기능 4: 초기 freshness 테스트는 유지하되 이름/설명/보고서에서 이를 callback 경계 검증과 구분한다. 미측정 UI 테스트와 센서 테스트 결과도 각각 분리해서 보고한다.
* **예외 처리 및 엣지 케이스:** sensor service 부재, sensor registration 실패, Handler/Looper 준비 불가, callback 경쟁 상태, stop 이후 callback을 고려한다. 예외를 삼키거나 sensor가 없는 상태를 성공으로 보고하지 않는다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 의도한 정상 이벤트만 freshness 갱신, 모든 잘못된/불안정 이벤트는 freshness false 또는 기존 timestamp 불변임을 자동화 테스트로 입증한다. 초기화 실패는 로그/명시적 상태로 진단 가능하다. 미측정 UI와 calculator 자동 테스트를 보존하고 전체 JVM 테스트가 통과한다. 실기기 accuracy/battery/heat는 승인된 protocol 전까지 `NOT RUN`으로 남긴다.

## 최종 판정

relay/Wellness 미측정 표현 변경, calculator의 미측정 단위 테스트 및 초기 detector 상태 테스트는 범위 한정 수용한다. 그러나 센서 callback freshness 경계는 미검증이고, 새 `Throwable` catch가 초기화 실패를 조용히 감춘다. 따라서 **BATCH-UNMEASURED-01은 PARTIAL PASS**이며 센서 처리/테스트 재작업 후 QA 재검수가 필요하다. **Release Hold 유지**; 내부 테스트/베타/Play 배포 승인 없음.
