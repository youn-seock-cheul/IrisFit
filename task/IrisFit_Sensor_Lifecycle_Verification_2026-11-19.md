# IrisFit 센서 리스너 생명주기 검증 — 2026-11-19

- 상태: **대상 코드 수정·자동 검증 완료 / 별도 accuracy·battery QA 미실시 / Release Hold 유지**
- 목적: `SmartDevicePoseDetector`의 실제 센서 등록 및 callback 수신, 모든 센서 등록 실패, stop/restart 경계에서 이전 세션의 늦은 callback 차단을 확인한다.
- 작업 시작 시 기존 worktree는 UI·화면·현지화 등 다수의 미커밋/미추적 변경을 포함했다. 본 작업은 관련 소스·테스트와 본 보고서만 변경했으며 다른 변경은 보존한다.

## 구현

- `SmartDevicePoseDetector`에서 Android `SensorManager` 접근을 `PoseSensorGateway` 경계 뒤로 분리했다. production gateway는 가용 센서를 조회하고 리스너별 callback을 등록하며 성공한 리스너만 보관·해제한다.
- 센서 부재, `registerListener`의 false 반환 및 등록 중 예외를 명시적으로 처리하고 기록한다. 일부 센서가 등록되지 않아도 나머지 등록 성공 센서의 유효 샘플만 사용할 수 있다. 등록된 센서가 없으면 freshness는 false다.
- 각 `startListening()` 호출은 generation ID를 만든다. stop 또는 새 세션이 시작된 뒤 이전 generation에서 도착한 sample/unreliable callback은 무시하여 새 세션의 freshness나 분류 상태를 오염시키지 않는다.
- `stopListening()`은 generation을 무효화하고 freshness를 지운 뒤 등록된 Android listener 전체를 해제한다.

## 검증

- **PASS — JVM 29개:** `.\gradlew.bat :app:testDebugUnitTest --tests com.example.myapplication.SmartDevicePoseDetectorTest --tests com.example.myapplication.ViewingTimeEvaluatorTest --console=plain -q`
  - `SmartDevicePoseDetectorTest`: **8 passed / 0 failed**
  - `ViewingTimeEvaluatorTest`: **21 passed / 0 failed**
  - 등록 전부 실패 시 unavailable/not fresh와 stop 정리, 이전 세션의 늦은 sample이 새 세션 freshness를 갱신하지 않는 경우를 검증했다. 기존 payload·timestamp·unreliable accuracy 회귀 테스트도 통과했다.
- **PASS — 실제 기기 SensorManager integration:** Galaxy S23 (`SM-S916N`, Android 16/API 36)에 Debug/AndroidTest APK 설치 후 `SensorLifecycleInstrumentedTest#physicalSensorRegistrationProducesFreshSamplesAndStopInvalidates` **OK (1 test)**. 실제 기기에서 하나 이상의 센서 등록, 5초 이내의 검증된 실제 callback 수신, stop 후 freshness 무효화를 확인했다.
- **PASS — 빌드:** `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:compileReleaseKotlin`; Release boundary 검사도 통과했다. Release APK 서명·배포를 수행한 것은 아니다.
- 첫 JVM 실행에서 기존 `ViewingTimeEvaluatorTest` 8개가 테스트 JVM의 미모의 `android.util.Log` 호출로 실패했다. Android gateway의 로깅을 테스트 환경에서도 예외 없이 처리한 뒤 위 대상 29개를 재실행해 모두 통과했다.

## 범위와 잔여 사항

- 등록 전부 실패는 fake gateway로 재현했고, 실제 기기에서는 정상적인 하드웨어 등록/callback 경로를 확인했다. 물리 기기의 센서를 실제로 제거하거나 Android 등록 실패를 강제한 것은 아니다.
- 지연 callback 차단은 fake gateway의 이전 generation callback으로 검증했다. 하드웨어/OS가 전달한 실제 지연 callback을 실기기에서 강제 재현하지는 않았다.
- 센서 freshness의 자세 분류 정확도, 실제 시청시간 정확도, 배터리·발열·전력 및 다른 지원 기기의 결과는 **NOT RUN**이다.
- 전체 unit/instrumentation suite, `ea23d44` 변경의 별도 QA, Play 배포도 이번 범위가 아니다. **Release Hold 유지.**
