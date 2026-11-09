# Instrumentation 시작 실패·ANR 관련 진단 — 2026-11-01

**개요: APP_DEFECT (테스트 산출물/런타임 구성).** 테스트 러너에서 필요한 클래스 부재가 직접 확인됐다. 일반 production 앱의 ANR 결함으로 확정할 증거는 부족하다.

## 1. Trace validity and context

- 프로세스: com.eyeoverlay.app. PID 19901, 22426, 32327. 테스트 러너 실행 조건. 당시 foreground UI 상태는 미확인.
- 11:42:46.564 PID 19901: AndroidJUnitRunner.onCreate에서 androidx.tracing.Trace NoClassDefFoundError. 11:43:03 시작 ANR 관련 AM 기록.
- 11:47:36.719 PID 22426: matching release 테스트도 같은 Trace 오류. 11:47:51 시작 지연/디버그 앱 ANR 건너뛰기 기록.
- Trace에 국한한 로컬 테스트 keep 시도 후 12:03:05.354 PID 32327: instrumentation 스레드에서 kotlin.LazyKt 부재. OutputDirCalculator → FileTestStorage → AndroidJUnitRunner.onStart.
- ANR 전체 스레드 덤프: 확보 못함. dumpsys activity lastanr에서 일치 보고서 없음. 직접 증거는 fatal stack과 AM 로그이며 ANR 순간 main의 blocking stack이 아니다.
- 사용자 체감 ANR/정확한 capture delay: 미확인. 일반 앱 사용 중 UI freeze라고 주장하지 않는다.

## 2. System health assessment

CPU/PSI/iowait/page fault 측정 없음. 시스템 자원 과부하나 외부 병목으로 분류할 근거 없음. 확인된 구성 결함 범위에서만 APP_DEFECT로 분류한다.

## 3. Root cause analysis

첫 실패는 최적화한 target과 debug 테스트 APK 조합이었다. 정식 release test variant도 공유 의존성의 최적화된 API와 러너의 기대 API가 일치하지 않아 클래스 해석이 실패했다. 첫 스택은 main의 runner.onCreate이며 이후 실패는 instrumentation 스레드다. deadlock·동기 I/O·잠금 소유자·CPU 독점 증거는 없다.

## 4. Remediation

1. 해당 테스트 러너 문제 대응에서는 production R8을 비활성화하지 않음. 앱에 광범위 keep이나 전역 오류 무시를 추가하지 않음. error_prone_annotations는 테스트 빌드 누락 참조 해결용 androidTest 의존성에만 추가.
2. 로컬 QA init에서만 개발용 인증서와 최적화 비활성화 적용. 알림 계약 2개가 12:08:40 KST 정상 실행 완료(XML failures/errors/skipped=0).
3. 이 결과를 최종 minified release 검증으로 대체하지 않는다. 최적화된 테스트 classpath 호환성과 잠금 해제 후 화면 복귀는 남은 항목으로 유지. 이후 별도 단독 시작 시험에서 production IllegalAccessError를 발견·수정했으며 아래에 기록.
4. 재현 시 app PID별 crash만 수집하고 실제 ANR 발생 시 즉시 유효한 전체 스레드 덤프와 시스템 상태를 확보한 뒤 원인을 재평가한다.

## 5. 후속 production 앱 단독 시작 시험 — 별도 결함

12:12:20.303 PID 11384의 main에서 IllegalAccessError를 재현했다. kotlin.collections.CollectionsKt__CollectionsKt가 kotlin.collections.b로, 하위 CollectionsKt__IteratorsJVMKt가 uc로 배치되며 상위 클래스 접근이 실패했다. 호출 경로에 kotlin.jvm.internal.ClassReference 초기화가 있다. 이는 instrumentation 없이 발생한 실제 출시 차단 결함이다. 앞선 테스트 러너 오류와 구분한다.

app/build.gradle.kts의 실험적 packageScope를 제거하고 R8 전체 앱 최적화를 적용했다. AGP 9.5.0 유지. 수정 후 같은 기기에서 최적화된 APK 사본을 개발용 인증서로 설치해 COLD 시작(Status ok, TotalTime 419ms), 3초 뒤 PID 12575 생존 및 해당 PID crash 버퍼에 fatal 없음 확인. UI 조작·장시간·다른 OS 검증까지 통과한 것은 아니다.

수정 전/후 증거: production-partial-optimization-crash.txt, production-launch.txt, production-smoke.txt. [전체 앱 최적화 전환 공식 가이드](https://developer.android.com/topic/performance/app-optimization/optimize-specified-packages).

## 6. 최종 검증 — 러너 격리와 실제 release UI

13:08:56.565 PID 18782에서도 앱 부착 runner.onCreate의 androidx.tracing.Trace 부재가 재현됐다. XML은 testRunFailed 1개·failures=1이며 기능 테스트가 실행되지 않았다. 시작 지연 실행을 자체 앱 force-stop으로 종료했고 Gradle 도구의 성공 표시를 통과로 사용하지 않았다.

별도 self-instrumenting release-validation 모듈에서 러너/라이브러리를 격리했다. 앱 내부 클래스·private 데이터·만료값 주입 없이 전체 R8 release를 외부 UI로 검증한다. 앱에 테스트용 광범위 keep이나 최적화 비활성화를 적용하지 않았다.

최종 13:25:21 KST XML: tests=2, failures=0, errors=0, skipped=0. 권한 철회/회전/홈 복귀와 실제 일반 종료/force-stop 전후 OS 예약 시각 보존·복원을 확인했다. 앱 부착 방식 전체의 호환성이 해결됐다는 뜻은 아니며, 최종 검증 경로를 외부 UI로 분리한 것이다.

증거: .scratch/p0_device_20261101/final-minified-tests.xml, final-minified-crash.txt, final-blackbox-tests.xml. [실행 방법](testing.md).

기존 진단 증거: .scratch/p0_device_20261101/release-matched-crash.txt, release-last-anr.txt, test-dependencies.txt.
