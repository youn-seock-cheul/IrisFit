# P0 실기기 검증 — 2026-11-01

**현재 판정: 채널 보완 후 전체 R8 release 외부 UI 3개 통과. 자연 알림 종료 시험은 사용자 요청으로 중단·출시 보류.** 14:11:14 KST 실행 XML은 tests=3, failures/errors/skipped=0(63.270초)이다. Studio UI 도구의 기기 미표시와 달리 ADB 연결을 확인해 실제 Galaxy S23+에서 실행했다. 아래 이전 UI 2개와 설치 해시는 채널 보완 전 기록이다.

## 추가 진행 — 알림 채널 차단·복구

- 앱 전체 알림 권한만 확인하던 누락을 발견해 개별 화면 휴식 채널의 IMPORTANCE_NONE도 비활성으로 처리했다.
- 해당 채널만 차단된 경우 설정 버튼이 채널 설정을 직접 열도록 수정했다. 앱 전체 차단이면 앱 알림 설정을 연다.
- 첫 만료 전에 설정할 수 있도록 예약 시작 시 채널을 생성한다. 기존 채널의 사용자 설정은 유지한다.
- 차단 → 앱 경고 → 설정 버튼으로 해당 채널 진입 → 허용 복구 → 경고 해제 및 원래 채널 상태 복원 테스트를 추가했다. 외부 UI 테스트 총 3개가 이번 수정 후 실기기에서 모두 통과했다. 증거: channel-ui-three-tests.xml. 한국어 기기의 설정 스위치 설명을 사용하는 테스트다.
- AAB·release 경계·APK/AAB 정렬·기존 단위 테스트 8개·release Lint·새 테스트 APK 빌드가 성공했다. 이 결과는 채널의 실제 만료 알림 전달/종료 액션 통과가 아니다.
- 14:37경 사용자가 알림의 제품 내 중요도를 재검토해 자연 시험을 중단했다. 해당 실행은 통과/실패로 판정하지 않는다. 러너·앱 종료 후 활성 OS 예약/알림 없음, USB 화면 유지/회전 0/0/0 복원 확인. 앱은 강제 종료 상태다. 저장된 시험용 시각이 남아 있을 수 있어 앱을 예정 시각 전에 다시 열면 복원될 수 있다. 추가 알림·재부팅·절전 장시간 시험을 보류하고 현재 release와 원래 제품 구상 간 차이부터 정리한다. 제품·판매 결정은 지금 요구하지 않는다.

Galaxy S23+ 한 대의 이전 결과를 모든 기기/OS 호환성이나 Play 승인으로 확대하지 않는다.

## 이전 실기기 환경·종료 상태 — 채널 보완 전

- Galaxy S23+ (SM-S916N), Android 16 / API 36. 실제 PAGE_SIZE=4096.
- 사용자가 변경한 AGP 9.5.0 유지. compileSdk/targetSdk 36.
- 최종 앱은 전체 R8을 적용한 release. 개발용 인증서로 APK 사본만 테스트 서명했으며 업로드 키/production 서명 설정은 변경하지 않았다.
- 기본 설정 AAB 재빌드·release 경계·APK/AAB 네이티브 정렬 검사 성공. 기존 단위 테스트 8개 통과, Lint **0 errors, 43 warnings**.
- 최종 앱 재설치·시작 및 SHA-256 일치 확인: **2dbebecc95283d30ff7e87717aaf7baae61a619a7d834cee9edf19b3122544af**.
- 종료 시 시험용 예약/알림 없음. USB 화면 유지=0, 자동 회전=0, 사용자 회전=0으로 원래 설정 복원. 알림 권한 허용 상태, 최종 앱 프로세스의 새 fatal 없음.

## 검증 종류별 실제 결과

| 종류 | 결과·범위 |
| --- | --- |
| 전체 R8 release 외부 UI | **2개 통과**, failures/errors/skipped=0. 13:25:21 KST, 47.545초. 러너는 별도 APK/프로세스 |
| 외부 UI 1 | 실제 알림 권한 철회 → 화면 타이머 → 가로/세로 전환 → 홈 복귀 → 종료 |
| 외부 UI 2 | 일반 am kill의 실제 PID 소멸·예약 보존, force-stop의 OS 예약 취소, 앱 재실행 시 **원래 예약 시각** 복원, 새 예약 시작 |
| 기존 debug Compose UI | 2개 통과. Activity 재생성·종료 및 시험용 기존 대화 데이터/로컬 기록 삭제 직접 확인 |
| 비최적화 release 기능 QA | 알림 계약 2개 통과. 만료 상태를 주입해 알림 제목·종료 PendingIntent·예약 제거와 조기/취소 수신 억제 확인 |
| 기존 단위 테스트 | 8개 통과. 기존 점수/시선 유틸 등을 포함하며 현재 타이머 전체 단위 커버리지라는 뜻은 아님 |
| 실제 수동 조작 | 권한 거절 후 안내, 권한 없는 타이머·홈 복귀, 개인정보 안내 열기/닫기, 삭제 취소·확정, 실제 알림 본문 복귀 |

서로 다른 빌드의 결과를 합쳐 “production release 6개 통과”라고 표시하지 않는다. 비최적화 QA의 상태 주입을 자연 20분 전달 시험으로 표시하지 않는다.

## 실제 시간 경과 알림

| 빌드·조건 | 시작 → 예정 → 실제 OS 전달 | 실측 |
| --- | --- | --- |
| 초기 debug / 충전·화면 꺼짐 | 11:17:09.004 → 11:37:09 → 11:40:16.446 | 23분 7.442초 경과, 예정 대비 3분 7.443초 지연 |
| 전체 R8 release / 충전·화면 유지, 홈으로 이동 | 12:21:31.336 → 12:41:31.335 → **12:46:06.449** | **24분 35.113초 경과, 예정 대비 4분 35.114초 지연** |

두 시험 모두 예약 단축·만료값 주입 없이 관찰했다. 두 번째 시험에서는 실제 알림 제목/내용을 확인하고 본문을 터치해 앱 휴식 안내로 복귀했으며 알림 자동 닫힘도 확인했다. 실제 20분 이후 화면 내 휴식 안내도 확인했다.

두 번째 자연 시험은 아래 예약 복원 수정 전 APK에서 수행했다. APK와 해시 9769630762b47e84412d72ed4ee8cb5d84be80513070c377f04727ec01554a41을 보존했다. 이후 수정은 앱 재실행 시 남은 예약 복원이며 20분 간격/수신기 전달 코드는 바꾸지 않았다. 최종 APK에서 자연 20분을 다시 관찰했다고 주장하지 않는다.

예약의 비정확 배치 창 899,999ms를 확인했다. 이것은 실제 지연 상한 보장이 아니다. “약 20분”, 시스템 지연 가능 안내를 유지하며 정확한 알람 권한을 추가하지 않았다. 시스템 음소거 상태여서 소리·진동 수신은 판정하지 않았다. 충전·화면 유지 시험은 비충전 심층 절전·배터리 시험이 아니다.

## 발견·수정한 출시 차단 문제

### 네이티브 16KB 정렬

첫 실행의 Android 호환 경고를 발견해 CameraX 1.3.4 → 1.6.2, graphics-path 1.1.0으로 수정했다. ML Kit face-detection 16.1.7 유지(debug 전용).

- debug 64비트 라이브러리 8개, release APK/AAB 각각 2개 ELF PT_LOAD 정렬 검사 통과.
- SDK 36 zipalign -c -P 16 4로 debug/release APK ZIP 정렬 통과.
- verifyDebugNativeAlignment / verifyReleaseNativeAlignment / verifyReleaseBundleNativeAlignment 추가.
- 정렬 검사 통과는 실제 16KB 환경 실행을 대체하지 않는다.

### 최적화 release 시작 크래시

부분 최적화 packageScope 빌드에서 Kotlin 컬렉션 상속 접근의 IllegalAccessError를 단독 실행으로 재현했다. 실험적 packageScope를 제거하고 R8 전체 앱 최적화로 수정했다. 최종 단독 시작·외부 UI 2개 통과. 광범위 keep이나 전역 오류 무시는 추가하지 않았다.

### 강제 종료 후 사라진 OS 예약과 남아 있던 카운트다운

OS 예약이 취소돼도 화면에 이전 카운트다운이 남는 문제를 발견했다. 현재 구현은 사용자가 앱을 다시 열 때 저장된 시각이 아직 미래라면 **그 시각 그대로** 예약을 재등록한다. 같은 PendingIntent의 기존 예약을 대체하므로 중복 예약이나 새 20분 시작을 만들지 않는다. 프로세스당 한 번 실행해 회전마다 재등록하지 않는다.

과거 시작 기록/PID/wasForceStopped를 판별하는 중간 구현은 대조 시험에서 오인이 확인돼 최종 코드에서 제거했다. 새 구현은 자체 앱 시작 기록을 조회하지 않으며 재부팅 구분값이 다른 예약과 이미 지난 예약을 재등록하지 않는다.

외부 테스트에서 일반 프로세스 종료 전후와 force-stop 후 재실행의 실제 OS origWhen 값 일치를 검증했다. [BreakReminderScheduler.kt](../app/src/main/java/com/example/myapplication/reminder/BreakReminderScheduler.kt), [ProcessStartReminderRecovery.kt](../app/src/main/java/com/example/myapplication/reminder/ProcessStartReminderRecovery.kt).

## 테스트 러너 실패와 분리 해결

앱에 직접 붙는 release instrumentation은 13:08:56.565 PID 18782에서 AndroidJUnitRunner.onCreate의 androidx.tracing.Trace 부재로 다시 실패했다. XML의 testRunFailed 1개를 기능 테스트 실행으로 세지 않는다. 도구가 Build successful을 표시했어도 XML은 failures=1이었다.

출시 R8을 약화시키는 대신 release-validation self-instrumenting 모듈을 추가했다. 앱 내부 클래스를 참조하지 않고 외부 UI/OS 예약을 시험하며 러너와 라이브러리는 별도 APK에 존재한다. 최종 외부 UI 결과는 2/2 통과다. 기존 앱 부착 방식의 알림 계약 전체가 minified 환경에서 통과했다는 뜻은 아니다.

[러너/크래시 진단](P0_TEST_RUNTIME_DIAGNOSIS_2026-11-01.md), [테스트 구성과 명령](testing.md).

## 기타 수동 결과

- Google Play 버튼으로 스토어를 열고 복귀해도 타이머 유지. 현재 기기/계정에는 “항목을 찾을 수 없습니다”. Console 등록·공개·접근 가능 여부를 소유자가 확인해야 한다.
- debug 카메라 미허용 시작 차단, 수동 시작 후 camera FGS·실제 ML Kit 프레임 처리 확인. 약 147초 후 서비스 없음 관찰. 정확한 제한 시간/화면 꺼짐 원인 구분은 미완료. 카메라는 판매 release에 없음.

## 증거와 재현

.scratch/p0_device_20261101에 보존했다.

- final-blackbox-tests.xml: 최종 외부 UI 2개 통과. 원본은 release-validation/build/outputs/androidTest-results/connected/productionCheck/TEST-SM-S916N - 16.xml.
- baseline-debug-ui-tests.xml / baseline-unoptimized-qa-tests.xml: 이전 빌드별 통과 결과.
- final-minified-tests.xml / final-minified-crash.txt: 앱 부착 러너 실패 증거.
- baseline-alarm-state.txt / baseline-notification-keys.txt: 첫 자연 알림.
- followup-natural-alarm-state.txt / followup-natural-notification-keys.txt: 전체 R8 자연 알림.
- installed-artifact-evidence.txt: 자연 시험 APK 해시. final-release-artifact.txt: 최종 설치 APK 해시.
- followup-final-device-settings.txt / followup-runtime.txt: 설정 복원·최종 프로세스 상태.
- release-timer.png: 자연 시험 전 실제 화면. release-home-final.png: 최종 수정 release 실제 화면.

```
gradlew.bat :release-validation:connectedProductionCheckAndroidTest --init-script gradle/release-validation.init.gradle --no-configuration-cache
gradlew.bat :app:bundleRelease :app:verifyReleaseBoundary :app:verifyReleaseNativeAlignment :app:verifyReleaseBundleNativeAlignment :app:testDebugUnitTest :app:lintRelease
```

일반 출시 산출물 명령에 테스트 init을 붙이지 않는다. 개발용 인증서는 업로드 키가 아니다.

## 남은 P0

- [ ] 채널만 차단했을 때의 만료·복구 경로, 실제 소리/진동, 자연 알림의 종료 액션.
- [ ] 실제 재부팅·비충전 심층 절전, 지원하는 다른 OS/제조사·실제 16KB 실행·장시간 배터리/발열.
- [ ] 공개 방침 URL·운영자/지원 이메일, 판매 방식·가격/국가·유료 범위. Pro 선택 시 결제/복원/환불 구현·시험.
- [ ] 소유자 업로드 키·Play App Signing·versionCode/기존 활성 트랙, Console 신고·사전 출시 보고서.
- [ ] debug 카메라의 별도 화면 꺼짐/권한 철회/알림 종료/정확한 제한 시간 시험(판매 범위 제외).

[수동 시험표](P0_MANUAL_TEST_MATRIX.md), [Console 신고 검토 초안](PLAY_CONSOLE_DECLARATIONS_DRAFT.ko.md), [스토어 설명](STORE_LISTING_DRAFT.ko.md), [공개 방침 초안](PRIVACY_POLICY_DRAFT.ko.md).
