# IrisFit 미측정 UI·센서 freshness — 배치 재검토 R3

- **배치 ID:** `BATCH-UNMEASURED-01`
- **검토 범위:** base `53f1efa` → final `ea40a32`
- **최종 커밋:** `ea40a32` (`fix(clock-domain): enforce strict monotonic clock domain for pose detector freshness and fail-closed on clock failure`)
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 성공; XML 13 suites에서 **68 tests, 0 failures, 0 errors, 0 skipped**
- **PM 판정:** **PARTIAL PASS — monotonic clock 수정 수용; 나머지 callback/lifecycle 경계 테스트 및 실기기 검증 대기, Release Hold 유지**

## 독립 검토 결과

| 항목 | 판정 | 근거 및 한계 |
|---|---|---|
| Monotonic clock domain | **PASS — 소스 확인** | `System.currentTimeMillis()` fallback은 제거됐고, 생산 경로의 clock provider는 `SystemClock.elapsedRealtime()`만 사용한다. SensorEvent의 nanos timestamp는 ms로 변환해 동일 elapsed-realtime 도메인의 현재값과 비교한다. |
| Clock failure fail-closed | **PASS — 코드 경로 확인** | clock provider가 `<= 0`을 반환하면 샘플을 거부하고 freshness timestamp를 갱신하지 않는다. `isFresh`도 현재 clock 값이 유효하지 않으면 false다. 다만 `Throwable`이 발생하는 clock failure를 주입해 거부를 검증하는 전용 테스트는 이번 변경에서 추가되지 않았다. |
| 결정적 clock 테스트 | **PASS — 범위 한정** | 테스트에서 `monotonicClockMsProvider`를 고정값으로 주입하고 SensorEvent timestamp도 같은 시간 도메인/단위로 생성한다. 유효 샘플 및 기존 NaN/proximity-invalid 테스트가 이 provider를 이용한다. |
| 초기화 오류 | **부분 수용** | SENSOR_SERVICE 및 MainLooper Handler 접근 시 `Exception`을 기록하고 null로 유지한다. 넓은 `Throwable` 삼킴은 로깅 보조 함수와 clock provider에 남아 있어 필요성/범위를 재검토할 수 있지만, 이전 wall-clock 대체 동작은 없어졌다. |
| 추가 sensor 경계 | **미완료** | clock=0/예외, 미래/0 timestamp, Infinity/empty/short payload, unknown type, stop 이후 callback, unreliable accuracy 이후 freshness 무효화에 대한 전용 테스트는 확인되지 않았다. |
| 자동 검증 | **PASS — 전체 JVM, 변경 직접 검증은 일부** | Gradle 성공과 XML 68/0/0을 독립 확인했다. 68개 전체 통과는 UI 렌더링 또는 위 미검증 detector 경계의 자동 검증을 의미하지 않는다. |
| 실기기/정확도/전력 | **NOT RUN** | 이번 PM 검토에서 센서 실 callback, 실제 viewing/posture accuracy, battery/heat 시험을 실행하지 않았다. 허용 기준과 시험 절차도 별도 승인 전이다. |

QA가 주장한 elapsed-realtime 단일화 및 clock 0 fail-closed 동작은 source inspection으로 확인했다. 독립 JVM 테스트를 재실행해 68/0/0도 확인했다. 다만 clock 오류 주입과 callback lifecycle 경계가 테스트되지 않아 QA의 sensor 판정을 배치 전체 PASS로 승격하지 않는다.

## 남은 개발·QA 지시

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** monotonic clock 수정이 정상·오류·lifecycle 경계에서 일관되게 작동함을 증명하고, 검증된 callback 외에는 freshness를 만들지 않도록 한다.
* **상세 요구사항 (What):**
  - 기능 1: clock provider가 0/음수 또는 예외를 반환할 때 sample 거부 및 `isFresh == false`를 검증한다.
  - 기능 2: timestamp 0, 미래 timestamp 및 정상 elapsed-realtime timestamp를 각각 검증한다.
  - 기능 3: Infinity/empty/짧은 배열/unknown type 및 proximity maximumRange의 경계값(같음/초과)을 검증한다.
  - 기능 4: 정상 이벤트 후 stop, listener 등록 불가, unreliable accuracy callback에서 freshness가 무효화되는 lifecycle 동작을 검증한다.
  - 기능 5: broad `Throwable` catch는 필요한 플랫폼 경계만 좁혀 사용하고, 예기치 않은 실패를 성공/미측정 fallback으로 위장하지 않는지 점검한다.
* **예외 처리 및 엣지 케이스:** clock provider가 throw/0/음수를 반환하는 경우 fail-closed; listener가 중단된 뒤 전달되는 callback은 무시; timestamp와 now는 같은 monotonic 기준이어야 한다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** clock 오류, payload 및 lifecycle 경계의 freshness 결과가 자동 테스트로 입증되고 전체 JVM 테스트가 통과한다. UI 렌더링 및 실기기 정확도·전력 검증은 각 별도 테스트/승인으로 관리하고 미실시 시 `NOT RUN`으로 남긴다.

## PM 판정

`ea40a32`는 wall-clock fallback을 제거하고 elapsed-realtime 단일 clock 및 fail-closed 경로를 구현했다. deterministic provider를 이용한 3개 payload 테스트가 있고 전체 JVM suite **68/0/0** 통과를 확인했으므로 해당 수정은 수용한다. 그러나 clock failure 주입과 추가 invalid/lifecycle callback 테스트가 없어 센서 freshness 검증 전체는 미완료다. **BATCH-UNMEASURED-01 R3 PARTIAL PASS**, 추가 테스트 및 QA 재검수 필요, **Release Hold 유지**. 실기기 viewing accuracy 및 battery/heat는 **NOT RUN**이다.
