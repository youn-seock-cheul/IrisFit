# 눈 배터리 화면 사용 추정 및 선택형 Health Connect — 2026-11-20

- 상태: **구현 및 자동 검증 완료; 제품 전체 QA PARTIAL PASS**
- 승인 범위: 사용자가 요청한 UsageStats 화면 사용 기반 점수 추정과 선택형 Health Connect 연동. Health Connect 기본값은 꺼짐.
- 기준 revision: `ac24beb`; 시작 시점부터 여러 기존 수정이 포함된 dirty worktree였으며 기존 변경을 보존했다.
- 당시 기능 검증: 실기기 설치 없이 격리 Android 17 에뮬레이터에서 계측했다. 이후 사용자 요청에 따른 Galaxy S23 Debug 설치는 [별도 설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md)을 참조한다. Release 서명/Play 배포, commit 및 push는 하지 않았으며 **Release Hold 유지**.

## 동작

- `UsageStatsManager`의 오늘 앱 전경 사용시간을 집계한다. IrisFit 자체 사용시간은 제외하며, 실제 화면 주시나 눈의 피로를 직접 측정하지 않는다.
- 추정 산식은 가중 화면 사용 15분당 1% 차감이다. 22:00–05:00 및 비디오/게임 사용은 1.25배, 소셜 카테고리는 1.1배 가중한다. 저장된 완료 휴식은 건당 +12%, 훈련은 건당 +20%이며 최종 점수는 0–100으로 제한한다.
- Usage Access가 거부되거나 쿼리 오류가 나면 실제 0분과 혼동하지 않고 `미측정`을 표시한다. 이벤트 정보를 사용할 수 없으면 기본 집계값으로 계산하고 가중치가 빠질 수 있음을 안내한다.
- Health Connect는 선택 설정이며 기본 꺼짐이다. 사용자가 켜기를 선택하면 읽을 데이터와 기기 내 사용 방식을 설명한 뒤 수면·걸음·운동 권한을 요청한다. 연동하지 않을 때 시작 잔량은 100% 기준이며, 수면 데이터가 있으면 시작 잔량(60–100), 활동은 최대 +5% 보너스에 반영한다. 권한/데이터가 없어도 기본 화면 사용 추정은 계속된다.
- 걸음 수와 등산 세션은 자연광 노출 자체가 아니며 활동 대리 지표다. UI는 이 추정치가 실제 시선·피로 측정이나 의학적 진단이 아님을 밝힌다. Health Connect 설정을 끄면 연동 데이터 읽기를 중단하고, 사용자는 별도로 Health Connect 설정에서 권한을 철회할 수 있음을 안내한다.
- 홈과 위젯에는 최신 점수를 공유한다. 설명·권한 안내·상태 문구를 한국어, 영어, 일본어, 스페인어에 반영했다.
- 홈에는 간결한 한 줄 요약만 기본 표시하고, 산식과 전체 고지는 `자세히`/`접기`로 열고 닫도록 했다. 사용자의 추가 승인에 따른 표시 방식 변경이다.
- 이후 승인된 홈 후속 수정에서 카드 안쪽 여백·항목 간격을 줄이고 `산식` 펼침 동작을 아이콘·화살표가 있는 컴팩트한 pill로 바꿨다. Health Connect 데이터 읽기가 완료되고 오류/미연동 상태가 아닐 때 실제 수면 시간, 깊은 수면 비율(있을 때), 계산된 시작 잔량과 걸음/등산 활동 보너스를 홈에 표시한다. 산식은 변경하지 않았다.

## 변경 파일

- `app/src/production/kotlin/com/example/myapplication/data/ScreenTimeManager.kt`, `ScreenUseWeighting.kt` — UsageStats 권한 및 집계, 야간/앱 카테고리 시간 가중치.
- `app/src/production/kotlin/com/example/myapplication/data/HealthConnectManager.kt`, `HealthConnectRationaleActivity.kt`, `app/src/main/AndroidManifest.xml`, `app/build.gradle.kts` — Health Connect SDK, 권한 및 선택 연동 구성.
- `app/src/production/kotlin/com/example/myapplication/session/EyeBatteryEngine.kt`, `GuidedSessionViewModel.kt`, `EyeBatterySnapshotStore.kt`, `EyeBatteryCalculator.kt` — 점수 산출, 상태/오류, 저장 및 위젯 전달.
- `app/src/production/kotlin/com/example/myapplication/ui/MainScreen.kt`, `WellnessHome.kt`, `BatteryTrendCard.kt`, `app/src/production/kotlin/com/example/myapplication/session/AppLocalization.kt` — 설정 토글, 설명/상태 표시, 다국어 및 추세 UI.
- `app/src/test/java/com/example/myapplication/EyeBatteryCalculatorTest.kt`, `AppLocalizationTest.kt`, `app/src/androidTest/java/com/example/myapplication/UiFollowupTest.kt` — 산식·가중치·번역·설정 및 화면 회귀 검증. UI 회귀 검사는 권한 유무에 따라 측정/미측정 아이콘을 확인하도록 정정했다.
- 추가 후속 변경: `app/src/production/kotlin/com/example/myapplication/ui/WellnessHome.kt`, `AppLocalization.kt`, `app/src/androidTest/java/com/example/myapplication/UiFollowupTest.kt` — 홈 안내 요약/상세 펼침 및 4개 언어의 문구 검증.

## 검증 결과

- `$env:ANDROID_SERIAL='emulator-5554'; .\gradlew.bat testDebugUnitTest compileDebugAndroidTestKotlin compileReleaseKotlin verifyReleaseBoundary connectedDebugAndroidTest` — **성공**. JVM **106/106**, 격리된 Android 17 AVD AndroidTest **19/19**, AndroidTest Kotlin 컴파일, Release Kotlin 컴파일 및 Release boundary 확인 통과.
- Usage Access 허용 상태와 거부 상태를 각각 설정한 뒤 `UiFollowupTest#betaHomeHidesUnreleasedEntryPointsAndLabelsBatteryEstimate`를 실행해 두 분기 모두 **1/1 통과**했다. 허용 분기는 사용량 성공 결과가 점수/UI로 전달되는지, 거부 분기는 미측정 처리를 확인한다.
- `git diff --check` — 통과. 기존 파일의 LF→CRLF 변환 안내만 표시됐다.
- 추가 승인 후 compact-summary 펼침/접힘 Android UI 테스트 **1/1 통과**, JVM **106/106**, Release Kotlin 컴파일 및 boundary 확인 재통과. 새 APK를 Galaxy S23에 업데이트 설치하고 foreground 시작을 확인했다. 설치 세부 및 artifact hash는 [폰 설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md).
- 홈 안내·여백 후속 변경 검증: `.\gradlew.bat testDebugUnitTest --tests com.example.myapplication.EyeBatteryCalculatorTest --tests com.example.myapplication.AppLocalizationTest compileDebugAndroidTestKotlin compileReleaseKotlin verifyReleaseBoundary assembleDebug` — 성공, 선택 JVM 테스트 **15/15**, AndroidTest/Release 컴파일 및 boundary, Debug APK 조립 통과. `$env:ANDROID_SERIAL='emulator-5554'; .\gradlew.bat connectedDebugAndroidTest` — Android 17 격리 AVD 전체 AndroidTest **19/19 통과**. 한 차례 단일 AndroidTest 클래스 지정 인수는 Gradle task-selection 오류로 테스트를 시작하지 못해 전체 suite로 재검증했다. `git diff --check` 통과(LF/CRLF 안내만 있음).
- 최신 Debug APK를 Galaxy S23(Android 16)에 `adb install -r`로 업데이트 설치하고 PID 및 resumed `MainActivityDefault`를 확인했다. SHA-256은 [폰 설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md)에 남겼다.
- 앱 설정 권한 화면의 수동 조작, 실제 Galaxy 사용량 정확도, Health Connect provider의 실제 수면/활동 데이터 읽기, 센서/시청 정확도, 배터리·발열·전력 및 Play 정책 검증은 **NOT RUN**.

## 판정 및 잔여 위험

구현 산출물과 자동 테스트는 통과했지만, UsageStats는 앱 전경 사용을 화면 실제 주시로 대체하지 않는다. 홈의 눈 배터리는 라벨이 붙은 화면 사용 **추정치**이며 실제 시청시간 정확도 증거가 아니다. Health Connect 실제 provider/data 연동도 기기에서 확인되지 않았다. 전체 QA 완료나 제품 출시 승인은 아니며 기존 PM의 실제 시청시간·정확도·전력 검증 게이트와 Release Hold는 유지한다.

## 2026-11-20 — 상태 구간 변경 취소

- 사용자 요청에 따라 홈과 위젯의 상태 판정을 직전 4단계로 복원했다. 범례는 위험(45% 미만, 빨강), 경고(45–64%, 주황), 주의(65–84%, 노랑), 최상(85% 이상, 초록)이다. 미측정은 어느 구간에도 포함하지 않는다. 낮은 잔량을 이유로 응원 매칭을 활성화하지 않는다.
- `EyeBatteryCalculator.kt`의 `EyeFatigueStatus.fromScore`를 `EyeBatteryEngine.kt`와 `IrisFitWidgetProviders.kt`에서 공유한다. `WellnessHome.kt`와 `AppLocalization.kt`의 4개 언어 범례를 복원하고 `BatteryTrendCard.kt`의 주·월간 막대는 변경 전 그래프 색 기준(80/50)으로 되돌렸다. 0–100 점수, 사용시간 가중치, Health Connect 수면·활동 반영과 홈 설명은 변경하지 않았다.
- `EyeBatteryCalculatorTest`에서 가중 사용시간으로 0–100점 전부를 생성해 점수와 상태를 검증했다. Debug JVM 계산·다국어 두 클래스 **15/15**, `:app:compileReleaseKotlin` 통과. 이번 복원본의 기기 UI 검증과 폰 재설치는 이 단계에서 하지 않았다.
- 같은 날 후속 배포 요청에 따라 복원본을 Galaxy S23에 Debug 업데이트로 설치했다. 전체 JVM **108/108** 통과, 실행 및 홈 4단계 범례 표시를 확인했다. 격리 AVD AndroidTest와 실기기 Health Connect·정확도·전력 검증은 **NOT RUN**이다. 상세: [설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md).

## 2026-11-20 — Health Connect 권한 화면 복구와 홈 연동 표시

- **진단:** Galaxy S23(Android 16)의 기존 설치본에서 Android 13용 안내 화면은 조회됐으나, Android 14 이상용 `VIEW_PERMISSION_USAGE` + `HEALTH_PERMISSIONS` 조회는 `No activity found`였다. 앱은 요청한 읽기 권한이 모두 허용되어야 켜진다. 개인 건강 기록은 조회하지 않았다.
- **승인·변경:** `AndroidManifest.xml`에 기존 안내 화면을 가리키는 시스템 권한 보호 alias를 추가하고, `MainScreen.kt`는 권한 화면을 여는 중 Activity가 재생성돼도 설정 탭을 복구한다. `GuidedSessionViewModel.kt`와 `WellnessHome.kt`는 권한 확인과 읽기가 성공했을 때만 기존 부제 자리에 `건강정보 연동 중`을 표시한다. 기록 없음은 연결 실패와 구별하고, 권한 철회 시 기본 계산으로 돌아간다. `AppLocalization.kt`의 4개 언어 산식은 시작 잔량·화면 사용·회복 세 항목으로 줄였다. 점수·구간 불변.
- **검증:** HEAD `ac24beb` + 기존 미커밋 작업 트리. `testDebugUnitTest` 계산·다국어 15/15, `assembleDebug`, `assembleDebugAndroidTest`, `compileReleaseKotlin`, `verifyReleaseBoundary` 통과. Android 17 격리 AVD `UiFollowupTest`의 권한 안내 화면 조회·산식 펼침/접힘·기본 꺼짐 3/3 통과. 같은 AVD에서 앱 스위치 → 시스템 권한 허용 → 기록이 없어도 홈 연동 표시 → 연동 끄기 → 다시 켜기 → 권한 하나 철회 후 기본 표시 복귀까지 확인했다. JVM 결과는 `app/build/test-results/testDebugUnitTest/`, 기기 결과는 `am instrument -w` 및 ADB UI 확인 기준이다.
- **범위:** 구현 검증 단계에서는 수정본의 S23 설치 및 실데이터 반영·정확도·전력 검증을 하지 않았다. Release Hold 유지.
- **19:32 후속 설치:** 사용자 요청으로 수정본을 S23에 데이터 보존 Debug 업데이트로 설치했다. 이번 `:app:assembleDebug` 성공, 앱 실행, Android 14 이상 권한 안내 화면 등록, 설치 APK의 SHA-256 일치를 확인했다. 건강정보 권한은 자동 허용하지 않았고 실데이터·정확도·전력 검증은 여전히 **NOT RUN**이다. [설치 결과·APK 해시](IrisFit_Phone_Debug_Install_2026-11-20.md).

## 2026-11-20 — 홈 배터리 카드 목업 정리와 연동 문구

- **승인:** 사용자 선택으로 상태 라벨은 4단계 모두 괄호 안 단어만 표시한다. 이어서 사용자가 첨부한 목업처럼 홈 배터리 카드를 정돈하고 연동 문구를 `건강정보연동 배터리 표시 중`으로 바꾸도록 요청했다. 함께 제안한 ⓘ 정보창 정리안은 미승인이라 구현하지 않았다.
- **변경:**
  - `WellnessHome.kt`의 `EyePedometerCard`를 마스코트·`IrisFit`·부제·자세 배지 → 상태 칩·잔량 % → 게이지·단어 범례(현재 구간 강조) → 휴식·원근 조절·시선 추적 타일 → 메시지 박스 → 공유 순서로 재구성했다. 타일은 현재 값과 회복 효과(`배터리 +12%`, `+20%`)를 보인다. 연동 중이면 부제 자리에 연동 문구, 메시지 박스에 Health Connect 요약을 보이고, 요약이 없으면 상태 안내와 데이터 상태 보조 줄을 보인다. 미측정은 `미측정` 칩과 `--%`.
  - 카드에서 화면 사용 줄, 추정 요약, 산식 펼침, 별도 경고 문구를 뺐다. Usage Access 권한 배너·공개 다이얼로그와 공유는 유지했다.
  - `MainScreen.kt` 홈 상단의 `IrisFit` + 부제 두 줄을 `IrisFit 눈 건강 배터리` 한 줄로 바꿨다.
  - `EyeBatteryEngine.kt`에 회복 값 상수(휴식 12, 훈련 20)를 두어 타일 표시와 계산이 같은 값을 쓴다. 산식·구간은 불변.
  - `AppLocalization.kt`: 연동 문구 4개 언어, 범례 단어만, 한국어 상태 라벨 `행복·졸림·불안·피로`, 영어 `Happy·Sleepy·Fatigued·Rest Urged`, 한국어 공유 버튼 `나의 눈 건강 배터리 공유하기`. 새 키 `battery_card_name`, `home_battery_gain`을 추가하고 쓰지 않게 된 `home_subtitle`, `battery_card_sub`, `tap_to_rest`, `tap_to_exercise`를 삭제했다. 일본어·스페인어 상태 라벨은 괄호 안이 기분 단어가 아닌 고지 문구라 유지했다. 산식·추정 요약 키는 화면에서 쓰지 않지만 보류로 남겼다.
  - 상태 라벨 키를 함께 쓰는 위젯·알림·공유 문구도 같은 단어로 표시된다.
  - 테스트: `UiFollowupTest`(카드 제목, 이름/연동 문구, 범례, 휴식 타일, 산식 미노출), `WellnessHomeTest`·`GuidedSessionUiTest`(휴식 타일 tag 클릭). `colorChoiceIsPersistedAndAppliedAfterActivityRecreation`은 앞서 승인된 탭 유지(`rememberSaveable`) 때문에 재생성 후 `설정` 텍스트가 페이지 제목과 탭 두 곳에 있어 실패했고, 선택 가능한 탭 노드의 선택 상태를 확인하도록 고쳤다.
- **목업과 다른 점:** 칩은 승인대로 `행복`만 표시한다(목업 `최상 (행복)`). 목업의 효과 문구(+30초/+20/유지)는 실제 계산 값으로 대체했다. 색은 기존 상태색·테마를 따르며 `나의 휴식 기록` 카드는 변경하지 않았다.
- **검증:** HEAD `ac24beb` + 기존 미커밋 작업 트리.
  - `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:compileReleaseKotlin :app:verifyReleaseBoundary` 성공. JVM 22개 클래스 **108/108**(`app/build/test-results/testDebugUnitTest/`), Release boundary 통과.
  - 격리 Android 17 AVD(`Medium_Phone_API_37.0`, read-only): `UiFollowupTest`·`WellnessHomeTest`·`GuidedSessionUiTest` 1차 **15/16**(위 탭 테스트 1건 실패) → 테스트 수정 후 `UiFollowupTest` **12/12**. `WellnessHomeTest` 2/2, `GuidedSessionUiTest` 2/2는 1차에서 통과.
  - 같은 AVD 화면 확인: 4개 언어 측정 상태, 연동 상태(한국어·스페인어; 에뮬레이터 권한만 부여, 건강 기록 없음), 미측정(Usage Access 거부) 상태. 잘림 없이 표시되고 세 타일 높이가 같았다. 관찰: 스페인어 휴식 타일 라벨은 자동 축소로 다른 라벨보다 작다. 공유 링크 위 간격이 목업보다 약간 넓다(48dp 터치 영역 유지). 연동됐지만 기록이 없으면 연동 문구와 메시지 박스의 `기록 없음·기본 계산` 보조 줄이 함께 보인다.
  - **NOT RUN:** `BreakReminderDeliveryTest`·`SensorLifecycleInstrumentedTest`·`ExampleInstrumentedTest` 재실행, 위젯·알림 표시, 실데이터·정확도·전력. Galaxy S23 설치는 2026-11-20 21:11에 별도로 완료했으며 설치·기동 확인만 했다(상세: [폰 설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md)). commit/push 없음, Release Hold 유지.
- **고지 결정:** 상단 `동작`의 "UI는 추정치가 실제 시선·피로 측정이나 의학적 진단이 아님을 밝힌다"와 관련해 카드에서 추정 요약·산식 펼침이 빠졌다. 사용자는 "필수가 아니면 목업처럼 깔끔하게, 필요하면 아주 간단하게"라고 지시했다. Google Play Health Content and Services 정책은 의료기기가 아닌 건강 앱에 대해 앱 설명(스토어 설명)에 '의료기기가 아니며 진단·치료·예방하지 않는다'는 고지를 요구하며, 카드 표시는 요구하지 않는다. 따라서 카드 안 고지는 추가하지 않았다. 앱 안 고지는 최상 상태 설명, Usage Access 권한 안내(`추정치`), Health Connect 공개 화면, 공유 문구에 남아 있다. 출시 준비 때 스토어 설명 고지를 반영해야 한다(Release Hold 중 미작성).

<a id="home-layout-restore"></a>

## 2026-11-20 — 목업 이전 스타일 복원, 배치·문구 정리

- 승인: 목업 외형을 되돌리고 사이즈 조절 없이 배치·텍스트만 수정. `WellnessHome.kt`는 목업 직전 원본을 기준으로 카드 패딩(16dp), 항목 간격(8dp), 마스코트(52dp), 잔량 글꼴(titleMedium), 타일 아이콘(18dp)·글자(10/11/9sp), 상태 배지와 안내 상자 스타일을 복원했다. 목업 타일의 글자 자동 축소는 사용하지 않는다.
- `MainScreen.kt`: 원래 두 줄 헤더 스타일(headlineMedium/bodyLarge)에 IrisFit과 오늘 누적 화면 사용 시간을 배치했다. 카드는 이름·연동 상태 → 상태·잔량·게이지 → 건강 메시지 → 휴식·훈련 순이다. 단어 상태 라벨, 연동 문구, 권한 안내와 공유 동작을 유지했다. 계산·권한·데이터 처리 코드는 수정하지 않았다.
- 검증: 원본과 새 헤더·카드의 치수 및 글꼴 값 비교, `:app:assembleDebug`, 헤더 변경을 반영한 `UiFollowupTest` 포함 `:app:compileDebugAndroidTestKotlin`, `:app:testDebugUnitTest`의 `AppLocalizationTest`·`ScreenTimeFormattedTest` 통과.
- 구현 직후에는 에뮬레이터 및 실기기 시각 확인, 실데이터 검증, S23 설치를 하지 않았다. 후속 사용자 요청으로 21:24 수정본 Debug APK를 S23에 설치했다(설치·기동·해시 확인만; 상세: [폰 설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md)). Release Hold 변경 없음.

<a id="home-copy-center"></a>

## 2026-11-20 — 홈 안내 간결화·휴식/훈련 중앙 정렬

- 승인: 대시보드의 `의료 측정값은 아닙니다`와 건강 기록 없음 안내를 삭제하고, 사용자에게 보이는 기술 명칭을 쉬운 표현으로 교체한다. 휴식·원근 조절·시선 추적 버튼의 아이콘과 텍스트는 중앙 정렬한다. 크기·여백 변경은 하지 않는다.
- `AppLocalization.kt`: 정상 상태 안내는 `휴식 습관이 좋습니다.`로 축약했다. 앱 자체의 홈·설정·동의 안내·오류 메시지에서 `Health Connect` 대신 건강 기록/건강 데이터 연동 표현을 사용한다. 한국어·영어·일본어·스페인어에 반영했다. 공유 문구의 고지, 데이터 이용 목적·기기 내 처리·권한 해제 설명은 유지했다. Android 자체 권한 화면의 명칭은 변경하지 않았다.
- `GuidedSessionViewModel.kt`: 정상 읽기 후 기록이 없는 `EMPTY` 상태는 추가 안내 문구를 내보내지 않는다. 연동 상태, 기본 계산, 실제 읽기 실패·권한 미허용 안내는 유지한다. 사용하지 않는 `health_data_no_records` 문자열은 4개 언어에서 삭제했다.
- `WellnessHome.kt`: 세 버튼 Column을 가로 중앙 정렬하고 텍스트 9개에 `TextAlign.Center`를 적용했다. 기존 아이콘 18dp, 글자 10/11/9sp, 내부 여백 10dp와 간격 2dp는 그대로다. 계산·권한·클릭 동작은 변경하지 않았다.
- 실제 검증: `:app:testDebugUnitTest --tests '*AppLocalizationTest' --tests '*EyeBatteryCalculatorTest' :app:assembleDebug` 성공. 21:32 실행 XML 기준 **16/16**(문구·번역 3, 배터리·건강 요약 13), 실패·오류·skip 0. `AppLocalizationTest`에 짧은 안내, 기술 명칭·기록 없음 문구 제거 및 오류 문구 유지 회귀 검증을 추가했다. 세 버튼 중앙 정렬·기존 치수 정적 확인과 `git diff --check` 통과.
- **NOT RUN:** 에뮬레이터/실기기 시각 검수, 건강 실데이터, S23 설치. 폰은 21:24 설치본을 유지한다. commit/push 및 Release/Play 배포 없음; Release Hold 유지.
<a id="pursuit-planet-size"></a>

## 2026-11-20 — 시선 추적 행성·위성 크기 절반

- 승인: 눈동자 훈련 탭 시선 추적 4종의 가운데 행성(지구·목성·토성·금성)과 위성(달) 크기를 절반으로 줄이고, 움직이는 반경은 유지한다.
- `SatelliteOrbitCanvas.kt`: 행성 반지름 `minDimension * 0.155f → 0.0775f`, 위성 반지름 `0.03f → 0.015f`. 토성 고리와 행성 표면 무늬·하이라이트는 반지름에 비례하므로 함께 축소된다. 위성 궤도 위치·궤도선은 `SatelliteOrbitGeometry.project`가 캔버스 크기 기준으로 계산하므로 변경하지 않았다(이동 반경·속도·앞뒤 가림 유지). 궤도선 두께와 배경 별은 그대로다.
- 실제 검증: `:app:testDebugUnitTest --tests '*SatelliteOrbitGeometryTest' :app:assembleDebug` 성공. 21:36 실행 XML 기준 **4/4**, 실패·오류·skip 0. `git diff --check` 통과.
- **NOT RUN:** 에뮬레이터/실기기 시각 검수, `UiFollowupTest` 재실행, S23 설치. 폰은 21:24 설치본을 유지한다(홈 문구·정렬 수정본도 미설치). commit/push 및 Release/Play 배포 없음; Release Hold 유지.
<a id="home-card-font-mockup"></a>

## 2026-11-20 — 홈 배터리 카드 글자 크기 목업 버전 복원

- 승인: 카드 안 글자 크기만 21:11 목업 설치본(`WellnessHome.kt` SHA-256 `b6e6362f…`, 세션 기록에서 해시 일치로 복구해 비교) 기준으로 되돌린다. 배치·여백·아이콘·카드 구성은 현재 상태를 유지한다.
- `WellnessHome.kt` 글자 크기 변경(현재 → 목업): 상태 칩 bodySmall 12sp → labelLarge 14sp, 잔량 % titleMedium 16sp → headlineSmall 24sp, 게이지 범례 9 → 11sp, 건강 메시지 본문 bodySmall 12sp → bodyMedium 14sp(보조 안내 줄은 12sp 유지), 연동 문구 labelSmall 11sp → bodySmall 12sp, 공유 11 → 13sp. 휴식·원근 조절·시선 추적 버튼은 라벨/값/효과를 10/11/9sp → 12/16/11sp로 하고, 목업과 같이 한 줄 유지 + 긴 번역 자동 축소(최소 8/9/8sp)를 적용했다. 카드 제목(titleMedium), 자세 배지(12sp), 권한 배너 글자는 변경하지 않았다. 계산·권한·클릭 동작 변경 없음.
- 실제 검증: `:app:assembleDebug :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --tests '*AppLocalizationTest' --tests '*EyeBatteryCalculatorTest'` 성공. 21:55 실행 XML 기준 **16/16**, 실패·오류·skip 0. 편집 전 파일과 `dp` 값 전체 일치(레이아웃 치수 불변) 확인, `git diff --check` 통과.
- **NOT RUN:** 에뮬레이터/실기기 시각 검수(4개 언어 줄임·높이), AndroidTest 실행, S23 설치. commit/push 및 Release/Play 배포 없음; Release Hold 유지.