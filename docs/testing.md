# EyeOverlay 검증 구성

## 사용자 지정 검증 시간 원칙 — 2026-11-01

고정 5분 제한 대신, 오래 걸리는 검증이 필요하면 예상 시간·목적·미실행 시 한계를 먼저 알려 사용자가 진행 여부를 판단하게 한다. 기본은 관련 단위 검사와 짧은 화면 확인이며 장시간 자연 알림·절전 시험은 자동 시작하지 않는다. 아래 자연 시간 경과 명령은 재현용 과거 기록이며 현재 실행 계획이 아니다. 장시간 검증은 사용자가 별도로 요청할 때만 진행한다. 짧은 시험을 자연 시간 경과·장기 신뢰성 통과로 표시하지 않는다.

## 30초 안내 세션 검증 — 최신 구현

단위 18개(신규 10+기존 8), debug UI 4개(신규 세션 2+기존 타이머 2, 합계 39.708초), 전체 R8 release 외부 UI 1개(53.607초) 통과. UI는 실제 30초 안내를 실행한다. 단위 검사는 가짜 시계로 대기 없이 검증한다. [상세 결과](GUIDED_SESSION_IMPLEMENTATION_2026-11-01.md).

```
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.myapplication.GuidedSessionUiTest,com.example.myapplication.WellnessHomeTest
gradlew.bat :release-validation:connectedProductionCheckAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.eyeoverlay.releasevalidation.GuidedSessionSmokeTest --init-script gradle/release-validation.init.gradle --no-configuration-cache
```

두 UI 검사는 임시 USB 화면 유지 설정을 저장·복원하며 잠금을 우회하지 않는다. 새 release 검사는 앱 내부 참조 없이 사용자 확인·프로세스 재시작 후 기록 유지·삭제를 확인한다. 아래 채널 UI 3개 결과는 이전 실행 기록이며 이번 release 1개와 합산해 이번에 4개를 실행했다고 표시하지 않는다.

## 이전 구성 분석

- 기존 앱은 Compose UI, JUnit4 단위 테스트 8개와 Compose instrumented UI 테스트 2개를 사용한다.
- 알림 수신/종료 계약 AndroidTest 2개는 만료 상태를 주입한다. 자연 20분 시험과 구분한다.
- 앱에 DI·Mockk/Mockito·Robolectric·Room 테스트·자동 스크린샷 비교/coverage 도구는 구성되어 있지 않다.
- 이번 P0 검증은 실제 release를 외부에서 조작하므로 앱 구조/DI를 바꾸거나 Fake를 주입하지 않는다. 기존 스택을 유지한다.
- 앱 내 release instrumentation은 공유 런타임의 Trace 클래스 부재로 러너 시작이 실패했다. Gradle 도구의 성공 표시 대신 XML의 실제 testcase와 실패 수를 확인한다.

## 전체 R8 release 외부 UI 테스트

release-validation은 com.android.test의 self-instrumenting 모듈이다. 테스트 러너·UI Automator가 별도 APK/프로세스에 있으므로 앱에 테스트용 keep 규칙이나 테스트 런타임을 넣지 않는다. productionCheck는 앱의 release를 대상으로 한다.

세 사용자 흐름 모두 채널 보완 후 전체 R8 release에서 통과했다. 2026-11-01 14:11:14 KST, XML tests=3, failures/errors/skipped=0. 기본 실행 대상은 ProductionUiTest이며 장시간 자연 시험은 별도 명시해야 실행된다.

1. 알림 권한 철회 → 화면 내 타이머 → 가로/세로 전환 → 홈 복귀 → 종료.
2. 일반 백그라운드 프로세스 종료 시 예약 보존 ↔ force-stop으로 OS 예약 취소 → 앱 재실행 시 원래 시각으로 복원 → 새 예약 시작. OS 예약 시각도 직접 대조한다.

3. 개별 휴식 채널 차단 → 앱 경고 → 채널 설정 직접 연결 → 허용 복구 → 원래 채널 설정 복원. 이 테스트의 시스템 설정 선택자는 한국어 기기를 대상으로 한다.

잠금 해제한 실제 기기에서 실행하며 검증 중 휴대폰을 조작하지 않는다. 잠금/PIN을 우회하지 않는다. 테스트는 EyeOverlay의 로컬 예약/기록을 삭제하고 앱을 종료할 수 있으며, 회전 설정을 저장·복원한다.

```
gradlew.bat :release-validation:connectedProductionCheckAndroidTest --init-script gradle/release-validation.init.gradle --no-configuration-cache
```

명시적인 로컬 init은 기존 개발용 인증서만 적용한다. 업로드 키/production 서명 설정은 바꾸지 않는다. 이 init을 bundleRelease나 스토어 업로드에 사용하지 않는다. 테스트는 앱 private 데이터나 내부 클래스를 참조하지 않으며 20분 알림을 가속하지 않는다.

결과: release-validation/build/outputs/androidTest-results/connected 하위 XML 및 HTML 보고서. 기대하는 세 테스트 이름과 tests=3, failures=0, errors=0, skipped=0을 확인한다. 기존 2개 통과 XML을 신규 3개 실행 결과로 사용하지 않는다. 러너 실패용 testRunFailed 1개를 기능 테스트 1개로 집계하지 않는다.

## 실제 20분 알림 종료 시험 (명시적 실행)

NaturalReminderStopTest는 시계·예약 값을 바꾸지 않고 최소 19분 이상의 실제 남은 예약을 확인한 뒤 홈에서 최대 40분 동안 알림을 관찰한다. 관측 간격은 15초이므로 관측 시각을 정확한 OS 전달 시각으로 표시하지 않는다. 자기 앱 알림 카드의 ‘알림 종료’만 눌러 알림/OS 예약 제거와 앱 초기 타이머를 검증한다. 알림 권한과 USB 화면 유지 값을 저장·복원하며 기기 잠금을 우회하지 않는다. 일반 3개 테스트와 별도로 tests=1 XML 및 P0_NATURAL 로그를 확인한다.

```
gradlew.bat :release-validation:connectedProductionCheckAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.eyeoverlay.releasevalidation.NaturalReminderStopTest --init-script gradle/release-validation.init.gradle --no-configuration-cache
```

2026-11-01 14:37경 사용자가 알림 시험의 우선순위를 재검토해 중단했다. 해당 실행은 통과/실패 판정 없이 중단으로 기록한다. 러너/앱 종료·OS 예약/알림 제거·USB 화면 유지 0 및 회전 0/0 복원을 확인했다. 추가 장시간 알림 시험은 보류한다. 하드 중단 시 @After가 실행되지 않을 수 있으므로 시작 전 저장한 natural-original-settings.txt와 실제 기기 설정을 대조하고 복원해야 한다.

## 기존 검증과 일반 출시 산출물

```
gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest
gradlew.bat :app:bundleRelease :app:verifyReleaseBoundary :app:verifyReleaseNativeAlignment :app:verifyReleaseBundleNativeAlignment :app:lintRelease
```

기본 산출물 명령에는 위 테스트 init을 추가하지 않는다. 앱 내 비최적화 QA가 필요하다면 .scratch/p0_device_20261101/p0-release-tests.init.gradle을 명시할 수 있지만 그 결과는 minified release 결과로 표시하지 않는다.

자연 알림·재부팅·심층 절전·실제 16KB 환경과 다른 제조사/OS는 별도 조건의 실기기 시험이다. [수동 시험표](P0_MANUAL_TEST_MATRIX.md)와 [실제 결과](P0_DEVICE_VALIDATION_2026-11-01.md)를 따른다. 저장한 release-timer.png는 실제 화면 기록이며 golden screenshot 비교 시험이 아니다.

## 공식 근거

- [별도 self-instrumenting 모듈 공식 예제](https://github.com/android/performance-samples/blob/main/MacrobenchmarkSample/macrobenchmark/build.gradle.kts)
- [AndroidX Test 안정 버전](https://developer.android.com/jetpack/androidx/releases/test)
