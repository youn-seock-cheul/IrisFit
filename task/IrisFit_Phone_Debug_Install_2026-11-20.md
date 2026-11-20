# IrisFit Galaxy S23 Debug 설치 기록 — 2026-11-20

- 승인: 사용자의 2026-11-20 “폰에 배포해줘” 요청에 따라 기기에 로컬 설치
- 결과: **설치 및 앱 시작 확인**
- 대상: Galaxy S23 (`SM-S916N`), Android 16. 기기 일련번호는 기록하지 않음.
- 소스 기준: `ac24beb` 및 해당 시점 dirty worktree의 현재 내용
- 패키지: `com.eyeoverlay.app`, Debug, versionName `1.0.0`, versionCode `1`, targetSdk 36
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `A6388697F107216A05E20BE63E7953FAFD89D633FAA9EE0F6EDFC7F74C97593C`

## 수행 및 확인

- `.\gradlew.bat assembleDebug` — 성공.
- 명시한 Galaxy S23 기기에만 `adb install -r` 실행 — `Success`. 기존 앱 데이터를 지우는 제거/초기화는 하지 않았다.
- Launcher 실행 요청 후 앱 PID가 확인됐고 `MainActivityDefault`가 resumed 상태였다. 직후 로그에서 `FATAL EXCEPTION` 및 해당 앱 프로세스 오류는 관찰되지 않았다.
- 설치 정보에서 versionName/versionCode 및 업데이트 시각을 확인했다.

## 범위와 제한

- 이 설치는 요청에 따른 **로컬 Debug APK 설치**다. Release 서명/빌드, Play Console 업로드, 공개/외부 배포는 하지 않았다.
- 별도 승인된 Release Hold 해제는 없으며 **Release Hold 유지**. 커밋 및 푸시도 하지 않았다.
- 앱 시작까지 확인했으며, 실제 UsageStats 권한 허용 후 사용량 갱신, Health Connect의 실제 데이터, 정확도·전력/발열 및 전체 수동 UI 검증은 이번 설치 작업에서 **NOT RUN**이다.
- 테스트용 격리 에뮬레이터에는 Debug 앱을 남기지 않았다.

## 설명 UI 간결화 후 업데이트 설치 — 2026-11-20

- 사용자 승인에 따라 홈 안내를 짧은 한 줄로 표시하고 `자세히`/`접기`로 산식·고지를 열고 닫도록 변경했다. 한국어·영어·일본어·스페인어에 반영했다.
- JVM **106/106**, Release Kotlin 컴파일, AndroidTest 컴파일 및 `verifyReleaseBoundary` 통과. 격리 Android 17 에뮬레이터의 홈 펼침/접힘 회귀 테스트 **1/1 통과**.
- 새 Debug APK를 같은 Galaxy S23에 `adb install -r`로 업데이트 설치했다. 설치 성공, PID 및 foreground Activity 확인. 앱 데이터 초기화는 하지 않았다.
- 최종 APK SHA-256: `71581D587435206731E6C21108E686BE021DB9600242F7133AA5923CCA3FC0FA`
- 설치 후 수면/활동 실제 데이터, UsageStats 정확도, 전체 실기기 UI 및 전력 검증은 **NOT RUN**. Release/Play 배포와 commit/push는 하지 않았으며 Release Hold 유지.

## Health Connect 안내·카드 밀도 개선 후 업데이트 설치 — 2026-11-20

- 사용자 요청에 따라 홈 카드의 과한 간격을 줄이고, 일반 `자세히` 링크를 정보 아이콘·회전 화살표가 있는 `산식` pill로 변경했다. Health Connect에서 데이터 읽기가 완료된 경우에만 실제 지난밤 수면/깊은 수면 비율, 기존 산식의 계산된 시작 잔량, 걸음·등산 활동 보너스를 간결하게 표시한다. 기존 점수 산식은 변경하지 않았다.
- JVM `EyeBatteryCalculatorTest` 및 `AppLocalizationTest` **15/15**, `compileDebugAndroidTestKotlin`, `compileReleaseKotlin`, `verifyReleaseBoundary`, `assembleDebug` 통과. Android 17 격리 AVD에서 `connectedDebugAndroidTest` 전체 suite 성공. `git diff --check` 통과.
- 새 Debug APK를 Galaxy S23(Android 16, `SM-S916N`)에 `adb install -r`로 업데이트 설치했다. 설치 성공, PID `4333`, `MainActivityDefault` resumed 확인. 앱 제거/데이터 초기화는 하지 않았다.
- APK SHA-256: `2769B41A45ADF15AA6FE4FA53FB2307BB3F27D8FD71B768BA65EA8F78DF0FCB4`
- 사용자가 요청한 로컬 Debug 설치만 수행했다. Release/Play 배포, commit/push는 하지 않았고 Release Hold 유지. 실기기 Health Connect 실제 기록/권한에 따른 메시지 표시, 정확도·전력 및 수동 UI 검증은 **NOT RUN**.

## 상태 구간 4단계 복원 후 업데이트 설치 — 2026-11-20

- 사용자 요청("폰에도 배포해줘")에 따라 상태 구간을 기존 4단계(위험 <45%, 경고 45–64%, 주의 65–84%, 최상 85%+)로 복원한 작업 트리를 설치했다. 점수 산식과 Health Connect 반영은 변경하지 않았다.
- 설치 직전 `assembleDebug`와 `compileDebugAndroidTestKotlin`이 통과했다. 설치 후 전체 JVM `testDebugUnitTest`는 22개 클래스 **108/108** 통과, 실패·오류·skip 0건이다.
- 처음에는 `adb devices`에 기기가 표시되지 않았다. Windows에서는 휴대폰이 USB/MTP로 인식된 상태였다. ADB 서버를 재시작하자 Galaxy S23(Android 16, `SM-S916N`)이 `device` 상태로 연결됐다.
- `adb install -r`로 업데이트 설치에 성공했다. 앱 제거와 데이터 초기화는 하지 않았다. 런처 실행 후 PID `15025`, `MainActivityDefault` resumed를 확인했다. `versionName=1.0.0`, `versionCode=1`, `lastUpdateTime=2026-11-20 19:05:00`이다. 몇 초 뒤에도 같은 PID가 유지됐고, crash 버퍼에는 이번 설치 이후의 앱 crash가 없었다.
- 실기기 홈 화면을 캡처해 4단계 범례와 현재 상태 `최상 (행복)` 강조를 확인했다. 캡처는 저장소에 포함하지 않았다. 이때 화면 사용 17시간 53분에도 100%가 표시된 것은 기존 산식에 따른 결과다. 휴식 1회(+12%)와 훈련 8회(+160%, 상한 200%)의 회복량이 사용시간 차감보다 커서 최대값 100으로 제한됐다.
- APK SHA-256: `963221ADC1EF735C5F6D0E10295FB48D9BD1FBBE2F27D4D6038835058E2F7680`
- 이번 복원본의 격리 에뮬레이터 AndroidTest는 **NOT RUN**이다. 실기기 Health Connect 실제 기록/권한 흐름, UsageStats 정확도, 전력·발열, 위젯 표시, 4개 언어 수동 UI 검증도 **NOT RUN**이다. Release/Play 배포와 commit/push는 하지 않았고 Release Hold를 유지한다.

## Health Connect 권한 복구·연동 표시·산식 간결화 후 설치 — 2026-11-20

- 사용자 요청("S23에 설치해줘")에 따라 HEAD `ac24beb` + 기존 미커밋 작업 트리의 Debug APK를 Galaxy S23(Android 16, `SM-S916N`)에 `adb install -r`로 업데이트했다. 앱 제거·데이터 초기화·건강정보 권한 자동 허용은 하지 않았다.
- 설치 범위는 Android 14 이상 권한 안내 화면 등록, 권한 확인·읽기 성공 시 홈의 `건강정보 연동 중` 표시, 산식의 시작 잔량·화면 사용·회복 세 항목 표시다. 기존 점수 계산과 4단계 상태 구간은 유지했다.
- 이번 실행의 `:app:assembleDebug` 성공 후 설치 `Success`, 앱 시작 `Status: ok`, `MainActivityDefault` resumed를 확인했다. `versionName=1.0.0`, `versionCode=1`, `lastUpdateTime=2026-11-20 19:32:40`이다. 후속 확인에서도 동일 프로세스가 실행 중이었으며 해당 프로세스의 crash 버퍼에 오류 항목이 없었다.
- 설치된 패키지의 `VIEW_PERMISSION_USAGE` + `HEALTH_PERMISSIONS` 조회가 `ViewHealthPermissionUsageActivity`로 해석됨을 확인했다. 로컬 APK와 기기에 설치된 `base.apk`의 SHA-256이 일치했다: `A3E613CB1804DA487FF6F3FEBCE24AA4940027C787357921DE86789D6A600454`.
- 이번 설치 작업에서는 JVM/AndroidTest를 재실행하지 않았다. 실기기 Health Connect 동의·실데이터 반영·전체 UI·정확도·전력 검증은 **NOT RUN**이며, 이전 격리 기기 검증을 이번 실기기 검증으로 간주하지 않는다.
- 개발 기록만 추가했으며 기존 소스 변경을 보존했다. 로컬 Debug 설치 외 commit/push·Release/Play 업로드는 하지 않았고 Release Hold를 유지한다.

## 홈 배터리 카드 목업 정리 — 2026-11-20 (미설치)

- 이후의 홈 카드 목업 정리·상태 라벨 단어·연동 문구 변경은 Galaxy S23에 설치하지 않았다. S23에는 위 19:32 설치본이 남아 있다. 변경·검증: [구현 기록](IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md).

## 홈 배터리 카드 목업 정리본 업데이트 설치 — 2026-11-20

- 사용자 요청에 따라 현재 Debug APK를 Galaxy S23(Android 16, `SM-S916N`)에 `adb install -r`로 업데이트 설치했다. 설치 결과 `Success`; 앱 데이터는 유지했고 런타임 권한을 자동 허용하지 않았다.
- APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256: `7F89179C6EF069137FC323533F322A737321C18496300FE65EBDE951851F4B5C`. 설치된 `base.apk`의 SHA-256도 일치한다.
- 패키지 `com.eyeoverlay.app`, versionName `1.0.0`, versionCode `1`; `MainActivityDefault` 실행 후 PID 확인, resumed Activity 확인. 설치 시각은 기기 기준 `2026-11-20 21:11:34`.
- 이번 작업은 기존 검증된 APK 설치·기동 확인만 수행했다. 테스트 재실행, 카드 전체 수동 검수, Health Connect 실데이터·권한, 사용량 정확도·전력 검증은 **NOT RUN**이다. Release/Play 배포와 commit/push는 하지 않았으며 Release Hold 유지.

## 목업 이전 카드 스타일 복원본 업데이트 설치 — 2026-11-20

- 사용자 요청에 따라 목업 이전 스타일과 배치·문구 조정이 적용된 최신 Debug APK를 Galaxy S23에 `adb install -r`로 업데이트 설치했다. 설치 결과 `Success`; 앱 데이터와 기존 런타임 권한은 유지했다.
- APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256: `F2E5EA74C49A54E134881B7D134C85AA7E53186369976977679D41CA73ADB505`.
- `MainActivityDefault`가 resumed 상태이며 PID `25161`을 확인했다. 패키지 versionName `1.0.0`, versionCode `1`, 기기 설치 시각 `2026-11-20 21:24:06`. 설치 APK SHA-256은 로컬 APK와 일치한다.
- 앞서 실행한 Debug 조립, AndroidTest 컴파일, `AppLocalizationTest`·`ScreenTimeFormattedTest` 통과 결과를 사용했다. 이번 설치에 대해 에뮬레이터·실기기 시각 검수나 Health Connect/UsageStats 실데이터 검증은 **NOT RUN**이다. Release/Play 업로드와 commit/push는 하지 않았으며 Release Hold 유지.
