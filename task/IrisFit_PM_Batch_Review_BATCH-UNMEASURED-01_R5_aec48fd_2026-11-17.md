# IrisFit 미측정 UI·센서 freshness — 배치 재검토 R5 및 PM 제출 승인서

- **배치 ID:** `BATCH-UNMEASURED-01`
- **검토 기준:** PM 지시 `IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R4_869a842_2026-11-16.md`
- **검토 대상 commit:** `4ed09ba` 및 `aec48fd`; 현재 HEAD `aec48fd`
- **중간 범위 주의:** HEAD에는 센서 테스트 외에 `ea23d44`의 beta MVP/UI 변경도 포함되어 있다. 해당 기능/리팩터링은 이번 sensor 지시 검토 범위에 포함하지 않으며 별도 QA 대상이다.
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 성공; XML 15 suites에서 **82 tests, 0 failures, 0 errors, 0 skipped**
- **QA 결과:** **PARTIAL PASS — R4 센서 테스트 요구사항 대부분 충족, 미등록 센서/중지 후 지연 callback 검증 잔여**
- **PM 결정:** **조건부 승인 — 완료한 자동화 테스트 범위만 인수해 PM에 제출. 배치 최종 완료 및 내부 테스트/출시 승인은 보류.**

## 1. R4 개발 지시사항 대비 검수

| 요구사항 | 검수 증거 | 판정 |
|---|---|---|
| Clock provider가 실제 예외를 throw하는 경우 fail-closed | `clockThrowingException_returnsFailClosedNotFresh`에서 주입한 `RuntimeException`으로 샘플 거부 및 `isFresh=false` 확인. `ViewingTimeEvaluatorTest`에도 같은 유형의 테스트가 추가됨 | **PASS — 경로 확인** |
| timestamp 0/음수/미래 거부 | 신규 0/음수 테스트 및 기존 결정적 monotonic clock 미래 timestamp 테스트 확인 | **PASS** |
| Infinity, empty/short payload 및 unknown type 거부 | 신규 테스트에서 각 입력의 거부와 `isFresh=false` 확인 | **PASS** |
| proximity `value == maximumRange` 경계 허용 | `proximityExactMaxRangeBoundary_acceptedAndValid`에서 정확히 같은 값이 승인되고 fresh 상태가 됨을 확인 | **PASS** |
| `SENSOR_STATUS_UNRELIABLE` 및 stop 이후 freshness 무효화 | 유효 샘플 뒤 unreliable accuracy 콜백과 `stopListening()` 호출 시 `isFresh=false` 검사 | **PASS — 상태 무효화 범위** |
| 미등록/등록 실패 상태 | `setUp()`이 매 테스트에서 `setTestRegisteredSensorsCount(1)`로 등록 상태를 인위적으로 설정. 센서 0개 또는 등록 실패 상황에 대한 테스트는 없음 | **미완료** |
| stop 후 늦게 도착한 이벤트 | stop 후 `isFresh=false`는 확인하나, 뒤늦은 `processSensorSample`/callback이 거부되고 fresh 상태를 복구하지 않는다는 검증은 없음 | **미완료** |
| 실제 Android listener 등록/해제 lifecycle | 테스트는 `ContextWrapper`에서 `getSystemService`를 null로 반환하고 `setTestRegisteredSensorsCount` 및 `processSensorSample`을 사용한다. 즉 detector 상태 전이와 callback 처리 핵심 로직을 JVM에서 검증하지만 실제 `SensorManager.registerListener`/`unregisterListener` 또는 `onSensorChanged(SensorEvent)` 경로를 구동하지 않음 | **미실시 — QA 보고서의 “실제 listener lifecycle” 주장은 과도함** |
| 전체 JVM 회귀 | 독립 Gradle 실행 성공, XML **82/0/0** | **PASS — 테스트 범위에 한정** |
| 실기기/정확도/배터리 | 이번 PM 검토에서 실행하지 않음 | **NOT RUN** |

## 2. PM 독립 판단

신규 테스트 파일의 여섯 테스트를 확인했고, Gradle 테스트 결과 및 XML 합계를 독립 재실행했다. clock exception, timestamp, payload, unknown type, proximity 상한 동등값, unreliable/stop에 대한 상태 검증은 R4 결함 범위를 실질적으로 보완한다.

다만 QA 제출물은 이들을 “실제 센서 리스너 생명주기” 테스트라고 표현했으나 테스트에는 실제 `SensorManager`/`SensorEvent`가 연결되어 있지 않다. helper가 등록 상태를 강제로 설정한다. 또한 미등록/registration failure와 stop 후 지연 callback 거부는 확인하지 않았다. 따라서 QA의 **FINAL PASS** 및 배치 전체 완료 주장은 승인하지 않는다. 다만 R4의 자동화 보완 작업은 아래 제한과 함께 PM에 제출할 수 있다.

## 3. 📄 PM 제출 승인서 (범위 제한)

**수신:** 수석 PM / 제품 승인자  
**제목:** `BATCH-UNMEASURED-01` R4 후속 자동화 테스트 결과 승인 요청

본인은 commit `aec48fd`의 후속 검증을 독립 확인했습니다. `.\gradlew.bat :app:testDebugUnitTest` 실행 및 XML 검증 결과 **82 tests, 0 failures, 0 errors, 0 skipped**였습니다. Clock exception fail-closed, timestamp 경계, 잘못된 payload/unknown type 거부, proximity maximumRange 동등 경계 허용, unreliable accuracy 및 stop 상태 무효화 테스트를 확인했습니다.

**승인 범위:** 위 JVM 테스트 추가와 해당 코드 경로의 테스트 증거는 **조건부 승인**하여 PM 기록에 반영합니다.

**승인 제외:** 실제 Android 센서 등록/해제 및 `SensorEvent` callback lifecycle, 등록 센서 0개/등록 실패, stop 이후 지연 callback이 freshness를 복구하지 않는 동작, 실기기 viewing/posture 정확도, 배터리 및 발열은 이번 결과로 승인하지 않습니다. HEAD에 포함된 `ea23d44` beta MVP/UI 변경도 별도 QA 없이 승인하지 않습니다.

**최종 판정:** **PARTIAL PASS / 배치 최종 완료 승인 보류 / Release Hold 유지.** 내부 테스트, 베타 또는 Google Play 배포에 대한 승인이 아닙니다.

PM 후속 결정 권고: 실제 sensor registration/callback 테스트가 필요한지 제품 위험 기준에 따라 선택하되, 선택하지 않을 경우 이 검증이 JVM의 상태 전이 테스트에 한정됨을 acceptance/QA 기록에 명시하십시오. 또한 `ea23d44` 변경 범위를 별도 배치로 추적·검수하십시오.
