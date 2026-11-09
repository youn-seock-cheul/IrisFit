# P0 출시 전 검증 결과

최신 검토일: 2026-11-01. **판정: 일부 실기기 검증 통과, 출시 보류.**

## 최신 제품 범위 검토

원래 눈 운동·오버레이·카메라 구상과 현재 타이머 중심 release의 차이, 사용자 가치 가설 및 빠진 핵심 체험을 [제품 차이·개발 우선순위](PRODUCT_GAP_REVIEW.ko.md)에 정리했다. 아래 첫 판매 범위는 이전 P0에서 축소한 임시 빌드 범위이며 최종 제품 승인으로 해석하지 않는다. 후속으로 30초 안내 세션·사용자 확인 완료 기록을 구현했다. 단위 18개, debug UI 4개(39.708초), 전체 R8 release 외부 UI 1개(53.607초) 통과. release AAB·경계·AAB 정렬 검사 성공, Lint 오류 0·경고 46. [최신 구현 결과](GUIDED_SESSION_IMPLEMENTATION_2026-11-01.md)를 따른다. 다음은 짧은 실제 체험으로 안내 문구·무음 종료의 불편 확인이다. 고정 5분 제한 대신 오래 걸리는 검증은 시간·목적·한계를 먼저 알려 사용자 판단을 받는다. 장시간 알림·절전 시험은 보류한다.

## 현재 추가 진행 — 채널 차단·복구

개별 휴식 채널 차단 감지·채널 설정 직접 연결·예약 시작 시 채널 생성을 수정했다. AAB·경계·정렬·단위 테스트 8개·Lint 및 테스트 APK 빌드 성공. 실제 Galaxy S23+에서 외부 UI 3개가 모두 통과했다(14:11:14 KST, failures/errors/skipped=0). 첫 채널 테스트는 설정에서 앱으로 돌아오는 테스트 탐색 때문에 실패했으며, 뒤로 가기로 복귀하도록 수정한 재실행에서 통과했다. 채널 설정도 원래 상태로 복원했다. 아래 UI 2개·APK 기록은 이전 빌드 기록이다.

자연 알림 종료 시험은 14:37경 사용자 요청으로 중단했다. 통과/실패 판정 없음. 러너·앱 종료, 활성 OS 예약/알림 없음, USB 화면 유지/회전 0/0/0 복원 확인. 채널 차단 중 만료·재부팅·비충전 절전은 미완료로 보존하되 추가 장시간 시험은 보류한다. 현재 출시 화면이 휴식 타이머로 축소된 상태와 원래 눈 운동·카메라 제품 구상의 차이를 먼저 정리해야 한다. 제품/판매 판단은 지금 요구하지 않는다.

## 2026-11-01 이전 실기기 결과 — 채널 보완 전

- Galaxy S23+ / Android 16(API 36): **전체 R8 release 외부 UI 2개 통과**(13:25:21, XML failures/errors/skipped=0). 별도 debug UI 2개·비최적화 QA 알림 계약 2개와 구분한다.
- 자연 알림 2회: 초기 debug는 23분 7.442초, 전체 R8 release는 **24분 35.113초 후 전달(4분 35.114초 지연)**. 두 번째 실제 알림 본문 터치·앱 복귀·자동 닫힘 확인. USB 충전 시험이며 소리/심층 절전/지연 상한 보장은 아니다. 두 번째 관찰 후 예약 복원 코드를 추가했으며 최종 APK에서 자연 20분을 다시 관찰한 것은 아니다.
- 16 KB 네이티브 정렬 경고를 발견해 CameraX 1.6.2 / graphics-path 1.1.0으로 수정. APK/AAB ELF 및 APK ZIP 검사 통과. 실제 기기는 4 KB이므로 16 KB 실행은 별도.
- 최적화 release 단독 실행에서 IllegalAccessError를 발견·재현했다. 실험적 packageScope를 제거하고 R8 전체 앱 최적화로 수정. 초기 수정 APK의 COLD 시작 419ms를 확인했고, 최종 수정 후 단독 시작·외부 UI 2개·설치 APK 해시 일치를 다시 확인했다.
- 기본 설정 release AAB 재빌드·경계/네이티브 검사 통과, 기존 단위 테스트 8개 통과, 최종 Lint **0 errors, 43 warnings**. AGP 9.5.0 유지.
- 강제 종료 후 사라진 OS 예약을 화면이 계속 카운트다운하던 문제를 수정. 사용자가 앱을 다시 열 때 **원래 시각의 남은 예약을 복원**한다. 외부 UI가 일반 종료/force-stop 전후 실제 OS 예약 시각을 검증했다. 앱 부착 instrumentation은 Trace 클래스 부재로 실패했으나 별도 self-instrumenting 모듈은 출시 R8을 유지하며 2개 통과.
- 권한 거절·철회·회전·홈 복귀·종료·삭제 수동 확인. 채널별 차단, 실제 재부팅·비충전 심층 절전·다른 기기/OS·배터리·실제 16KB 실행은 미완료. 임시 화면 유지/회전은 원래 값으로 복원하고 시험 예약/알림을 정리했다.
- 운영자/공개 방침, 판매 방식/가격/결제, 업로드 키·Play Console 승인 조건도 계속 미완료.

상세: [실기기 기록](P0_DEVICE_VALIDATION_2026-11-01.md), [시험표](P0_MANUAL_TEST_MATRIX.md), [크래시/테스트 진단](P0_TEST_RUNTIME_DIAGNOSIS_2026-11-01.md), [테스트 실행 방법](testing.md), [Console 신고 검토 초안](PLAY_CONSOLE_DECLARATIONS_DRAFT.ko.md).

아래는 2026-10-31의 구현 범위와 당시 실행 기록이다. 기기 미연결·UI 미실행·Lint 45개 표기는 최신 결과가 아니다.

## 이번 변경의 범위

첫 판매 대상을 **EyeOverlay 화면 휴식 타이머**로 정리했다. 정식 release는 계정·카메라 없이 사용하는 약 20분 후 1회 휴식 알림과 화면 내 타이머, 종료, 로컬 기록 삭제, Google Play 업데이트 확인을 제공한다. 정확한 시각 알람·자동 반복·의학적 측정 기능은 제공하지 않는다.

- 합성 피로도/동공 값, 고정 심박수 72 BPM/보험 할인 점수 96점, 근거 없는 50% 효과 등의 표시를 실제 출시 화면에서 제거.
- 기존 시력 검사·의사 AI 코칭·DMS·Wear OS·보험/내비 프로토타입은 판매 대상과 빌드 대상에서 제외.
- debug만 카메라·오버레이 실험 포함. 미검출을 졸음으로 판정하지 않으며 앱에서 사용자가 시작하고 앱/알림에서 종료, 화면 꺼짐/최대 2분 후 종료하도록 구현. 실기기 검증은 미완료.
- YouTube 접근성 서비스 manifest 등록과 자동 카메라 시작 제거.
- APK 자체 복사/설치 및 REQUEST_INSTALL_PACKAGES/FileProvider 경로 제거.
- MediaPipe·GenAI 의존성 제거, CameraX/ML Kit/lifecycle-service를 debug 의존성으로 한정.
- 앱 이름은 판매 화면에서 AI 기능을 암시하지 않는 EyeOverlay로 변경. applicationId는 com.eyeoverlay.app 유지.

## 소스 위치와 빌드 경계

[app/build.gradle.kts](../app/build.gradle.kts)의 sourceSets가 실제 컴파일 대상을 명시한다.

- 실제 Activity/UI: [MainActivity.kt](../app/src/production/kotlin/com/example/myapplication/MainActivity.kt), [WellnessHome.kt](../app/src/production/kotlin/com/example/myapplication/ui/WellnessHome.kt), [PrivacyPolicyDialog.kt](../app/src/production/kotlin/com/example/myapplication/ui/PrivacyPolicyDialog.kt)
- 공통 소스: src/main/java 하위 data, reminder, ui/theme 디렉터리.
- debug 전용 소스: src/main/java 하위 service, vision. 권한/서비스 등록은 [debug manifest](../app/src/debug/AndroidManifest.xml)에만 존재.
- 그 외 기존 src/main/java의 MainActivity/UI/AI 프로토타입은 보존된 미컴파일 코드다. 해당 파일을 수정해도 현재 출시 화면에는 반영되지 않는다.
- 기존 MediaPipe 모델 assets는 빌드 대상에서 제외.
- 교체 전 Activity/Service 원문은 .scratch/p0_original에 보존. 이 저장소에서는 git 저장소를 확인하지 못했다.

## P0 항목별 판정

| 항목 | 코드에서 처리한 사항 | 남은 완료 조건 |
| --- | --- | --- |
| 효능/범위 | 가짜 측정값/의료 효과 표시 제거, 비의료 고지 및 실제 기능에 맞는 상품 페이지 초안 | 건강 앱 신고, 실제 스크린샷·공개 상품 페이지 확인 |
| 접근성/카메라 시작 | 접근성 서비스 등록 제거, release 카메라 권한/코드/SDK 제외 | 실제 제출 산출물/Console 선언 대조. debug 권한 철회 등 기기 시험 |
| 사용자 통제 | 알림 예약 취소 및 기록 삭제. debug 세션 중지·시간 제한 구현 | UI 테스트 실행, 절전/차단/프로세스 종료/재부팅/실제 알림 전달 시험 |
| 개인정보 | 데이터 흐름 표, 로컬 삭제, 백업/기기 이전 제외, release 인터넷 권한 및 연구 SDK 부재 검사 | 운영자/문의 이메일/공개 방침 URL·시행일 확정 및 앱 연결, Data safety 제출 |
| DMS/Wear/보험 | 첫 판매 범위에서 제외. 미검출의 졸음 판정 제거 | 재도입 시 성능·기기·계약 검증 별도 선행 |
| 결제 | 허위 결제/업데이트 흐름 제거. 현재 인앱 구매/Pro 없음 | 판매 방식·가격 확정. Pro 선택 시 Play 상품/토큰 검증/승인·보류/환불·복원 시험 |
| 릴리스 | compileSdk/targetSdk 36, release AAB와 검증 task 생성 | 업로드 서명·Play App Signing·versionCode 중복 확인·개발자/결제 프로필·테스트 트랙·기기 품질 검증 |

## 2026-10-31 실행한 검증 — 과거 기록

Gradle 동기화 성공. 다음 명령 전체가 **BUILD SUCCESSFUL**로 완료됐다.

```
gradlew.bat :app:assembleDebug :app:bundleRelease :app:verifyReleaseBoundary :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintRelease
```

- 기존 단위 테스트 8개 통과. 새로운 UI 테스트 2개는 테스트 APK 컴파일까지 확인했으며, 연결된 기기가 없어 실행하지 않았다.
- release Lint: **0 errors, 45 warnings**. 남은 경고는 버전 업데이트 권고, 미사용 리소스, KTX 사용 권고 등이며 이번 작업에서 전체 경고를 없앴다고 주장하지 않는다. baseline이나 전역 오류 무시는 추가하지 않았다.
- verifyReleaseBoundary: 병합 manifest 및 releaseRuntimeClasspath 검사 통과.
  - 권한: android.permission.POST_NOTIFICATIONS, com.eyeoverlay.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION.
  - CAMERA/SYSTEM_ALERT_WINDOW/FOREGROUND_SERVICE/REQUEST_INSTALL_PACKAGES/INTERNET/ACCESS_NETWORK_STATE 없음.
  - service 등록 없음. CameraX/ML Kit/MediaPipe SDK 없음.
  - 검증 task는 직렬화 가능한 입력을 사용하며 configuration cache 저장까지 성공.
- adb devices 결과: 연결된 기기 없음.
- 생성 산출물: app/build/outputs/apk/debug/app-debug.apk, app/build/outputs/bundle/release/app-release.aab. **AAB 생성은 업로드 서명이나 Play 승인 증거가 아니다.**

UI 테스트: [WellnessHomeTest.kt](../app/src/androidTest/java/com/example/myapplication/WellnessHomeTest.kt)
- 카메라/알림 권한 없이 타이머 시작 → Activity 재생성 후 예약 유지 → 종료.
- 로컬 기록 전체 삭제 → 이전 대화 기록과 예약 제거.

기기를 연결한 뒤 위 두 테스트와 알림 수신·재부팅·절전·알림 차단을 실행해야 한다. 서로 다른 실제 기기/OS에서 배터리·발열·장시간 동작, 지원하는 네이티브 라이브러리의 페이지 크기 호환성도 확인한다. SDK 설정을 올렸다는 사실만으로 호환성 시험을 대체하지 않는다.

## 정책 점검 근거와 한계

[수정 전 자동 정책 보고서](../.scratch/play_policy_insights_b75d6905-a0b8-496e-9a80-e8bd09d7ba88/compliance_report.md)는 play-policy-insights 스킬의 수집·작업자·검증·보고 절차로 생성했다. 해당 보고서는 수정 전 접근성 고지 누락과 카메라 FGS 시작 경로를 지적한다. 이번 release에서는 해당 기능과 SDK 자체를 제외하고 별도 경계 검사를 수행했다. 수정 전 보고서의 판정을 수정 후 앱의 확정 판정으로 사용하지 않는다. 자동 보고서의 일부 데이터 유형 표에는 키워드 오탐 설명도 포함되어 있으므로 실제 수집 목록으로 그대로 옮기지 않는다.

공개 미국 스토어 페이지 조회는 404였으며, 다른 국가·비공개 트랙·Play Console 등록이 없다는 뜻은 아니다. Console 접근/제출/승인은 수행하지 않았다.

- [target API 공식 요구사항](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en): 조회일 기준 신규 앱/업데이트 API 36 요구를 확인해 반영.
- [접근성 정책](https://support.google.com/googleplay/android-developer/answer/10964491?hl=en)
- [건강 콘텐츠 정책](https://support.google.com/googleplay/android-developer/answer/16679511?rd=2)
- [카메라 FGS 제한](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [자체 APK 설치 권한 정책](https://support.google.com/googleplay/android-developer/answer/12085295?hl=en-en)

## 운영자가 제공/완료해야 하는 출시 차단 항목

1. 운영자명, 수신 가능한 고객지원 이메일, 로그인 없이 열리는 공개 개인정보처리방침 URL 및 시행일.
2. 유료 다운로드 또는 무료+Pro 선택, 가격/국가/유료 기능 범위. Pro 선택 시 실제 Play 상품 정보와 결제 구현·테스트 필요.
3. 소유자가 관리하는 업로드 키와 Play App Signing 설정, Console 최고 versionCode 및 패키지/서명 일치 확인.
4. 건강 앱/Data safety/광고/대상 연령·등급/앱 액세스 신고, 계정별 테스트·프로덕션 액세스 조건 충족.
5. 실제 기기 테스트 및 필요 시 16 KB 페이지 크기 환경 검증, 사전 출시 보고서와 최종 스크린샷.

초안: [개인정보처리방침](PRIVACY_POLICY_DRAFT.ko.md), [스토어 상품 페이지](STORE_LISTING_DRAFT.ko.md).
