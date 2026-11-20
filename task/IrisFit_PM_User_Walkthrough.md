# IrisFit Walkthrough — PM 로드맵 및 개발 이력 통합본

- 문서 관리자: 수석 PM
- 기준 파일 위치: `C:\Users\youns\AndroidStudioProjects\IrisFit_LabA\Walkthrough.md` (저장소 진입점) 및 본 통합 문서
- 기준일: 2026-11-16
- 문서 상태: **공동 진행 기준 — 실제 시청시간 검증 작업 재개방, Release Hold**
- 현재 출시 상태: **Release Hold**

## 이 문서의 목적과 기준

이 문서는 사용자와 PM이 IrisFit의 제품 로드맵, 현재 상태, 주요 구현 이력, QA 증거, 결정 및 다음 단계를 함께 확인하는 **통합 Walkthrough 기준**이다.

저장소 루트 `Walkthrough.md`는 이 통합 문서로 연결되는 진입점이다. 제품 로드맵과 현재 상태는 여기에서, 개발·QA의 자세한 근거는 연결된 작업 보고서 및 문서 인덱스에서 확인한다. 오래된 기술 메모는 이력으로 보존하되 최신 검증 상태와 혼동하지 않는다.

## 진행 지도

**현재 상태 감사 → 베타 범위·수용 기준 승인 → 개발·자체 검증 → QA 검수 → PM 완료/고정 판정 → 내부 테스트 승인·실행 → 제한 베타 → 정식 출시 심의·별도 승인 → 출시 후 개선**

단계를 건너뛰거나 자동 승인하지 않는다. 각 단계가 끝나면 PM이 결과와 다음 선택을 이 파일에 갱신한다. 출시까지 시간이 걸리거나 막히는 항목이 있으면 이유와 필요한 결정을 함께 표시한다.

**현재 로드맵 위치:** 실제 시청시간 목표(Android 13/API 33 이상, 측정 불가 시 `미측정`)는 유지한다. 후속 사용자 요청으로 UsageStats 화면 사용 기반 **추정치**를 제공하고, Health Connect 수면/활동 연동은 선택 기능(기본 꺼짐)으로 추가했다. UsageStats 추정치는 실제 시선/시청시간 측정값이 아니다. 최신 자동 검증은 JVM 106/106, 격리 Android 17 전체 AndroidTest 19/19, Usage Access 허용/거부 흐름 각 1/1이다. Health Connect 실데이터, S23 정확도, 수동 시각 검수, 전력 및 Play 검증은 미완료라 제품 전체 QA는 **PARTIAL PASS**. 상세: [배터리 추정·Health Connect 기록](IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md). Release Hold 유지.

**2026-11-19 센서 검증 추가:** 위 R5 기록은 당시 기준선이다. 후속 수정에서 실제 `SensorManager` 접근 경계를 주입 가능하게 만들고 세션별 callback generation guard를 추가했다. 등록 실패/이전 세션의 늦은 이벤트 JVM 테스트 29개와 Galaxy S23 실제 센서 callback/stop 기기 테스트 1개가 통과했으며 Debug·AndroidTest APK 조립 및 Release Kotlin 컴파일도 통과했다. 하드웨어 등록 실패를 물리 기기에서 강제하거나 자세/실제 시청시간 정확도 및 전력을 측정한 것은 아니다. 상세: [센서 생명주기 검증 기록](IrisFit_Sensor_Lifecycle_Verification_2026-11-19.md). 기존 Release Hold는 유지한다.

## 한눈에 보는 진행 현황 — 2026-11-16

| 영역 | 현재 상태 | 결과/남은 기준 |
|---|---|---|
| 제품 목표 | **결정됨** | 목표는 실제로 손에 들고 보는 시간. Android 13+ 대상, 측정할 수 없으면 `미측정`; UsageStats는 실제 시청시간의 대체값이 아님 |
| 현황 감사 | **완료 — 정적 범위** | 소스/권한/데이터 경로를 확인하고 QA가 재검토. 정책/Play 및 전체 기기 검증은 미완료 |
| 코드 및 자동 테스트 | **부분 완료** | 기존 `aec48fd` 기준 JVM 82개. 후속 센서/evaluator JVM 29개 및 기기 instrumented lifecycle test 1개 통과; 정확도/전력 및 별도 beta UI QA 잔여 |
| 실기기 기본 확인 | **제한 완료** | Galaxy S23+ `SM-S916N`, Android 16/API 36에서 Debug 설치·실행 및 기본 UI 확인. 자세 배지와 UsageStats 숫자는 표시 확인이지 정확도 측정이 아님 |
| 미측정 처리 | **부분 완료** | relay Toast/Wellness 아이콘을 포함한 코드 경로 중립화. UI 렌더링 자동 검증은 없음 |
| 센서 freshness | **생명주기 검증 완료(제한 범위)** | JVM 등록 실패/늦은 callback 경계와 S23 실제 등록·callback·stop 무효화 통과. 자세/시청시간 정확도, 다른 기기 범용성은 미검증 |
| 기기 프로필 | **코드 확인 완료** | `SM-S916N`/S916 계열이 `GALAXY_S23`으로 분류됨; 모델 단위 테스트 1건 통과 |
| 시청 정확도/배터리 | **NOT RUN** | 정답 라벨 기반 정확도와 배터리 비교 미실시. 허용 기준/시험 절차도 사전 승인 필요 |
| 출시 | **BLOCKED** | 내부 테스트 업로드·베타·Production 배포 미승인; Release Hold 유지 |

**다음 순서:** (1) 미측정 소비처·센서 검증·S23+ 매핑 수정과 경계 테스트 → (2) QA 재검수 → (3) PM 완료 판정 → (4) 사전 승인된 실제 기기 정확도/배터리 시험 → (5) 내부 테스트와 출시를 각각 별도 승인. 실기기에서 확인된 `100%`, `2분`, `책상 위 놓임`은 앱 화면 결과이며 실제 시청시간 정확도 증거로 해석하지 않는다.

각 기능의 요구사항 ID, PM 기본설계/QA 협의 승인, Developer 상세설계, 개발 작업, QA 테스트 및 PM 완료 판정은 `IrisFit_PM_Dev_QA_Requirements_Process.md`의 공통 추적 절차를 따른다. 이 안내서는 전체 흐름과 현재 위치를 보여주고, 상세 문서는 요구사항 ID로 연결한다.

### 전체 제품 로드맵 및 현재 진행

| 단계 | 목적 및 종료 기준 | 현재 상태 |
|---|---|---|
| 1. 현황 감사 | 실제 소스/Release 구성과 위험을 조사하고 QA가 근거를 재검토 | **완료(정적 범위)** — 실제 기기/Play 검증은 미완료 |
| 2. 제품 범위·기준 승인 | 베타 핵심 범위, 데이터 의미, 예외/정책 및 측정 가능한 완료 기준을 PM이 승인 | **부분 완료** — 베타 MVP 범위와 실제 시청시간 목표, Android 13+ 및 `미측정` 원칙 결정. 정확도·전력 기준/시험 절차 미승인 |
| 3. 개발 및 자체 검증 | 승인된 요구사항만 구현하고 기준별 증거/잔여 위험을 인계 | **후속 테스트 제출** — `BATCH-UNMEASURED-01`, `aec48fd`; JVM 82/0/0; 미등록/지연 callback 잔여 |
| 4. QA 검증 | PM 완료 기준별 자동/수동 시험을 수행하고 증거와 PASS/FAIL/BLOCKED/NOT RUN 보고 | **PARTIAL PASS** — 베타 UI 수정, 운동 저장 흐름 및 자동 suite PASS. 수동 시각 검수·실기기 정확도/전력 검증 대기 |
| 5. PM 완료·고정 판정 | QA 결과·기기 범위·잔여 위험을 검토해 완료/보류 판정 | **보류** — 제품 기능 완료/고정 미승인; 후속 수정 후 재검수 필요 |
| 6. 내부 테스트 | Release 패키지 설치 및 핵심 흐름을 실제 테스트, PM/소유자가 트랙 업로드 승인 | **미착수/미승인** — Release Hold 유지 |
| 7. 제한 베타 | 승인된 테스터와 기간/표본으로 사용성·안정성·피드백 검증 | **미착수** — 내부 테스트 종료 및 PM 승인 필요 |
| 8. 정식 출시 심의 | 베타 결과, Play 정책/스토어 자료, 운영 위험을 검토하고 공개 여부 별도 승인 | **미착수** — 자동 승인 없음 |
| 9. 출시 후 개선 | 리뷰·오류·지표를 개인정보 보호 원칙에 따라 분석하고 새 작업으로 관리 | **미착수** |

단계 3–5가 끝나도 실제 시청시간 측정/전력 타당성은 별도 검증 게이트다. 실제 기기 시험 전에는 해당 수치의 정확도·전력 효율을 PASS로 표시하지 않는다.

## 주요 개발 Walkthrough 이력

### 2026-11-19 — 베타 MVP/UI 재검수

`ea23d44` 및 현재 UI를 별도 범위로 검수했다. 전체 JVM 103/0/0, `IrisFitBetaMvpQaTest` 4/0/0, 격리 에뮬레이터의 `UiFollowupTest` 7/7, 휴식 완료·삭제 및 회전/백그라운드 흐름 2/2가 통과했다. Galaxy S23 UI 재검수는 6/7 통과, 자가 확인 응답 케이스 1개가 timeout되어 불안정으로 기록했다. 베타 MVP는 홈의 친구/공개 릴레이 카드와 AI 내비게이션을 노출하고 있어 명시된 숨김 기준을 만족하지 않는다. 또한 홈의 배터리 점수는 UsageStats의 앱 전경시간 기반이며 승인된 실제 시청시간 측정값이 아니다. 판정은 **PARTIAL FAIL**, 제품 코드/출시 범위는 변경하지 않았고 Release Hold를 유지한다. 상세: [베타 MVP/UI QA 재검수](IrisFit_Beta_MVP_UI_QA_Review_2026-11-19.md).

### 2026-11-20 — 베타 MVP/UI 후속 수정 및 재검증

홈 릴레이 카드와 AI 하단 탭을 숨겼으며, 점수·추세·위젯은 실제 시청시간 evaluator 연결 전까지 UsageStats를 점수로 변환하지 않고 `UNMEASURED` 상태를 유지한다. `testDebugUnitTest` 103/103, Debug/AndroidTest APK 조립, Release Kotlin compile 및 boundary 검사 통과. 새 베타 홈 회귀 테스트는 Galaxy S23 1/1, 전체 `UiFollowupTest`는 격리 Android 17 에뮬레이터 8/8 통과했다. 같은 전체 테스트 클래스의 S23 실행은 self-check timeout 후 Compose hierarchy 오류로 불안정(2 pass/6 fail)이었다. 따라서 두 수정 대상은 PASS이나 전체 QA는 **PARTIAL PASS**이며 운동 저장 흐름/전체 AndroidTest/수동 locale-theme/실제 accuracy·전력은 미완료 또는 NOT RUN이다. 상세: [후속 QA 기록](IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md).

잔여 게이트 재검증 결과: `testDebugUnitTest --rerun-tasks` **103/103**, 격리 `Medium_Phone_API_37.0` Android 17에서 `connectedDebugAndroidTest` **18/18** 통과. 그중 `UiFollowupTest` 10/10은 원근 조절·시선 추적 기록 저장과 4개 언어 × 5개 색톤의 자동 UI smoke를 포함한다. 수동 시각 검수나 S23 전체 클래스는 이번에 실행하지 않았고, marker demo도 현재 production beta 화면에서 접근 경로를 확인하지 못해 NOT RUN이다. 실기기 정확도·전력·Play 검증도 NOT RUN이므로 전체 QA PARTIAL PASS와 Release Hold는 유지한다. 세부 결과와 테스트 변경: [후속 QA 기록](IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md).

### 2026-11-20 — 눈 배터리 화면 사용 추정 및 선택형 Health Connect

사용자가 UsageStats를 통한 화면 사용 기반 점수 계산과 Health Connect를 설정에서 선택하는 기능을 요청했다. 승인된 범위로 오늘 앱 전경 사용시간을 기반으로 야간/앱 카테고리 가중치와 기존 완료 기록을 계산에 연결하고, Health Connect의 수면·걸음·등산 데이터 읽기는 기본 꺼짐의 선택 기능으로 추가했다. 권한 거부/조회 실패는 `미측정`으로 구분하며 홈·설정의 다국어 안내에 실제 시선/피로 측정이 아님과 활동 데이터가 자연광 측정이 아님을 명시했다. 이는 실제 시청시간 목표나 정확도 기준을 대체/충족하지 않는다.

최종 작업 트리에서 JVM **106/106**, 격리 Android 17 전체 AndroidTest **19/19**, Usage Access 허용/거부 경로 각각 **1/1**, Release 컴파일 및 `verifyReleaseBoundary`가 통과했다. 이후 사용자 요청으로 Galaxy S23(Android 16)에 Debug APK를 로컬 설치하고 앱 시작을 확인했다. Health Connect 실데이터, 사용량 정확도, 전력·발열, 수동 UI 및 Play 검증은 **NOT RUN**이다. 외부 배포/Play 업로드는 하지 않았고 Release Hold 유지. 상세: [기능 검증](IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md), [폰 설치](IrisFit_Phone_Debug_Install_2026-11-20.md).

후속 승인으로 홈 카드 여백을 줄이고 산식 토글을 정보 아이콘·화살표 pill로 바꿨다. Health Connect 실제 데이터 읽기가 완료되면 수면 시간/깊은 수면 비율과 기존 산식의 시작 잔량, 걸음·등산 활동 보너스를 안내한다. 계산 산식은 변경하지 않았다. 선택 JVM 테스트 15/15, Android 17 격리 AVD 전체 AndroidTest 19/19, Release 컴파일·boundary 검증 및 Debug APK 조립 통과 후 Galaxy S23(Android 16)에 업데이트 설치하고 앱 실행을 확인했다. 실기기 Health Connect 데이터·권한, 정확도·전력은 NOT RUN이며 Release Hold 유지. 상세: [구현·검증](IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md), [설치 및 APK 해시](IrisFit_Phone_Debug_Install_2026-11-20.md).

사용자 요청에 따라 3단계 상태 구간과 저잔량 응원 매칭 변경을 취소하고, 홈·위젯을 기존 4단계(위험 <45%, 경고 45–64%, 주의 65–84%, 최상 85%+)로 복원했다. 점수·Health Connect 산식은 유지했다. 전체 JVM 108/108 통과 후 Galaxy S23에 Debug 업데이트 설치했고, 실기기 홈의 4단계 범례 표시를 확인했다. 격리 AVD AndroidTest, 실기기 Health Connect·정확도·전력 검증은 NOT RUN이며 Release Hold를 유지한다. 상세: [설치 기록](IrisFit_Phone_Debug_Install_2026-11-20.md).

### 2026-11-18 — UI 통일 및 승인된 후속 개발

용어·설정 목록·공통 카드/버튼·Material Symbols 아이콘 개편 후, 사용자가 실제 데이터 표시 → 비의료 표현 → 훈련 겹침 → 홈 팁 → 제목 대비 → 번역 순서의 보완을 승인했다. 5종 색톤 선택과 영구 개발 기록도 요청했다. 2026-11-19에는 원근 조절/시선 추적 훈련의 중단·뒤로가기 후 재시작 결함을 수정하고, 빠른 점멸·파동 리플과 4가지 3D 위성 궤도를 구현했다. 네 모드를 지구·목성·토성·금성에 매핑하고, 패널 없는 작은 자연 위성 및 행성 표면 무늬의 자전을 추가했다. 이후 사용자 피드백에 따라 급격한 방향 전환이 있던 지그재그를 부드러운 S자 궤도로 교체했다. 관련 JVM 테스트, Debug/AndroidTest APK 조립 및 Galaxy S23(Android 16) UI 테스트가 통과했다. 상세 근거는 [후속 개발 기록](IrisFit_UI_Followup_2026-11-18.md)에 보존한다. 기존 Release Hold 및 센서 정확도/전력 미검증 상태는 그대로다.

아래는 기존 저장소 `Walkthrough.md`에 있던 구현 이력과 이후 제품 검토 이력을 이 문서에 합친 요약이다. 상세 코드 경로와 테스트 로그는 연결된 개발 보고서에서 확인한다. 이전 단계의 확인 결과가 현재 HEAD나 현재 Release 상태를 보장하지 않는다.

### 2026-11-12 — 위젯 단일화 및 Release 컴파일 수정 이력

- Widget 1만 유지하도록 Provider/Manifest/리소스와 기록 갱신 경로를 정리했다.
- 당시 중간 조치로 `BlueLightFilterService`의 컴파일 문제를 수정했으나, 다음 날 서비스 자체를 제거했으므로 해당 수정은 최종 설계가 아니다.
- 기존 기록에 따르면 이 중간 단계는 정적 확인만 있었고 Release 빌드·단위 테스트·Lint·실기기 검증은 미실행이었다.

### 2026-11-13 — 앱 자체 블루라이트 오버레이 제거 및 온디바이스 코치 기록

- 앱 자체 블루라이트 오버레이를 제거하고 Android 디스플레이 설정 바로가기를 유지했다. 오해를 줄이는 설명으로 수정하고, 눈 운동 오버레이에 필요한 권한은 별도 근거로 유지했다.
- 기존 인계 기록은 사용자가 직전 수정본 Release 빌드 성공을 확인했다고 적었으나, 이번 변경분의 Release 빌드/테스트/Lint는 별도 실행하지 않았다고 명시했다.
- Gemini Nano 온디바이스 코치의 기능 및 기기별 지원·다운로드·실기기 확인 필요성을 기록했다. 이전 기록도 해당 ZIP에서 테스트/Lint/Release 빌드를 재실행해야 한다고 제한했다.

### 2026-11-15 — 실제 시청시간 추정 기능 검토 및 재개방

- 목표를 “앱 사용 시간”이 아니라 실제로 손에 들고 화면을 보는 시간으로 정의했다. UsageStats는 앱 전경 사용을 집계할 뿐 실제 시선을 입증하지 않는다는 점을 제품 기준으로 명시했다.
- API 33 이상을 목표로 하고 측정 불가 시 `미측정`을 표시하기로 했다. Galaxy S23/Z Fold7은 초기 시험 후보이며 전체 Android 호환성의 증거가 아니다.
- Developer commit `3218622`는 59개 JVM 테스트 통과를 보고했고, 후속 `66aa763`는 61개 테스트를 통과했다. PM 검토에서 시스템 조회 오류와 실제 0분 구분, 센서 freshness 및 하드웨어 증거의 공백을 확인해 PARTIAL PASS 및 작업 재개방으로 기록했다.
- commit `5018854`는 typed `ScreenTimeQueryResult`를 추가하고 evaluator public API에서 mock 입력을 분리했다. 그러나 정수 helper가 조회 실패를 0으로 바꾸며 운영 호출처에서 사용되는 점을 PM이 지적했다.
- commit `aad01ee`는 센서 등록/freshness 신호를 evaluator에 연결했고, 61개 JVM 단위 테스트가 통과했다. PM 재검토에서는 실패-0 운영 경로가 남아 있고 최초 센서 이벤트 전에 freshness가 참일 수 있음을 확인했다. 최종 판정은 **PARTIAL PASS**, 관련 수정/QA 재검수 대기다.
- commit `b808392`는 최초 이벤트 전 timestamp를 0으로 초기화하고 typed 결과 처리를 일부 운영 경로에 확장했다. 전체 61개 JVM 테스트 통과를 확인했으나 PM 재검토에서 Manifest에 등록된 실제 Widget 1 경로와 `MainScreen.AiCoachTab`에 실패-0 API가 잔존함을 확인했다. 미측정 상태에서도 점수가 산출되며, 센서 이벤트 검증 전에 freshness가 갱신될 수 있다. 판정은 **PARTIAL PASS**, 후속 수정/재검수 대기다.
- commit `8a72672`는 활성 Widget 1 및 AI 코치의 typed query 처리, 최초 센서 이벤트 guard, 잘못된 timestamp 및 unreliable accuracy 처리의 freshness 무효화를 보완했다. 전체 JVM 테스트 61/0/0은 통과했지만, 미측정 score/status는 여전히 표시될 수 있고 센서 payload 검증 전에 freshness가 갱신될 수 있다. 실제 detector/UI 경계 전용 테스트도 확인되지 않아 PM 판정은 **PARTIAL PASS**, 후속 수정/QA 재검수 대기다.
- commit `9dd7059`는 calculator의 미측정 결과를 `-1`/`UNMEASURED`로 분리하고 단위 테스트를 추가했다. 전체 JVM 테스트 61/0/0을 재실행해 확인했으나 위젯 두 렌더 경로와 AI 코치·공유·추세 소비처가 -1을 점수처럼 노출하거나 계산에 사용한다. 센서 timestamp 갱신은 payload 검증 전이라 freshness 거짓 양성도 남는다. PM 판정은 **PARTIAL PASS**, 수정 및 QA 재검수 대기다. 상세 지시: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md`.
- 2026-11-15 실기기 스모크: Galaxy S23 (SM-S916N, Android 16/API 36)에 설치된 앱(version 1.0.0, targetSdk 36)을 실행해 foreground 시작을 확인했다. 가속도계·자이로·근접·조도 센서가 OS 센서 목록에 노출됐다. 이는 센서 가용성 확인이지 IrisFit freshness 콜백/시청 정확도 테스트는 아니다. USB 충전 중이므로 배터리 시험은 **NOT RUN**. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Device_Smoke_Check_2026-11-15.md`.
- commit `6d2b718` Developer/QA 제출을 재검토했다. 61개 JVM 단위 테스트를 재실행해 통과를 확인했지만 커밋에 테스트 수정은 없다. 실기기에서는 화면 사용시간/점수/자세 배지 표시와 AI Coach 베타 안내 다이얼로그를 관찰했다. 이는 UsageStats 결과나 자세 분류의 정확도 검증이 아니며, 현재 자세 캡처는 `책상 위 놓임`이었다. bitmap 위젯, AI/목표/추세 소비처의 미측정 처리와 센서별 malformed/range payload freshness 문제가 남아 PM 판정 **PARTIAL PASS**. USB 충전 중 배터리 시험 및 실제 시청 정확도는 **NOT RUN**. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md`.
- commit `d301df4` 및 현재 Debug 설치를 확인했다. 62개 JVM 테스트는 통과했다. bitmap Widget 1과 공유의 미측정 표시는 개선됐지만 AI 코치/목표/게이지/추세는 여전히 -1을 표시·비교할 수 있다. 센서 최소 배열 길이/finite 및 일부 값 검사가 추가됐으나 proximity `maximumRange`는 freshness 갱신 전에 검사되지 않으며 unknown sensor type도 freshness를 갱신할 수 있다. `SM-S916N`은 현재 S23 프로필 감지 조건에 없어 generic으로 분류된다. 실기기 홈에는 `책상 위 놓임`, `100%`, `2분`이 표시됐지만 실제 시청/자세 정확도 증거가 아니다. 배터리 시험은 충전 중이라 **NOT RUN**. 판정 **PARTIAL PASS**. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_d301df4_2026-11-16.md`.
- commit `311ab4c` 및 현재 Debug 빌드를 재검증했다. Gradle XML 기준 JVM 테스트 **63/0/0**. 모델 탐지에 S916/S918 패턴이 추가됐고 단위 테스트로 `SM-S916N`→`GALAXY_S23`을 확인했다. 센서 callback은 유한 payload, 가속도계/자이로 축 수, 음수 light/proximity, finite 양수 proximity maximumRange, timestamp 및 알려진 type을 확인한 뒤 freshness를 갱신한다. 다만 proximity 샘플 값이 maximumRange를 넘는 경우는 막지 않으며 detector 이벤트 테스트가 없다. AI Coach/Goal은 점수 `-1`을 `%`로 표시할 수 있고 Wellness 게이지는 -1을 0으로 clamp, trend도 미측정값을 유효한 값처럼 처리할 수 있다. Galaxy S23+ Debug 앱 실행 및 기본 홈 UI를 확인했지만 `99%`, `15분`, `책상 위 놓임`은 UI 관찰이며 실제 시청/자세 정확도는 아니다. 충전 중이라 배터리 비교는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_311ab4c_2026-11-16.md`.
- commit `59bb238`를 검토하고 JVM XML **63/0/0**을 확인했다. Proximity `values[0] <= maximumRange` guard는 freshness 갱신 전에 적용된다. `BatteryTrendCard`는 unmeasured flag를 calculator에 전달하지만, 결과 `-1`을 그래프 하한으로 plot하고 라벨에 `-1%`를 표시할 수 있어 누출 차단은 미완료다. MainScreen AI/Goal/relay와 Wellness gauge/legend에도 미측정 처리가 남아 있다. Debug APK 설치와 Activity 시작은 성공했으나 이번 UI dump에서 점수/홈 내용을 검증하지 않았다. 정확도 및 배터리 시험은 별도 승인된 절차가 없어 **NOT RUN**. 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_59bb238_2026-11-16.md`.
- commit `efcd86b`를 검토하고 JVM XML **63/0/0**을 확인했다. 일간 `DailyStockChartView`에 미측정 placeholder와 음수 하단 라벨 처리가 추가되어 해당 코드 경로의 개선은 수용한다. 그러나 주간/월간 차트가 휴식/운동 기록만으로 임의 점수를 생성하고, MainScreen AI/Goal/relay 및 Wellness gauge/legend에도 미측정 소비가 남아 전체 trend/UI 처리는 미완료다. 커밋에 UI/sensor 경계 테스트는 추가되지 않았으며 QA 렌더링 PASS를 자동 테스트 증거로 확인하지 못했다. Debug 설치/Activity 실행은 확인, 실제 viewing/posture accuracy와 battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_efcd86b_2026-11-16.md`.
- commit `ce6d320`를 검토하고 JVM XML **63/0/0**을 재확인했다. 모든 trend 기간의 본문에 전역 미측정 placeholder가 적용되어, 현재 score 자체가 미측정이면 임의 bar/line은 숨겨진다. 하지만 현재 전역 measured 상태가 주간/월간 각 구간의 실제 측정을 의미하지 않으며, 해당 구간은 휴식/운동 기록으로 만든 가짜 점수가 계속 표시된다. MainScreen AI/Goal/relay 및 Wellness gauge/legend의 미측정 소비도 남았다. UI/sensor 경계 테스트가 없어 QA의 화면별 PASS는 독립 검증되지 않았다. 시청/자세 정확도와 배터리 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce6d320_2026-11-16.md`.
- commit `25fea7e`를 검토하고 JVM XML **63/0/0**을 확인했다. 기록이 없는 과거 주간 날짜는 `-` 및 중립 바로 표시되지만, 기록이 있는 날짜는 `screenTimeMinutes=120` 가정을 사용해 계산한다. 월간 탭에는 `70 + rests*15 + miniGames*25` 임의 점수 공식이 남았다. MainScreen AI/Goal/relay 및 Wellness gauge/legend 미측정 consumer와 UI/sensor 경계 테스트도 미완료다. QA의 월간 공식 제거 주장은 소스와 일치하지 않는다. 실제 accuracy/battery는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_25fea7e_2026-11-16.md`.
- commit `44c1606`를 검토하고 JVM XML **63/0/0**을 재확인했다. 주간/월간 activity-count 임의 산식은 제거됐으며 현재/이번 주가 아닌 과거 구간은 `-1` placeholder로 처리된다. 즉 과거 측정 데이터 source가 아직 없어 실제 추세값은 제공되지 않는다. MainScreen AI/Goal/relay와 Wellness gauge/legend의 미측정 소비 및 UI/sensor 경계 테스트가 남는다. viewing/posture accuracy와 battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_44c1606_2026-11-16.md`.
- commit `f05cb53`를 검토하고 JVM XML **63/0/0**을 확인했다. GoalAchievementCard와 AiCoachTab에 measured/score guard가 추가됐다. 그러나 relay는 `-1`을 위험 점수로 분기하고 Toast에 출력할 수 있으며, Wellness gauge는 -1을 0으로 clamp하고 danger legend를 강조할 수 있다. UI 변경의 자동화 경계 테스트는 추가되지 않았다. actual viewing/posture accuracy, sensor freshness, battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f05cb53_2026-11-16.md`.
- commit `23ee202`를 검토하고 JVM XML **63/0/0**을 재확인했다. Relay는 미측정/음수 점수에서 score 기반 위험 추천과 점수 Toast를 피하고 일반 주제를 사용하며, Wellness gauge/범례는 미측정 상태에서 중립 처리한다. Goal/AiCoach guard도 유지된다. 다만 Compose UI 렌더링 및 sensor callback/freshness 경계 테스트는 없고, relay fallback 문구는 눈 상태를 긍정적으로 암시할 여지가 있어 더 중립적인 표현을 권장했다. 실기기 UI, viewing/posture accuracy, sensor freshness 및 battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_23ee202_2026-11-16.md`.
- commit `ce8ac49`를 검토하고 JVM XML **63/0/0**을 확인했다. 미측정 AI relay 제목은 “눈 건강을 위한 30초 휴식 릴레이”로 중립화됐다. 하지만 같은 분기의 Toast는 “맞춤 릴레이 주제”라고 해 개인화 추천으로 오인될 수 있고, Wellness 미측정 상태의 mascot은 `ic_mascot_happy`를 선택한다. 미측정 시 중립 UI 전반을 재검토할 필요가 있다. 이번 커밋은 MainScreen 문구 변경이며 Compose UI/sensor callback 경계 테스트는 없다. 실기기 UI, viewing/posture accuracy, sensor freshness 및 battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce8ac49_2026-11-16.md`.
- commit `f7a8d2d`를 독립 검토하고 `.\gradlew.bat :app:testDebugUnitTest`를 실행했다. XML 결과 13 suites, **63/0/0**. relay 미측정 Toast가 “맞춤”을 제거한 일반 휴식 안내로 바뀌고, Wellness 미측정 mascot이 `ic_nav_eye_comfort`를 사용함을 소스에서 확인했다. 변경 diff에는 전용 UI 테스트가 없으므로 QA의 출력/렌더링 PASS 주장은 코드 경로 수준으로만 수용한다. sensor callback/freshness 경계 테스트, 실기기 UI, viewing/posture accuracy 및 battery/heat는 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f7a8d2d_2026-11-16.md`.
- `BATCH-UNMEASURED-01` (`ce8ac49` → `67c4dd4`)를 PM 독립 검토했다. `.\gradlew.bat :app:testDebugUnitTest` 성공, XML 13 suites에서 **65/0/0** 확인. Relay/Wellness 미측정 표현, calculator의 `UNMEASURED` 테스트 및 detector 생성 직후 `isFresh=false` 검사는 수용한다. 다만 `Throwable`을 잡아 `null` fallback하는 detector 초기화 코드는 오류를 숨기며 Handler가 없으면 polling을 시작하지 못할 수 있다. 추가된 초기 상태 테스트는 callback freshness 경계를 검증하지 않는다. payload/range/timestamp 유효·무효 이벤트, stop/unreliable 이후 freshness 테스트가 필요하다. accuracy/battery/heat는 이번 PM 검토에서 실행하지 않아 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md`.
- 배치 후속 commit `53f1efa`를 재검토했다. `.\gradlew.bat :app:testDebugUnitTest` 성공, XML 13 suites **68/0/0** 확인. NaN 및 proximity maximumRange 초과 거부, 유효 payload 처리 테스트를 소스와 결과로 확인했다. 단, 테스트 timestamp는 `System.currentTimeMillis()`이고 production sensor sample timestamp는 elapsed-realtime domain이어야 한다. 코드도 `SystemClock.elapsedRealtime()` 오류 시 wall-clock fallback을 사용해 clock domain을 섞을 위험이 있어 제거/주입형 monotonic clock 및 fail-closed 검증이 필요하다. Infinity/unknown/invalid timestamp/stop/unreliable callback 테스트도 부족하다. 실기기 정확도/전력은 이번 검토에서 실행하지 않아 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R2_53f1efa_2026-11-16.md`.
- 배치 후속 commit `ea40a32`를 재검토했다. `.\gradlew.bat :app:testDebugUnitTest` 성공, XML **68/0/0** 확인. wall-clock fallback이 제거되고 `SystemClock.elapsedRealtime()` provider 단일화, clock 0 fail-closed 처리가 적용됐으며 결정적 provider 테스트 3개를 확인했다. 다만 clock 예외 자체, timestamp 0/미래, Infinity/empty/short/unknown type, stop/unreliable callback의 전용 테스트가 없다. 테스트 추가 후 QA 재검수가 필요하다. 실기기 정확도·전력은 이번 검토에서 실행하지 않아 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R3_ea40a32_2026-11-16.md`.
- 배치 후속 commit `869a842`를 재검토했다. `.\gradlew.bat :app:testDebugUnitTest` 성공, XML **70/0/0** 확인. provider `0L` fail-closed와 미래 timestamp 거부 테스트가 추가됐다. clock provider가 실제 예외를 throw하는 테스트는 아니므로 그 경로는 미검증이다. Infinity/empty/short/unknown type, timestamp 0 및 stop/unreliable lifecycle 테스트도 추가로 필요하다. 실기기 정확도·전력은 이번 검토에서 실행하지 않아 **NOT RUN**. PM 판정 **PARTIAL PASS**, Release Hold 유지. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R4_869a842_2026-11-16.md`.
- `aec48fd` 후속 테스트를 독립 검토했다. `.\gradlew.bat :app:testDebugUnitTest` 성공 및 XML 15 suites **82/0/0** 확인. Clock exception, timestamp 0/음수/미래, Infinity/empty/short, unknown type, proximity `value == maximumRange`, unreliable accuracy 및 stop 이후 `isFresh=false` 상태 검증을 확인했다. 단 테스트는 ContextWrapper에서 sensor service를 null로 두고 helper로 등록 상태를 조작하므로 실제 listener 등록/해제나 `SensorEvent` callback을 검증하지 않는다. 미등록/등록 실패와 stop 후 늦은 callback 차단도 미검증이다. 또 HEAD에는 별도 `ea23d44` beta MVP/UI 범위가 포함되어 이번 검토에서 승인하지 않았다. PM은 R4 테스트 범위만 조건부 수용, 배치 PARTIAL PASS 및 Release Hold 유지로 판정. 실기기 accuracy/battery/heat는 **NOT RUN**. 상세: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R5_aec48fd_2026-11-17.md`.
- 센서 이벤트 freshness/payload 검증, 실제 시청 정확도 및 배터리 비교는 **NOT RUN**이다. 기기 센서 하드웨어 가용성과 짧은 앱 UI/베타 안내 스모크만 제한 확인했다. 내부 테스트 배포, Release Hold 해제 및 정식 출시는 승인되지 않았다.
- 이전 검토 기록: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_aad01ee_2026-11-15.md`. 최신 검토와 수정 지시: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_b808392_2026-11-15.md`.

### 기존 Walkthrough 중복본 정리

- `docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`는 통합 Walkthrough에 필요한 이력을 반영한 뒤 중복 문서로 삭제했다.
- 감사 보고서의 원본 참조는 당시 검토 기록으로 보존한다. 과거 스냅샷의 테스트 통과나 출시 준비 표현을 현재 Release 승인 또는 실제 시청시간 정확도 증거로 재사용하지 않는다.

## 처음부터 끝까지 함께 진행하는 순서

### 1단계 — 현재 앱을 있는 그대로 확인하기

**목적:** 과거 문서의 주장이 아니라 지금 저장소와 Release 구성에서 실제로 동작하는 기능과 위험을 확인한다.

**하는 일**

- 기존 변경을 보존하고 저장소/빌드 기준점을 기록한다.
- 기능을 실제 구현, 부분 구현, 데모, 미구현, 미검증으로 분류한다.
- 권한, 개인정보, 센서, 점수·기록, 날짜, AI/PRO, 위젯/알림 및 다국어의 현재 동작을 조사한다.
- 테스트/빌드 결과는 실제 실행 로그로 확인한다.

**사용자가 확인할 내용:** 앱에서 꼭 제공해야 하는 핵심 가치, 당장 출시하고 싶지 않은 기능, 기존 사용자 데이터나 정책상 지켜야 하는 조건.

**단계 종료:** 개발 감사 보고서와 QA 검토 결과가 준비되면 PM이 읽고 다음 단계로 넘어갈지 결정한다.

**현재 상태:** Developer 감사와 QA 정적 재검토가 완료됐다. 정적 감사만으로 실제 기기·정확도·배터리·Play 검증은 입증되지 않아 전체 제품 QA는 **BLOCKED**다. Release Hold 유지. 작업 ID는 `AUD-01`, 기준점 기록은 `AUD-02`, 감사 QA 재검토는 `QA-01`이다.

### 2단계 — 베타에서 제공할 제품 범위 정하기

**목적:** 앱의 핵심 경험을 명확히 하고, 아직 검증되지 않은 기능이 사용자 기대를 해치지 않게 한다.

**함께 결정할 내용**

- 베타에서 반드시 제공할 핵심 기능
- 데모/미구현/정책 미확정으로 숨기거나 비활성화할 기능
- 사용자에게 표시할 점수·권한·데이터 설명
- 성공을 판단할 테스트 대상, 기간, 관찰 항목

**단계 종료:** PM이 포함/제외 범위를 명시적으로 승인하고, 개발자가 바로 실행할 수 있는 작은 작업으로 나눈다.

**현재 상태:** 사용자가 원하는 지표는 손에 들고 실제로 화면을 보는 시간이며, 단순 화면 사용/켜짐 시간은 대체 지표가 아니다. 사용자는 해당 목표 유지와 실기기 타당성 검증 선행을 선택했다. 범용 목표는 Android 13/API 33 이상이며 측정할 수 없는 경우 `미측정`으로 표시한다. Galaxy S23 및 Z Fold7은 초기 시험 후보지만 정확한 OS, 전력 허용치, 오탐/미탐 기준과 시험 절차는 아직 확정되지 않았다. 전력 비교는 각 기기의 동일 조건 기준선과 후보 간 차이 및 반복 편차를 기록하며, 모든 폰에 절대값 하나를 일괄 적용하지 않는다. UsageStats와 자세 신호만으로 실제 시청을 입증할 수 없고, 카메라/센서의 지속 사용은 저전력·정책 적합성이 미검증이다. 상세 근거와 계획은 `IrisFit_PM_Actual_Viewing_Time_Low_Power_Feasibility_2026-11-15.md`, `IrisFit_PM_Actual_Viewing_Time_Feasibility_Test_Plan_2026-11-15.md`를 참조한다. **실기기 시험 및 실제 측정 기능 구현은 별도 계획/기준 승인 전까지 승인되지 않았다.**

### 3단계 — 필요한 부분만 나눠 개발하기

**목적:** 서로 관련 없는 작업을 섞지 않고, 각 기능을 읽고 검증하기 쉽게 만든다.

**하는 일**

- 하나의 작업은 가능한 한 하나의 문제와 완료 기준을 다룬다.
- 공통 저장소·인터페이스·화면 상태·빌드 설정을 공유하는 작업은 의존관계를 명시한다.
- 개발자에게는 해당 작업에 필요한 배경과 범위만 전달한다.
- 개발자는 구현, 자체 테스트 및 변경/잔여 위험을 인계한다.

**단계 종료:** 개발자 인계가 제출되면 해당 작업의 코드를 멈추고 QA 단계로 전환한다.

**현재 상태:** 최신 `6d2b718`는 일반 위젯/공유에 미측정 문자열을 적용하고 finite 센서값을 거부한다. 그러나 활성 bitmap 위젯/AI/목표/추세에서 -1%가 표시되거나 정상 점수처럼 소비될 수 있고, detector는 payload 타입별 구조·범위를 확인하기 전에 freshness를 갱신한다. 61개 JVM 테스트는 통과했으나 소비처 렌더링 및 detector 이벤트 경계 검증은 확인되지 않았다. PM 판정 PARTIAL PASS. 상세 검토/지시: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md`.

### 4단계 — QA가 확인하고 결함을 돌려보내기

**목적:** 요구사항대로 동작하는지 구현 결과와 분리해 확인한다.

**하는 일**

- QA는 승인된 완료 기준으로 테스트를 수행한다.
- 결과는 PASS, FAIL, BLOCKED, NOT RUN 중 하나로 기록한다.
- 실패 시 재현 단계, 기대/실제, 영향과 증거를 개발자에게 전달한다.
- QA 역할은 코드를 직접 고치지 않는다. 개발자가 수정한 뒤 QA가 다시 검증한다.

**중요:** 개발자와 QA가 같은 AI여도 역할과 결과 기록은 나눈다. 이는 다른 사람의 독립 승인을 뜻하지 않는다. 사용자가 별도 검토를 원하면 별도 검토 전까지 이를 미완료로 표시한다.

**단계 종료:** 모든 필수 기준이 통과하거나 PM이 잔여 위험을 명시적으로 수용한다.

**현재 상태:** `6d2b718` QA 판정은 **PARTIAL PASS**. calculator 미측정 결과와 일반 위젯/공유 처리는 개선됐지만 활성 bitmap 위젯 및 모든 점수 소비처의 미측정 처리와 실제 payload 유효성에 따른 sensor freshness는 미해결이다. 센서 이벤트 freshness/시청 정확도/배터리는 NOT RUN. PM 검토와 수정 지시: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md`.

### 5단계 — 완료된 영역을 고정하기

**목적:** 통과한 기능이 무관한 작업에 의해 다시 깨지는 것을 방지한다.

**하는 일**

- 완료 기준, 테스트 로그, 소스/빌드 식별 및 QA 판정을 보관한다.
- 해당 영역을 “완료-고정”으로 표시한다.
- 이후 변경이 꼭 필요하면 이유, 영향, 회귀 테스트 범위를 먼저 설명하고 PM 승인 후 진행한다.

**단계 종료:** 변경 후 관련 회귀 테스트도 통과하고 새 버전의 증거가 기록된다.

### 6단계 — 내부 테스트

**목적:** 제한된 테스터가 배포 Release를 실제 설치하고 핵심 흐름을 확인한다.

**하는 일**

- 승인된 서명과 패키지로 Release를 빌드한다.
- PM/소유자 승인 후 지정한 내부 테스트 트랙에만 배포한다.
- 설치 버전과 출처를 확인하고 권한, 휴식/운동, 저장, 재실행 및 오류를 검증한다.

**단계 종료:** 핵심 흐름과 데이터 유지에 출시 차단 문제가 없고, PM이 베타 진입을 승인한다.

**현재 제한:** 키 확인, Console 업로드 및 Release Hold 해제는 별도 승인 사항이다.

### 7단계 — 제한 베타

**목적:** 제한된 실제 사용자에게 핵심 가치가 전달되는지와 이해하기 어려운 점을 확인한다.

**관찰할 내용**

- 첫 실행 후 핵심 기능을 찾고 완료하는지
- 권한 안내가 이해되고 거부해도 앱을 계속 쓸 수 있는지
- 저장 실패, 앱 종료, 기기별 오류 및 사용자 혼란
- 사용자 피드백과 크래시 등 품질 지표

건강 개선/치료를 성공 지표로 사용하지 않는다. 베타 범위와 표본/기간은 시작 전에 PM이 결정한다.

**단계 종료:** 결과와 잔여 위험을 검토한 뒤 PM이 정식 출시 심의를 승인한다.

### 8단계 — 정식 출시 여부 결정

**목적:** 베타 결과, 스토어 등록정보, 정책, 지원 체계 및 운영 위험을 검토해 공개 여부를 결정한다.

정식 출시는 개발자나 QA의 테스트 통과만으로 결정하지 않는다. 출시 권고안과 증거를 검토한 후 PM/소유자가 Production 공개를 별도로 승인한다.

### 9단계 — 출시 후 개선

**목적:** 실제 리뷰, 사용자 제보, 안정성 및 사용 지표를 통해 제품을 개선한다.

리뷰는 개인정보를 제거해 주제·빈도·심각도·재현성으로 분류한다. 지표는 분모와 측정 기간을 함께 기록한다. 각 개선은 새 작업 ID와 완료 기준을 갖고 기존 완료 영역은 영향 평가 없이 수정하지 않는다.

## 역할을 한눈에 보기

| 역할 | 책임 | 하지 않는 일 |
|---|---|---|
| 사용자 | 제품 목표/선호/데이터·출시 선택에 필요한 방향을 결정 | 기술 구현 상세를 모두 지정할 필요 없음 |
| PM | 우선순위, 제품 정책, 범위, 단계 이동 및 출시 승인 | 근거 없는 기술/정책 완료 선언 |
| 시니어 Android Developer/코드 리뷰어 | 승인된 개발 지시서 기반 설계·구현·코드 리뷰, 자체 검증 및 근거/잔여 위험 인계 | QA 판정 또는 PM 제품/출시 승인 대행 |
| 시니어 Android QA | 개발자 코드를 전제 없이 PM 지시서와 대조, 엣지 케이스·사용자 이탈 위험 점검, 테스트 코드/수동 체크리스트와 증거 보고 | QA 역할에서 직접 코드 수정 또는 PM 제품/출시 승인 대행 |

Developer와 QA는 동일 AI이므로, 역할을 분리한 QA 재검토는 사람의 독립 QA와 동등하지 않다.

## 공동 확인 방식

1. 사용자는 이 파일에서 현재 단계와 질문/결정 항목을 확인한다.
2. 사용자가 제품 선택을 해야 할 때 PM은 한 번에 한 가지 핵심 결정을 이해하기 쉽게 제시한다.
3. 결정이 내려지면 날짜/결정 ID/영향을 기록하고, 개발 지시서에 필요한 부분만 반영한다.
4. 개발/QA 결과는 이 안내서에 긴 로그를 복사하지 않고 요약과 보고서 링크/파일명을 적는다.
5. 다음 단계로 넘어가기 전 PM은 사용자에게 현재 결과, 미완료 위험, 필요한 결정과 다음 권고를 공유한다.

## 현재 단계 카드

- **현재 단계:** 3–4 — 베타 수정 및 배터리 추정 자동 QA PASS, 제품 전체 QA **PARTIAL PASS**. UsageStats 화면 사용 기반 추정치가 연결됐고 접근 권한이 없으면 `미측정`
- **완료된 것:** 승인된 베타 범위 수정, 저장/삭제 흐름, JVM **106/0/0**, 격리 Android 17 전체 AndroidTest **19/0/0**, Usage Access 허용/거부 UI 흐름 각 **1/1**, Release Kotlin 컴파일 및 boundary 확인
- **부분 완료/미승인:** 센서/evaluator JVM **29/0/0**, Galaxy S23 SensorManager lifecycle instrumented test **1/1**. UsageStats는 실제 시선/시청시간 측정이 아니며, Health Connect 실제 provider/data는 검증하지 않았다. S23 전체 UI 클래스, 수동 시각 검수, marker demo는 NOT RUN. 상세: [배터리 추정·Health Connect 보고서](IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md) 및 [베타 QA 보고서](IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md).
- **미완료:** 수동 시각 검수, Galaxy S23 전체 UI 재실행, 센서/실제 시청시간 정확도 및 전력 기준 승인과 실기기 검증, Health Connect 실데이터, Play 정책 검토. Release/Play 배포는 미승인
- **다음 행동:** 정확도·전력 시험은 사전 승인된 기준과 절차가 마련된 뒤 진행한다. 해당 시험과 Release/Play 단계는 별도 승인 전까지 대기한다.
- **배포 승인:** 이번 사용자 요청으로 Galaxy S23 로컬 Debug 설치 승인. Release/Play 외부 배포 승인은 없음 (Release Hold 유지)
- **다음 단계 전환 승인자:** PM (QA 판정만으로 자동 이동하지 않음)

## 문서 연결

- `IrisFit_PM_Dev_QA_Requirements_Process.md` — 요구사항 ID, 기본설계/상세설계, 개발·QA 역할, 변경 및 완료 추적 규칙
- `IrisFit_Current_State_Audit_2026-11-15.md` — Developer 읽기 전용 감사 결과와 증거
- `IrisFit_Current_State_Audit_QA_Review_2026-11-15.md` — QA 재검토 및 미검증/차단 항목
- `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md` — 선택된 베타 범위의 요구사항 ID/기본설계 초안
- `IrisFit_PM_Beta_MVP_Basic_Design_QA_Review_2026-11-15.md` — 기본설계의 테스트 가능성/미결 기준 QA 협의
- `IrisFit_PM_Documentation_Index_and_Decision_Log.md` — 전체 문서와 결정/작업 기록
- `IrisFit_PM_Kickoff_Current_State_Audit_2026-11-15.md` — 현재 상태 감사 지시
- `IrisFit_PM_TopDown_Modular_Workplan_2026-11-15.md` — 개발/QA 작업 분해, 역할 전환, 완료 영역 고정
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Feasibility_Test_Plan_2026-11-15.md` — 실제 시청시간 기기 타당성 검증 초안 및 미승인 기준
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Second_Review_2026-11-15.md` — 개발·QA 통합 PASS에 대한 PM 2차 재검토, 작업 재개방 및 수정 지시서
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_2026-11-15.md` — commit `3218622`와 59개 단위 테스트 결과에 대한 PM 사전 검토 및 QA 확인 항목
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_66aa763_2026-11-15.md` — commit `66aa763` 및 61개 테스트에 대한 QA/PM 판정과 남은 운영 데이터 계약
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_5018854_2026-11-15.md` — 최신 Developer 인계 PM 검토, 운영 호출처/freshness 결함 및 후속 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_aad01ee_2026-11-15.md` — `aad01ee` QA 보고서의 PM 대조 검토, PARTIAL PASS 판정 및 후속 개발 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_b808392_2026-11-15.md` — `b808392` QA 보고서 대조 검토, PARTIAL PASS 및 활성 운영 경로/센서 경계 수정 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_8a72672_2026-11-15.md` — `8a72672` QA 보고서 대조 검토, PARTIAL PASS 및 미측정 score/payload freshness 수정 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md` — `9dd7059` QA 보고서 대조 검토, PARTIAL PASS 및 모든 소비처/payload freshness 수정 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Device_Smoke_Check_2026-11-15.md` — Galaxy S23의 짧은 실기기 실행/센서 가용성 스모크 결과와 검증 한계
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md` — `6d2b718`/실기기 QA 인계 대조 검토, PARTIAL PASS 및 남은 결함 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_d301df4_2026-11-16.md` — `d301df4`/실기기 최신 Debug 검토, PARTIAL PASS 및 남은 미측정·센서·기기 매핑 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_311ab4c_2026-11-16.md` — `311ab4c`/63개 테스트/실기기 실행 재검토, PARTIAL PASS 및 남은 미측정 소비처·sensor boundary 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_59bb238_2026-11-16.md` — `59bb238`/63개 테스트/센서 범위 및 UI consumer 재검토, PARTIAL PASS 및 남은 미측정 누출 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_efcd86b_2026-11-16.md` — `efcd86b`/63개 테스트/일간 trend 처리 및 주간·월간 임의 점수 재검토, PARTIAL PASS 및 개발 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce6d320_2026-11-16.md` — `ce6d320`/63개 테스트/전 trend 탭 상태 및 구간별 임의 점수 재검토, PARTIAL PASS 및 개발 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_25fea7e_2026-11-16.md` — `25fea7e`/63개 테스트/주간 가정치 및 월간 수식 점수 재검토, PARTIAL PASS 및 개발 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_44c1606_2026-11-16.md` — `44c1606`/63개 테스트/주간·월간 임의 점수 제거 및 잔여 미측정 consumer 검토, PARTIAL PASS 개발 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f05cb53_2026-11-16.md` — `f05cb53`/63개 테스트/Goal·AiCoach 수정 및 relay/Wellness 미측정 잔여 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_23ee202_2026-11-16.md` — `23ee202`/63개 테스트/relay·Wellness 미측정 코드 검토 및 UI/sensor 테스트·중립 문구 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce8ac49_2026-11-16.md` — `ce8ac49`/63개 테스트/relay Toast 개인화 표현·Wellness 미측정 mascot 및 UI/sensor 테스트 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f7a8d2d_2026-11-16.md` — `f7a8d2d`/63개 테스트/relay·Wellness 코드 수정 수용 및 UI/sensor 경계 테스트 잔여
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md` — 배치 `ce8ac49`→`67c4dd4`/65개 테스트/초기 freshness 한계와 오류 처리 재작업 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R2_53f1efa_2026-11-16.md` — 배치 R2 `67c4dd4`→`53f1efa`/68개 테스트/monotonic clock domain 재작업 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R3_ea40a32_2026-11-16.md` — 배치 R3 `53f1efa`→`ea40a32`/68개 테스트/추가 clock·callback lifecycle 경계 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R4_869a842_2026-11-16.md` — 배치 R4 `ea40a32`→`869a842`/70개 테스트/actual exception 및 추가 lifecycle 테스트 지시
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R5_aec48fd_2026-11-17.md` — 배치 R5 `aec48fd`/82개 테스트/조건부 PM 수용, 미등록·지연 callback 및 별도 beta QA 잔여
- `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_QA_Role_and_Handoff_Standard_2026-11-15.md` — QA 검수 역할, 출력 형식 및 PASS/BLOCKED/NOT RUN 판정 원칙
- 저장소 루트 `Walkthrough.md` — 이 통합 Walkthrough의 진입점
- 기존 중복 Walkthrough 3개(`docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`) — 통합 후 삭제

---

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 사용자와 PM이 동일한 진행 상태, 결정, 출시 단계 및 다음 행동을 쉽게 확인할 수 있도록 단일 공동 안내서를 유지한다.
* **상세 요구사항 (What):**
  - 기능 1: 본 파일을 사용자·PM 공동 진행 안내의 기준으로 사용하고 현재 단계, 완료/미완료, 다음 행동과 승인 상태를 갱신한다.
  - 기능 2: 상세 감사·개발·QA 로그는 별도 작업 ID 문서에 보관하고 이 파일에는 짧은 요약과 문서 연결만 기록한다.
  - 기능 3: Developer 작업과 QA 판정의 역할 전환 및 결과를 분리하고, 결함/재검증이 있으면 같은 작업 ID로 이력을 연결한다.
  - 기능 4: 완료된 영역은 증거가 확보된 뒤 완료-고정 상태로 표시하고, 이후 변경에는 영향 분석·PM 승인·회귀 결과를 남긴다.
* **예외 처리 및 엣지 케이스:** 루트 `Walkthrough.md`는 이 문서로 연결되는 진입점으로 유지하고, 이 문서에 제품 로드맵과 주요 기술 이력을 통합한다. 과거 스냅샷, 미검증 결과, PM 미승인 및 Release Hold를 완료로 표시하지 않는다.
* **Google Play 정책 체크포인트:** 권한·개인정보·건강 표현·SDK/AI·결제 및 배포 승인은 사용자에게 단계별로 알기 쉽게 공유하되, 정책 근거와 검증 한계를 생략하지 않는다.
* **완료 조건 (Acceptance Criteria):** (1) 사용자가 통합 본문에서 전체 로드맵, 현재 단계·완료/부분 완료/미완료, 다음 행동을 확인할 수 있다. (2) 감사부터 출시 후까지 단계별로 정리되어 있다. (3) 구현 요약과 상세 증거 문서가 연결된다. (4) 단계 이동, 고정/재개방 및 출시 승인이 실제 PM 결정과 일치한다.
