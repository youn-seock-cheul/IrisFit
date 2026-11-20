# IrisFit 베타 MVP/UI QA 재검수 — 2026-11-19

- 범위: 기존 `ea23d44` 베타 MVP/UI 변경 및 현재 UI 후속 작업. 센서 lifecycle 구현은 별도 QA 범위다.
- 판정: **PARTIAL FAIL — 베타 범위 조건 불충족; Release Hold 유지**
- 제품 코드 변경: 없음. 오래된 화면 선택자/초기화 경로 때문에 실행되지 않던 휴식 UI 테스트만 현재 내비게이션과 지역화 키에 맞춰 수정했다.
- 기기: Galaxy S23 (`SM-S916N`, Android 16/API 36), 격리 Android 17/API 37 에뮬레이터.

## 판정 및 결함

| ID | 요구사항 | 판정 | 근거 |
|---|---|---|---|
| QA-BETA-01 | `IRIS-REQ-BETA-SCOPE-001` | **FAIL** | 홈에 친구 응원 릴레이와 공개 글로벌 릴레이 카드가 계속 노출되고 각 CTA가 베타 안내창을 연다 (`MainScreen.kt:746-747`, `PublicGlobalChallengeCard.kt`). 하단 AI 탭도 계속 보이며 탭하면 같은 안내창을 연다 (`MainScreen.kt:116`). 실제 대화·동기화 진입은 막혀 있지만, 승인된 기준은 데모/미구현 진입점 자체를 숨기는 것이다. |
| QA-BETA-02 | `IRIS-REQ-SCORE-001` | **BLOCKED / FAIL** | 홈 배터리 점수는 `ScreenTimeManager.queryTodayScreenTime()`의 전체 앱 UsageStats 전경시간을 사용하고, `EyeBatteryCalculator`가 15분당 차감 및 휴식/운동별 회복을 적용한다 (`GuidedSessionViewModel.kt:116`, `ScreenTimeManager.kt:43-78`, `EyeBatteryCalculator.kt:48-55`). 이는 사용자가 승인한 실제 손에 들고 보는 시간과 다르며 산식·표시·전력 기준도 승인되지 않았다. 실제 시청시간 지표로는 수용할 수 없다. |
| QA-BETA-03 | `IRIS-REQ-SESSION-001` | **PASS (제한 범위)** | 에뮬레이터에서 휴식 시작→30초 후 완료 확인→확인 전 무기록→완료 기록 저장→Activity 재생성 후 완료 상태 표시→기기 기록 삭제를 통과했다. 백그라운드 진입 시 일시정지, 복귀 후 수동 재개, 중단 시 미저장 경로도 통과했다. |
| QA-BETA-04 | `IRIS-REQ-EXERCISE-001` | **PARTIAL PASS** | 운동 안내 겹침 4개 모드, 원근 조절 중단/재시작, 시선 추적 위성 Canvas·행성 라벨·재시작을 UI 테스트로 확인했다. 운동을 끝까지 수행하고 완료 이력을 저장하는 전체 경로와 모든 궤도의 수동 시각 검수는 미실시다. |
| QA-BETA-05 | `IRIS-REQ-RECORD-001` | **PARTIAL PASS** | 휴식 완료 이력 저장·재생성 복원·전체 삭제를 격리 에뮬레이터에서 확인했다. 다른 설정/레거시 데이터의 삭제 범위와 복구 경계는 이번 검수에서 확인하지 않았다. |

릴레이/AI UI는 준비 안내만 보여 주므로 현재 소셜 기능의 동기화·응원 흐름은 제공하지 않는다. 다만 `IRIS-REQ-BETA-SCOPE-001` 위반은 안내창으로 대체해도 해소되지 않는다. 실제 제품 범위/표현을 바꾸지는 않았으며, 노출 제거는 기존 승인 범위에 맞는 후속 수정 대상이다.

## 검증 증거

- 전체 Debug JVM: **103 tests / 0 failures / 0 errors / 0 skipped**.
- 베타 MVP 전용 `IrisFitBetaMvpQaTest`: **4 / 0 / 0 / 0**. 미측정 상태, 완료 중복 방지, 1,505건 레코드 저장/복원·집계, UUID 생성을 확인했다. 이 테스트는 비즈니스 로직 테스트이며 UI/제품 산식 승인 증거는 아니다.
- `UiFollowupTest`, 에뮬레이터: **7 tests OK**. 색톤 저장·Activity 재생성, 자가 확인 미응답/선택 응답·복원/초기화, 4개 원근 조절 모드의 안내 위치, 위성 화면·행성 라벨·훈련 재시작을 확인했다.
- 동일 UI 테스트, Galaxy S23: **6 pass, 1 timeout**. 자가 확인 응답 테스트가 `performScrollTo()` 중 180초 제한에 걸렸다. 같은 테스트를 에뮬레이터에서 단독 실행했을 때는 통과했다. 실기기 결과는 불안정으로 남긴다.
- `GuidedSessionUiTest`의 휴식 전체 흐름과 회전/백그라운드 경로를 현재 UI 선택자로 보정한 뒤 격리 에뮬레이터에서 각각 통과했다. 수정 파일은 `app/src/androidTest/java/com/example/myapplication/GuidedSessionUiTest.kt`다. 과거 전체 클래스 실행은 오래된 마커 진입 및 화면 문자열 선택자로 실패했으며, 전체 클래스를 수정 후 재실행하지 않았다.
- Debug/AndroidTest APK 조립, `compileReleaseKotlin`, Release boundary 검증, `git diff --check`: **PASS**. 서명된 Release APK 생성·배포는 하지 않았다.

## 미검증 및 다음 게이트

- 모든 현지화/테마 조합의 수동 화면 검수, 모든 훈련의 완료 저장 경로, AndroidTest 전체 suite는 **NOT RUN**.
- 정확도, 실제 시청시간, 배터리·발열·전력 및 Play 정책/배포 검증은 **NOT RUN**.
- 다음 QA 게이트: 베타 홈/탐색에서 릴레이·AI/PRO 진입점을 제거한 빌드 재검수, 사용량/배터리 측정 기준의 PM 승인 후 지표 재검수, 잔여 UI 흐름 및 전체 suite 확인.
- 배포 승인 없음. **Release Hold 유지.**
