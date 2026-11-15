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

**현재 로드맵 위치:** 실제 시청시간 목표(Android 13/API 33 이상, 측정 불가 시 `미측정`)를 유지하며 Galaxy S23/Z Fold7은 초기 시험 후보로 정했다. 최신 commit `efcd86b`와 JVM 테스트 **63 passed / 0 skipped / 0 failed**를 확인했다. 일간 trend 차트는 미측정 placeholder/라벨 처리가 추가됐다. 그러나 주간/월간 trend는 휴식·운동 기록으로 임의 점수를 생성하며, MainScreen AI/Goal/relay 및 Wellness gauge/legend에서도 미측정 소비가 남아 있다. 이번 변경에 UI/sensor 경계 자동 테스트는 추가되지 않았고 제출된 QA 렌더링 PASS를 별도 테스트로 확인하지 못했다. Debug 설치와 Activity 실행은 확인했으나 정확도/전력 측정은 하지 않았다. PM은 **PARTIAL PASS**, Release Hold 유지로 판정했다.

## 한눈에 보는 진행 현황 — 2026-11-16

| 영역 | 현재 상태 | 결과/남은 기준 |
|---|---|---|
| 제품 목표 | **결정됨** | 목표는 실제로 손에 들고 보는 시간. Android 13+ 대상, 측정할 수 없으면 `미측정`; UsageStats는 실제 시청시간의 대체값이 아님 |
| 현황 감사 | **완료 — 정적 범위** | 소스/권한/데이터 경로를 확인하고 QA가 재검토. 정책/Play 및 전체 기기 검증은 미완료 |
| 코드 및 자동 테스트 | **부분 완료** | `efcd86b`; JVM **63 passed, 0 failed, 0 skipped**. 일간 trend placeholder 개선. 주간/월간 임의 점수, 다른 consumer 누출 및 sensor/UI 경계 테스트 잔여 |
| 실기기 기본 확인 | **제한 완료** | Galaxy S23+ `SM-S916N`, Android 16/API 36에서 Debug 설치·실행 및 기본 UI 확인. 자세 배지와 UsageStats 숫자는 표시 확인이지 정확도 측정이 아님 |
| 미측정 처리 | **미완료** | 일간 trend는 placeholder 처리. 주간/월간이 기록만으로 가짜 점수를 만들며 AI Coach, Goal, 게이지/범례도 sentinel을 소비할 수 있음 |
| 센서 freshness | **부분 완료** | payload 유한성·축 길이·unknown type·양수 finite `maximumRange` 및 proximity 값 상한을 검사한 뒤 freshness 갱신. 실제 listener 경계 테스트 필요 |
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
| 3. 개발 및 자체 검증 | 승인된 요구사항만 구현하고 기준별 증거/잔여 위험을 인계 | **수정 인계 제출** — commit `efcd86b`; 전체 단위 테스트 63/0/0; 일간 trend 개선, 주간/월간 score 근거 및 다른 미측정 consumer 보완 필요 |
| 4. QA 검증 | PM 완료 기준별 자동/수동 시험을 수행하고 증거와 PASS/FAIL/BLOCKED/NOT RUN 보고 | **PARTIAL PASS** — 일간 trend 코드 확인; UI/sensor boundary 테스트, 전체 미측정 consumer 및 정확도/전력 검증 잔여 |
| 5. PM 완료·고정 판정 | QA 결과·기기 범위·잔여 위험을 검토해 완료/보류 판정 | **보류** — 제품 기능 완료/고정 미승인; 후속 수정 후 재검수 필요 |
| 6. 내부 테스트 | Release 패키지 설치 및 핵심 흐름을 실제 테스트, PM/소유자가 트랙 업로드 승인 | **미착수/미승인** — Release Hold 유지 |
| 7. 제한 베타 | 승인된 테스터와 기간/표본으로 사용성·안정성·피드백 검증 | **미착수** — 내부 테스트 종료 및 PM 승인 필요 |
| 8. 정식 출시 심의 | 베타 결과, Play 정책/스토어 자료, 운영 위험을 검토하고 공개 여부 별도 승인 | **미착수** — 자동 승인 없음 |
| 9. 출시 후 개선 | 리뷰·오류·지표를 개인정보 보호 원칙에 따라 분석하고 새 작업으로 관리 | **미착수** |

단계 3–5가 끝나도 실제 시청시간 측정/전력 타당성은 별도 검증 게이트다. 실제 기기 시험 전에는 해당 수치의 정확도·전력 효율을 PASS로 표시하지 않는다.

## 주요 개발 Walkthrough 이력

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

- **현재 단계:** 3–4 — `efcd86b` PM PARTIAL PASS, 전 기간 추세의 근거 데이터와 미측정 consumer 및 sensor/UI 경계 보완 후 QA 재검수 대기
- **완료된 것:** 저장소 기준점/정적 감사, 감사 QA 재검토, 베타 MVP 범위 기본설계, 사용자가 승인한 실제 시청시간 목표와 Android 13+ 미측정 원칙, 두 Galaxy 기기의 초기 시험 후보, 오늘 화면 사용시간 UI에서 evaluator 추정치 비노출의 정적 확인
- **부분 완료/미승인:** Developer commit `efcd86b`와 전체 단위 테스트 63 passed/0 skipped/0 failed를 확인했다. 일간 추세의 placeholder 표시는 개선됐지만 주간/월간은 기록 기반 가짜 점수를 표시하고 AI/Goal/relay/Wellness 미측정 누출 및 sensor/UI 경계 테스트가 남았다. 실제 viewing/posture accuracy 및 battery 측정은 **NOT RUN**이다.
- **미완료:** 모든 consumer의 미측정 상태 표시 및 파생 계산 차단, sensor type별 payload 선검증, `SM-S916N` 기기 모델 매핑, UI/센서 경계 테스트, QA 재검수, 실기기 정확도/배터리 타당성 기준 승인 및 실행, 내부 테스트 및 배포 승인
- **다음 행동:** Developer가 미측정 상태를 모든 점수 소비처에서 명시적으로 처리하고 유효 payload에서만 freshness를 갱신한 뒤 자동 테스트 근거를 제출한다. 별도로 PM 승인 후 실기기 타당성 시험을 진행한다.
- **배포 승인:** 없음 (Release Hold 유지)
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
