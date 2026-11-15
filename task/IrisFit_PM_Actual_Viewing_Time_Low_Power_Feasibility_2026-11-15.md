# IrisFit 실제 시청시간·저전력 측정 가능성 사전 검토

- 작성일: 2026-11-15
- 역할: PM — 정적 기술 가능성 검토
- 연계 요구사항: `IRIS-REQ-SCORE-001`, `IRIS-REQ-USAGE-001`
- 상태: **예비 검토 완료 — 실기기 전력/정확도 검증 및 측정 방식 결정 대기**
- 앱 코드 변경: 없음
- Release 상태: **Release Hold**

## 1. 사용자 목표와 제약

사용자가 원하는 지표는 휴대전화의 모든 활동 시간이나 단순 화면 켜짐 시간이 아니다. **사용자가 실제로 휴대전화를 손에 들고 화면을 보는 시간**을 알려주는 것이 목표다. 책상 위에 놓인 휴대전화나 잠자는 동안 켜져 있는 화면은 시청시간으로 계산되어서는 안 된다는 예시가 제시됐다.

동시에 앱은 24시간 실행되어도 배터리 소모가 거의 없는 가벼운 앱이어야 한다. 사용자는 실제 시청시간을 추정하는 카메라/센서 접근을 원하지만, 이 전력 제약을 유지하고 싶어 한다. 숫자와 같은 값을 나타내는 보조 그래픽 방향은 선택됐으나, 실제 측정 방식은 승인되지 않았다.

## 2. 현재 저장소에서 확인된 관련 구현

1. `app/src/production/kotlin/com/example/myapplication/data/ScreenTimeManager.kt`는 `UsageStatsManager`의 여러 패키지별 `totalTimeInForeground`를 합산한다. 이는 사용자 시선이나 폰을 손에 들었는지를 측정하지 않으며, 사용자가 설명한 목표의 대체값으로 사용할 수 없다.
2. `app/src/production/kotlin/com/example/myapplication/data/SmartDevicePoseDetector.kt`는 가속도계, 자이로스코프, 근접 및 조도 센서를 사용하고 `PoseClassifier`가 기기 자세를 분류한다. 기기 자세/움직임만으로 눈이 화면을 보고 있는지 확인할 수는 없다. 배터리 소모량은 측정되지 않았다.
3. `app/src/main/java/com/example/myapplication/vision/GazeTracker.kt`는 카메라 영상을 5Hz로 분석하고 얼굴·눈 뜸 신호를 처리한다. 현재 `gazeX`, `gazeY`는 화면 중앙값으로 설정되므로 실제 시선 방향 검증은 아니다. 얼굴이 보이고 눈이 떠 있다는 사실만으로 사용자가 화면을 보고 있다고 확정할 수 없다.
4. `app/src/main/java/com/example/myapplication/service/EyeExerciseOverlayService.kt`는 개발용 카메라 실험이다. 디버그 빌드에서만 시작하고, 전면 카메라와 오버레이 권한/사용자 노출 알림을 사용하며, 화면이 꺼지면 중지되고 최대 실행 시간은 2분이다. 24시간 제품용 측정이나 배터리 지속성의 증거가 아니다.

## 3. Android 플랫폼 제약 및 기술 판단

- Android의 카메라 권한은 사용 중 권한 제약을 받는다. Android 14+에서 카메라 Foreground Service는 서비스 유형과 `FOREGROUND_SERVICE_CAMERA` 등 요구 권한을 선언해야 하며, `CAMERA`는 while-in-use 권한이다. 앱이 백그라운드인 상태에서 해당 서비스를 새로 시작할 수 없다. 따라서 앱이 보이는 동안 사용자가 직접 시작하는 설계와 foreground service 알림이 필요할 수 있다. 서비스가 백그라운드에서 계속 동작하는 것과 백그라운드에서 임의로 시작할 수 있는 것은 별개다.
- Google Play는 Android 14+ foreground service 사용 시 Play Console에 유형별 사용 목적/사용자 영향/데모 영상을 제출하도록 안내하며, `TYPE_CAMERA` 예시는 백그라운드 카메라 스트리밍이다. IrisFit의 자동 시청 추정이 해당 사용 사례에 적합한지, 정책상 허용되는지는 별도 Play 정책 검토 전 확정할 수 없다.
- 카메라를 상시 분석하면 카메라와 영상 처리기가 계속 작동하므로 “배터리 소모가 거의 없는 24시간 상시 측정”을 보장할 근거가 없다. 실제 비용은 기기/카메라/분석 주기별 측정이 필요하다.
- Android 공식 `SensorManager` 문서는 필요 없는 센서를 비활성화하지 않으면 몇 시간 내 배터리를 소모할 수 있고 화면이 꺼져도 시스템이 센서를 자동 비활성화하지 않는다고 경고한다. 따라서 자세 센서도 24시간 등록하는 것을 저전력이라고 가정할 수 없다.
- 센서만으로는 실제 눈의 주시 여부를 알 수 없다. 센서는 기기를 책상에 놓았는지/움직였는지 추정하거나 카메라 사용 구간을 줄이는 보조 신호로 평가할 수 있지만, 이를 실제 시청시간이라고 단정하면 안 된다.
- 따라서 **정확한 실제 시청시간, 상시 자동 측정, 거의 무시 가능한 배터리 소모를 동시에 보장할 수 있다고 현재 증거로 결론 내릴 수 없다.** 저전력 조건으로 제한한 하이브리드 접근도 정확도와 배터리 영향에 대한 실기기 검증 전에는 제안 수준이다.

공식 문서:
- [Foreground service types — Android Developers](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Restrictions on starting a foreground service from the background — Android Developers](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [SensorManager — Android Developers](https://developer.android.com/reference/android/hardware/SensorManager)
- [Understanding foreground service and full-screen intent requirements — Google Play Console Help](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)

## 4. 권고하는 다음 조사 범위

검토 결과, "앱이 24시간 설치/대기 상태"와 "앱이 24시간 센서/카메라로 능동 측정"은 다르다. 전자는 센서/카메라를 계속 켜지 않는 한 가능하지만, 화면을 보지 않는 구간을 자동으로 구분할 수 없다. 후자는 개인정보 고지·권한·OS 표시/제한 및 배터리 비용을 동반할 수 있다.

사용자 승인 후에만 제한된 기술 검증을 설계한다. 후보는 (A) 앱 화면이 보일 때만 센서/카메라를 잠깐 켜는 전경 세션 — 백그라운드 사용량은 놓침, (B) 사용자가 명시적으로 켜고 끄는 지속 추적 서비스 — 상시 알림과 카메라/센서의 잠재적 높은 배터리 비용, (C) 카메라 없이 자세/화면 상태로 추정 — 실제 시청 판정이 아니라 근사치다. 어떤 방식도 지금 승인되지 않았다.

검증을 진행한다면 다음을 별도 승인받고, 결과를 먼저 측정한다.

- 동일 기기/밝기/통신 상태/화면 사용 패턴에서 추적 OFF 기준선과 각 후보의 8~24시간 배터리 소모 및 발열을 비교한다.
- 사용자의 실제 행동을 수동으로 라벨링해 손에 들고 보는 상태, 책상 위, 수면/방치, 화면이 켜진 채 시선이 다른 곳, 거치대 사용을 포함한 오탐/미탐을 산출한다.
- 카메라/센서 권한, 지속 알림, OS 개인정보 표시, 강제 종료/재부팅/권한 철회 시 처리 및 Play foreground-service 선언 적합성을 검토한다.
- 합격 기준(허용 배터리 추가 소모, 허용 오탐/미탐, 지원 기기/OS, 측정 활성화 방식)은 테스트 전에 사용자가 승인한다.

## 5. 현재 판정과 미검증 항목

- 자동 실제 시청시간 측정: **NOT APPROVED**
- 저전력/24시간 배터리 영향: **NOT RUN** — 전력 측정용 실기기/테스트 절차가 없다.
- 시청 감지 정확도: **NOT RUN** — 기기/조명/사용 자세별 기준 데이터가 없다.
- Android/Play 정책 적합성: **NOT DETERMINED** — 사용 사례에 대한 구체적인 제출/정책 검토가 없다.
- 연결된 Android 테스트 기기: **없음/확인 불가** — 이번 환경에서는 `adb`를 찾을 수 없어 디바이스 목록을 확인하지 못했다.
- 카메라·센서 기반 베타 범위 변경 및 실기기 시험: **PM 승인 대기**
- 개발 착수: **금지 — 측정 방식과 제품 범위 결정 전**

`IRIS-REQ-SCORE-001`은 사용자 목표를 “실제 손에 들고 보는 시간”으로 정정하고, 기술적 구현 가능성 및 배터리 비용을 확인한 후 수치 정의를 다시 확정해야 한다. 이전의 전체 앱 전경 사용시간 합산 방향은 사용자 목표에 부합하지 않는다.
