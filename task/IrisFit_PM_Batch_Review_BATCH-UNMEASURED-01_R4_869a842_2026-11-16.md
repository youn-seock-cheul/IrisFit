# IrisFit 미측정 UI·센서 freshness — 배치 재검토 R4

- **배치 ID:** `BATCH-UNMEASURED-01`
- **검토 범위:** base `ea40a32` → final `869a842`
- **최종 커밋:** `869a842` (`test: add clock failure fail-closed and future timestamp rejection unit tests`)
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 성공; XML 13 suites에서 **70 tests, 0 failures, 0 errors, 0 skipped**
- **PM 판정:** **PARTIAL PASS — 0 clock/future timestamp 경로 수용; 실제 clock 예외 및 남은 callback/lifecycle 경계 테스트 필요; Release Hold 유지**

## 독립 검토 결과

| 항목 | 판정 | 근거 및 한계 |
|---|---|---|
| Clock provider가 `0L` 반환 | **PASS — JVM 테스트** | `testPoseDetectorClockFailure_ReturnsFailClosedNotFresh`에서 provider=0일 때 샘플을 거부하고 `isFresh=false`를 검증한다. 이는 fail-closed sentinel 경로 검증이다. |
| 미래 timestamp 거부 | **PASS — JVM 테스트** | 결정적 monotonic now보다 5초 미래인 sensor timestamp가 거부되고 fresh가 되지 않는 테스트가 있다. |
| 실제 예외 throw 시 fail-closed | **NOT TESTED** | 신규 clock-failure 테스트는 provider가 `0L`을 돌려줄 뿐 예외를 throw하지 않는다. 기본 provider 내부는 `Throwable`을 잡아 0을 반환하지만, 이 catch 경로 자체의 동작은 회귀 테스트하지 않는다. |
| Monotonic clock domain | **PASS — 소스 확인** | production provider는 elapsed-realtime만 사용하고 wall-clock fallback이 없다. |
| 기존 payload 경계 | **부분 검증** | NaN, proximity 초과, valid accelerometer, future timestamp는 확인된다. Infinity, empty/short array, unknown type, proximity 경계 동등값, timestamp 0/음수는 미확인이다. |
| Listener lifecycle/accuracy | **NOT TESTED** | 정상 sample 후 stop, listener 0개/등록 실패, unreliable accuracy 뒤 freshness 무효화 테스트가 없다. 테스트 helper가 내부 `listening`/등록 수를 변경할 뿐 실제 listener 등록/callback lifecycle을 구동하지 않는다. |
| 자동 검증 범위 | **PASS — 전체 JVM, 범위 한정** | 70/0/0을 Gradle 및 XML에서 재확인. UI 렌더링, hardware callback 또는 실기기 정확도 증거는 아니다. |
| 실기기·배터리 | **NOT RUN** | 이번 검토에서 독립 측정하지 않았다. QA 보고의 USB 충전 언급을 이번 세션의 측정 사실로 간주하지 않는다. |

QA가 보고한 70개 테스트와 미래 timestamp 거부는 독립 확인했다. “clock failure” 테스트는 0 반환만 커버하므로 실제 throw 경로까지 검증됐다고 확대해서는 안 된다. 또한 70개 통과는 전체 sensor lifecycle boundary 완료를 뜻하지 않는다.

## 남은 개발·QA 지시

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** clock과 sensor lifecycle의 실패 경로에서도 측정 freshness가 거짓으로 켜지지 않는다는 점을 테스트로 보장한다.
* **상세 요구사항 (What):**
  - 기능 1: clock provider가 실제 예외를 throw하는 테스트를 추가한다. production clock adapter가 예외를 0/fail-closed로 처리하는 경로와 `processSensorSample` 거부/`isFresh=false`를 검증한다.
  - 기능 2: timestamp 0 및 과거/음수 경계, Infinity, empty/short payload, unknown sensor type, proximity 값이 maximumRange와 정확히 같은 경계를 추가한다.
  - 기능 3: 유효 이벤트 이후 `stopListening`, 미등록 상태 및 `SENSOR_STATUS_UNRELIABLE` 처리 뒤 freshness가 false가 되는 lifecycle 테스트를 추가한다.
  - 기능 4: 단위 테스트에서 내부 상태 helper를 이용한 검증과 실제 listener lifecycle 검증을 구분하고, 테스트 이름/인계에 정확한 검증 범위를 적는다.
* **예외 처리 및 엣지 케이스:** clock exception은 wall clock으로 대체하지 않고 freshness를 거부한다. stop/unreliable 이후 늦게 도착한 이벤트는 freshness를 다시 활성화하지 않는다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** clock throw/0, timestamp, payload, range, stop/unreliable 경계의 상태 및 timestamp 갱신 여부가 자동 테스트로 입증되고 전체 JVM 테스트가 통과한다. 실기기 정확도·전력은 승인된 protocol 전까지 `NOT RUN`이다.

## PM 판정

`869a842`에서 0 clock fail-closed sentinel과 미래 timestamp 거부 테스트를 수용하고 **70/0/0**을 확인했다. 하지만 provider가 실제 예외를 throw하는 경로와 remaining payload/lifecycle boundary는 검증되지 않았다. 따라서 `BATCH-UNMEASURED-01`은 **PARTIAL PASS**이며 후속 테스트 후 QA 재검수가 필요하다. **Release Hold 유지**; 실기기 viewing accuracy 및 battery/heat는 **NOT RUN**이다.
