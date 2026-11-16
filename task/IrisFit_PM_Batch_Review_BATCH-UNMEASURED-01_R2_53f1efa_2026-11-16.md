# IrisFit 미측정 UI·센서 freshness — 배치 재검토 R2

- **배치 ID:** `BATCH-UNMEASURED-01`
- **검토 범위:** base `67c4dd4` → final `53f1efa`
- **최종 커밋:** `53f1efa` (`test: add payload validation unit tests and guard SystemClock in updatePose`)
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 성공; XML 13 suites에서 **68 tests, 0 failures, 0 errors, 0 skipped**
- **PM 판정:** **PARTIAL PASS — UI/계산기 및 일부 payload 검증 수용; monotonic clock와 오류 처리 보완 후 재검수. Release Hold 유지.**

## QA 제출 대비 독립 결과

| 항목 | 판정 | 근거 및 한계 |
|---|---|---|
| 미측정 relay 문구 및 Wellness 아이콘 | **PASS — 소스 확인** | 이전 batch의 일반 휴식 Toast와 `UNMEASURED` 중립 아이콘 변경은 유지된다. 이번 commit의 변경사항은 아니다. |
| 초기 freshness 및 calculator | **PASS — JVM 테스트** | 생성 직후 `isFresh=false`, calculator의 `-1`/`UNMEASURED` 결과 테스트가 존재한다. |
| NaN payload 거부 | **PASS — 로직/테스트 확인** | NaN payload가 `false`로 거부되고 fresh timestamp가 설정되지 않는 핵심 처리 경로를 테스트한다. |
| proximity maximumRange 초과 | **PASS — 로직/테스트 확인** | 상한 초과 proximity 값이 거부되고 fresh timestamp가 설정되지 않는 경로를 테스트한다. |
| 유효 payload freshness | **PARTIAL** | 가속도계 샘플을 통한 정상 경로 테스트가 추가됐다. 그러나 테스트 timestamp는 `System.currentTimeMillis()` 기반으로 만들고, Android sensor event의 timestamp는 elapsed-realtime 기반 monotonic nanos다. 현재 production code도 `SystemClock` 실패 시 wall clock으로 fallback하여 서로 다른 clock domain을 혼합할 수 있다. 테스트는 fallback 환경에서 통과할 수 있으나 실제 SensorEvent 시간 계약을 입증하지 않는다. |
| 기타 sensor 경계 | **NOT TESTED** | Infinity/empty/짧은 배열/unknown type/invalid·future timestamp, stop 후 callback, unreliable accuracy 이후 freshness 무효화는 추가 테스트에서 확인되지 않았다. |
| 초기화 오류 및 로깅 | **PARTIAL / 수정 필요** | `sensorManager`와 Handler 실패는 `Exception` catch 후 로그 및 null fallback으로 바뀌어, 조용한 실패는 줄었다. 그러나 `safeLogE`/`safeLogW`는 `Throwable`을 삼킨다. 더 중요한 점은 `isFresh`, sample validation, `updatePose`가 elapsedRealtime 실패 시 `System.currentTimeMillis()`로 fallback하는 것으로, sensor timestamp와 동일한 monotonic clock domain을 보장하지 않는다. 이 fallback은 제거하고 clock failure 시 fail-closed해야 한다. |
| 전체 JVM 회귀 | **PASS — 범위 한정** | Gradle 성공과 XML 68/0/0을 독립 확인했다. 이것은 실기기 sensor callback, UI 렌더링 또는 실제 viewing accuracy 검증이 아니다. |
| 실기기 및 전력 | **NOT RUN** | 이번 PM 검토에서 실기기 callback, 정확도, 배터리 및 발열 시험을 실행하지 않았다. |

QA가 보고한 3개 payload 테스트 추가와 68개 테스트 결과는 확인했다. 다만 유효 payload 테스트의 wall-clock timestamp는 실제 Android SensorEvent timestamp와 의미가 다르며, 이를 monotonic clock 기반으로 고치는 것이 핵심 재작업이다. 이번 판정은 센서 핵심 계약이 완전히 검증됐다는 뜻이 아니다.

## 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** SensorEvent timestamp와 freshness 계산이 Android의 동일한 monotonic clock domain을 사용하도록 해 잘못된 미래/과거 판정 및 freshness 거짓 양성을 방지한다.
* **상세 요구사항 (What):**
  - 기능 1: freshness와 sample validation에서 `SystemClock.elapsedRealtime()` 오류를 `System.currentTimeMillis()`로 대체하지 않는다. clock을 읽을 수 없으면 freshness를 승인하지 않고 오류를 명시적으로 처리한다.
  - 기능 2: 테스트 가능한 clock abstraction/주입을 사용해 현재 monotonic time 및 sensor timestamp를 결정적으로 구성한다. 단위 테스트 timestamp는 epoch wall time이 아닌 elapsed-realtime domain으로 만든다.
  - 기능 3: valid callback만 freshness timestamp를 갱신하고 NaN/Infinity/empty/short/unknown type/out-of-range/invalid timestamp는 갱신하지 않는 테스트를 보완한다.
  - 기능 4: stop 이후 이벤트와 unreliable accuracy 상태에서 freshness가 무효화되는지 테스트한다. `safeLog*`가 `Throwable`을 삼키는 구조를 제거하거나, 예상 가능한 로깅 실패만 명시적이고 제한적으로 처리한다.
* **예외 처리 및 엣지 케이스:** clock 조회 실패, 등록된 센서 0개, 등록 실패, Handler/Looper 준비 불가, 오래된/미래 timestamp, stop 직후 전달된 callback을 포함한다. 어떤 실패도 listening/freshness 성공처럼 보고하지 않는다.
* **Google Play 정책 체크포인트:** 새 권한 또는 백그라운드 센서 수집을 추가하지 않는다. 자세/UsageStats를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** 운영과 테스트 모두 elapsed-realtime clock domain을 일관되게 사용한다. clock 오류는 freshness false로 안전하게 처리된다. 모든 유효/무효 callback 경계의 timestamp 갱신 여부가 자동 테스트로 입증되고 전체 JVM 테스트가 통과한다. 실기기 viewing accuracy 및 battery/heat는 승인된 protocol 전까지 `NOT RUN`이다.

## PM 판정

미측정 UI·calculator 수정과 NaN/proximity-invalid 및 초기 freshness 테스트는 범위 한정 수용한다. 그러나 유효 payload 테스트가 wall-clock timestamp에 의존하고 production fallback이 clock domain을 혼합할 수 있어 센서 freshness 검증은 아직 완료가 아니다. **BATCH-UNMEASURED-01 PARTIAL PASS**, 재작업 후 QA 재검수, **Release Hold 유지**. 실기기 정확도/전력은 NOT RUN이다.
