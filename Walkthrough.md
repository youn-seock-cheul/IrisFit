# IrisFit Walkthrough

이 파일은 저장소에서 제품 로드맵과 구현 이력을 함께 확인하는 진입점입니다. **현재 기준 문서는 [`task/IrisFit_PM_User_Walkthrough.md`](task/IrisFit_PM_User_Walkthrough.md)**이며, 이 문서에 제품 상태, 전체 로드맵, 주요 개발 이력, QA 근거와 결정이 통합되어 있습니다.

## 후속 개발 — 2026-11-18~19

승인된 UI 후속 수정과 설정의 5종 색톤 선택을 구현했다. 2026-11-19에는 눈 운동 중단 후 재시작 때 세션 ID가 남던 두 훈련의 공통 결함을 수정하고, 빠른 점멸·파동 리플 및 4가지 3D 위성 궤도를 구현했다. 무한대·8자·원형·부드러운 S자 모드를 각각 지구·목성·토성·금성으로 구분하고, 패널 없는 작은 자연 위성과 표면 패턴의 행성 자전을 추가했다. 지그재그의 급격한 방향 전환은 부드러운 S자 곡선으로 교체했다. 관련 JVM 테스트와 Debug/AndroidTest APK 조립이 통과했고, Galaxy S23(Android 16)에서 재시작 UI 회귀와 위성 표시 테스트가 통과했다. 상세 구현·검증 한계는 [후속 개발 기록](task/IrisFit_UI_Followup_2026-11-18.md)에 남겼다. 현재 변경은 미커밋이며 아래 제품/출시 보류 상태는 유지한다.

2026-11-19 센서 검증 요청에 따라 `SmartDevicePoseDetector`의 등록/해제 경계를 분리하고 세션 generation guard를 추가했다. 등록 실패·늦은 이전 세션 callback의 JVM 테스트, Galaxy S23 실제 센서 등록 및 callback 기기 테스트, Debug/AndroidTest 조립과 Release Kotlin 컴파일이 통과했다. 자세/시청시간 정확도 및 전력 검증은 미실시이며 Release Hold는 유지한다. 상세 증거와 한계는 [센서 생명주기 검증 기록](task/IrisFit_Sensor_Lifecycle_Verification_2026-11-19.md)에 있다.

## 베타 MVP/UI QA 후속 — 2026-11-20

기존 베타 범위 위반인 홈 릴레이/AI 진입점을 숨겼고, 실제 시청시간 측정이 연결되기 전까지 홈·위젯의 배터리 점수는 `미측정`으로 고정했다. 수정된 두 blocker는 Galaxy S23 새 회귀 테스트 1/1, Android 17 에뮬레이터 `UiFollowupTest` 8/8로 통과했다. 2026-11-20 사용자 요청으로 Debug APK를 Galaxy S23(Android 16)에 로컬 설치하고 MainActivity foreground 실행을 확인했다. 서명 Release/Play 배포가 아니며 전체 QA는 PARTIAL PASS, Release Hold 유지다. [후속 검증 및 설치 기록](task/IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md).

같은 날짜에 남은 자동 QA를 다시 실행해 JVM 103/103, 격리 Android 17 에뮬레이터의 전체 AndroidTest 18/18을 통과했다. `UiFollowupTest` 10/10에는 운동 완료 저장과 4개 언어 × 5개 색톤의 홈 자동 smoke가 포함된다. 수동 시각 검수, Galaxy S23 전체 UI 재실행, 센서/실제 시청 정확도, 전력·발열 및 Play 검증은 NOT RUN이므로 전체 판정은 계속 PARTIAL PASS다. 상세 결과: [후속 QA 기록](task/IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md). Release Hold 유지.

## 눈 배터리 화면 사용 추정 및 선택형 Health Connect — 2026-11-20

사용자 승인에 따라 홈의 배터리를 UsageStats 기반 화면 사용 **추정치**로 연결하고, Health Connect 수면·활동 연동을 설정에서 선택하도록 했다(기본 꺼짐). Usage Access가 거부되면 미측정으로 유지한다. 추정치는 실제 시선·실제 시청시간·눈 피로 측정이나 진단이 아니며, 기존 실제 시청시간 정확도 목표를 통과한 것으로 보지 않는다.

사용자 피드백에 따라 홈에는 짧은 요약만 보이고 산식·고지는 `자세히`에서 펼쳐지도록 바꿨다. JVM 106/106과 홈 펼침/접힘 UI 테스트 1/1 통과 후 최신 Debug APK를 Galaxy S23에 업데이트 설치하고 실행을 확인했다. Health Connect 실데이터, 사용량 정확도, 센서/전력 및 Play 검증은 NOT RUN이다. 설치는 로컬 Debug이며 외부 배포는 아니고 Release Hold 유지. 상세: [기능 검증](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md), [폰 설치 기록](task/IrisFit_Phone_Debug_Install_2026-11-20.md).

후속 승인으로 홈 카드 여백을 줄이고 산식 토글을 정보 아이콘·화살표 pill로 다듬었다. Health Connect 실제 데이터 읽기가 성공한 경우 수면/깊은 수면과 기존 산식의 계산 결과, 걸음·등산 보너스를 홈에 안내하며 산식 자체는 유지했다. 선택 JVM 테스트 **15/15**, Android 17 격리 AVD 전체 AndroidTest **19/19**, Release 컴파일·boundary 검증 및 Debug APK 조립이 통과했다. 새 Debug APK를 Galaxy S23에 업데이트 설치해 PID와 foreground Activity를 확인했다. APK SHA-256 및 세부 한계는 위 설치 기록에 추가했다. 실기기 Health Connect 데이터·권한 흐름과 정확도·전력은 여전히 NOT RUN; Release Hold 유지.

2026-11-20 후속: [상태 구간 변경 취소 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md). 홈·위젯 상태는 기존 4단계로 복원했고, 점수·Health Connect 계산은 유지했다. 이후 사용자 요청으로 복원본을 Galaxy S23에 Debug 업데이트 설치했다. 전체 JVM 108/108 통과와 실기기 홈 4단계 범례 표시를 확인했다. 격리 AVD AndroidTest, Health Connect 실데이터, 정확도·전력 검증은 NOT RUN이며 Release Hold를 유지한다. 상세: [폰 설치 기록](task/IrisFit_Phone_Debug_Install_2026-11-20.md).

2026-11-20 홈 카드 목업 정리: 사용자 승인에 따라 홈 배터리 카드를 첨부 목업 구성(상태 칩·잔량, 단어 범례, 휴식·훈련 타일, 메시지 박스, 공유)으로 정돈하고, 상태 라벨을 `행복` 등 단어로, 연동 문구를 `건강정보연동 배터리 표시 중`으로 바꿨다. 산식·구간은 그대로다. JVM 108/108, Release boundary, 격리 Android 17 AVD의 관련 UI 테스트(탭 유지 반영 수정 후 `UiFollowupTest` 12/12, 홈·휴식 흐름 4/4)와 4개 언어·연동·미측정 화면을 확인했다. 이후 사용자 요청으로 21:11에 해당 Debug APK를 S23에 업데이트 설치하고 앱 기동 및 설치 APK 해시 일치를 확인했다. 위젯·알림·실데이터 검증은 NOT RUN. 카드 안 추가 고지는 사용자 지시에 따라 넣지 않았고, 출시 때 스토어 설명에 '의료기기 아님' 고지가 필요하다. Release Hold 유지. [변경·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md), [폰 설치 기록](task/IrisFit_Phone_Debug_Install_2026-11-20.md).

## 제품/출시 상태 — 2026-11-17 기준 유지

2026-11-20 홈 카드 후속: 목업 적용 전 글자·아이콘·버튼 크기와 여백을 복원했다. 홈 상단에 오늘 화면 사용 시간을 두고, 카드에는 잔량·게이지 → 건강 안내 → 휴식·훈련 순으로 배치했다. 사용자 요청에 따라 21:24 최신 Debug APK를 S23에 업데이트 설치하고 앱 실행과 APK 해시 일치를 확인했다. 실기기 시각 검수·실데이터 확인은 NOT RUN. [수정·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md#home-layout-restore), [폰 설치 기록](task/IrisFit_Phone_Debug_Install_2026-11-20.md).

2026-11-20 홈 문구·정렬 후속: 사용자 승인으로 정상 상태 안내를 짧게 줄이고, 홈의 의료 측정 고지·건강 기록 없음 안내를 제거했다. 앱 내 건강 연동 용어를 쉬운 표현으로 교체하고 세 휴식·훈련 버튼의 아이콘·텍스트만 중앙 정렬했다(크기·여백 유지). Debug 조립과 관련 JVM 16/16 통과. 이번 수정본은 S23 미설치이며 시각·실데이터 검증 NOT RUN. [변경·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md#home-copy-center).

2026-11-20 시선 추적 후속: 사용자 승인으로 눈동자 훈련 시선 추적 4종의 가운데 행성과 달 크기를 절반으로 줄였다(이동 반경 유지, 토성 고리 비례 축소). Debug 조립과 궤도 JVM 4/4 통과. S23 미설치, 시각 검수 NOT RUN. [변경·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md#pursuit-planet-size).

2026-11-20 홈 카드 글자 크기 후속: 사용자 승인으로 카드 안 글자 크기만 21:11 목업 버전으로 되돌렸다(잔량 24sp, 버튼 12/16/11sp, 안내 14sp 등; 배치·여백·아이콘 유지). Debug 조립, UI 테스트 컴파일, 관련 JVM 16/16 통과. S23 미설치, 시각 검수 NOT RUN. [변경·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md#home-card-font-mockup).

2026-11-20 Health Connect 후속: Android 14 이상에서 누락된 권한 안내 화면을 등록하고, 홈 부제에 실제 연동 상태를 표시했다. 산식은 세 항목으로 줄였다. 격리 기기에서 시스템 허용·해제와 홈 표시를 확인했다. 이후 사용자 요청으로 19:32에 S23에 데이터 보존 Debug 업데이트 설치를 완료했으며, 앱 실행·권한 안내 화면 등록·설치 APK 해시 일치를 확인했다. 건강정보 권한 자동 허용과 실데이터 검증은 하지 않았고 Release Hold를 유지한다. [진단·검증 기록](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md), [폰 설치 기록](task/IrisFit_Phone_Debug_Install_2026-11-20.md).

목표는 실제 시청시간(손에 들고 실제 화면을 보는 시간)입니다. UsageStats의 앱 사용시간이나 자세 분류를 실제 시선 측정으로 간주하지 않습니다.

| 구분 | 현재까지 / 남은 일 |
|---|---|
| **완료** | 현재 구현 정적 감사와 QA 검토; 실제 시청시간 목표·Android 13+·측정 불가 시 `미측정` 원칙 설정; `aec48fd` 기준 JVM 테스트 **82 passed / 0 failed / 0 skipped**; clock/payload/lifecycle 상태 테스트 추가 |
| **진행 중** | 개발·QA 단계. 베타 수정과 현재 자동 QA 게이트 PASS; 수동·실기기 게이트가 미완료라 전체 QA/PM 완료 판정은 **PARTIAL PASS** |
| **우선 보완** | 수동 시각 검수; 승인된 센서·실제 시청시간 정확도 및 배터리/발열 시험 기준 마련과 실기기 검증 |
| **실기기에서 미검증** | 센서 callback/freshness 정확도, 자세 오탐·미탐, 실제 시청시간 정확도, 배터리/발열 비교. 승인된 시험 프로토콜이 없어 정확도/전력 결과는 **NOT RUN** |
| **출시** | 내부 테스트·베타·Play 배포 모두 미승인. **Release Hold 유지** |

**다음 순서:** 모든 미측정 소비처와 sensor/UI 경계 테스트 보완 → QA 재검수 → PM 완료 판정 → 사전 승인된 기준으로 실기기 정확도/배터리 시험 → 별도 내부 테스트/출시 승인. 화면에 표시된 점수·사용시간·자세는 정확도 증거가 아닙니다.

## 통합 Walkthrough

- [2026-11-18 UI 개편 및 후속 개발 기록](task/IrisFit_UI_Followup_2026-11-18.md) — 승인된 순서, 색톤 선택, 구현·검증 상태. 대화 초기화 시 이 문서부터 확인.
- [2026-11-19 베타 MVP/UI QA 재검수](task/IrisFit_Beta_MVP_UI_QA_Review_2026-11-19.md) — `ea23d44`와 현재 UI의 범위 판정, 테스트 증거 및 잔여 항목.
- [2026-11-20 베타 MVP/UI QA 후속](task/IrisFit_Beta_MVP_UI_QA_Followup_2026-11-20.md) — 릴레이/AI 진입점 제거 및 점수 미측정 처리, 전체 자동 suite 재검증 결과와 미완료 실기기·수동 게이트.
- [2026-11-20 눈 배터리 추정·Health Connect 구현](task/IrisFit_Eye_Battery_Health_Connect_Implementation_2026-11-20.md) — 사용자 승인 UsageStats 화면 사용 추정 및 기본 꺼짐 Health Connect 선택 연동의 변경·검증·미완료 게이트.
- [2026-11-20 Galaxy S23 Debug 설치](task/IrisFit_Phone_Debug_Install_2026-11-20.md) — 사용자 요청에 따른 로컬 Debug 설치, 설명 간결화 후 업데이트 및 실행 확인. Release/Play 배포가 아님.

- [PM 로드맵 및 개발 이력 통합본](task/IrisFit_PM_User_Walkthrough.md) — 유일한 현재 상태/제품 로드맵 기준
- [PM 결정 로그 및 문서 인덱스](task/IrisFit_PM_Documentation_Index_and_Decision_Log.md) — 승인, 작업 상태, 증거 링크
- [제품 개선 아이디어 백로그 — 다국어/마일리지](task/IrisFit_PM_Product_Ideas_Backlog_2026-11-16.md) — PM 검토 대기; 아직 범위/정책 승인 아님
- [이전 QA 결과/PM 판정 — 9dd7059](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md)
- [이전 QA 결과/PM 판정 — 6d2b718](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md)
- [최신 QA 결과/PM 판정 — f05cb53](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_f05cb53_2026-11-16.md)
- [최신 QA 결과/PM 판정 — 23ee202](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_23ee202_2026-11-16.md)
- [최신 QA 결과/PM 판정 — ce8ac49](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_ce8ac49_2026-11-16.md)
- [최신 QA 결과/PM 판정 — f7a8d2d](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_f7a8d2d_2026-11-16.md)
- [최신 배치 QA 결과/PM 판정 — BATCH-UNMEASURED-01](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md)
- [배치 재검토 R2 — 53f1efa](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R2_53f1efa_2026-11-16.md)
- [배치 재검토 R3 — ea40a32](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R3_ea40a32_2026-11-16.md)
- [배치 재검토 R4 — 869a842](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R4_869a842_2026-11-16.md)
- [배치 재검토 R5 및 PM 제출 승인서 — aec48fd](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_R5_aec48fd_2026-11-17.md)
- [Galaxy S23 실기기 스모크 체크](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Device_Smoke_Check_2026-11-15.md)
- 상세 로드맵과 결정 이력은 아래 통합 문서를 확인하세요.

## 이력 및 재검증 원칙

이 Walkthrough는 기존 파일의 2026-11-12 Widget 1 정리, 2026-11-13 블루라이트 오버레이 제거 및 Gemini Nano 코치 기록을 통합 문서의 개발 이력으로 보존합니다. 기존 검증은 해당 시점의 기록이며 현재 Release 상태나 실제 시청시간의 정확도를 입증하지 않습니다. 자세한 이력과 한계는 통합 문서에 기록합니다.

중복된 과거 Walkthrough 스냅샷 3개(`docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`)는 통합 후 삭제했습니다. 과거 검증 결과는 현재 승인으로 해석하지 않습니다.
