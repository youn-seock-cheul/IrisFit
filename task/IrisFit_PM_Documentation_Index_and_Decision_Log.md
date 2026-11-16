# IrisFit 기획·분석 문서 인덱스 및 결정 로그

- 문서 관리자: 수석 PM
- 기준 문서 위치: `C:\Users\youns\AndroidStudioProjects\IrisFit_LabA\task`
- 추가 PM 지시/검토 문서 위치: `C:\Users\youns\AndroidStudioProjects\task`
- 기준일: 2026-11-16
- 출시 상태: **Release Hold**

## 운영 규칙

- 실질적인 기획, 감사, QA, 개발 지시 및 PM 결정은 날짜가 포함된 Markdown 문서로 보존한다.
- 기존 보고서를 덮어쓰지 않는다. 새 증거와 결과는 새 문서 또는 날짜가 명시된 추가 기록으로 남긴다.
- 각 문서에서 확인된 사실, 해석, 제안, 승인된 결정, 미확인 사항을 구별한다.
- 테스트 실행일, 환경, Git HEAD/빌드 변형, 명령, 성공·실패·건너뜀, 원시 로그/결과 경로를 기록한다.
- `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`을 구별한다. 실행하지 않은 검증은 통과 처리하지 않는다.
- 비밀정보, 서명키, 비밀번호, 토큰 및 민감한 사용자 데이터를 기록하지 않는다.
- 지시가 충돌하면 최신 문서를 임의로 선택하지 않고 PM 결정을 요청한다.
- PM 승인 없이 코드 범위, 데이터 정책, 서명키, 사용자 데이터 또는 Play 배포를 변경하지 않는다.
- 저장소 작업 전 dirty worktree를 포함한 기준점을 기록하고 기존 변경을 보존한다.

## 현재 기준

- 진행 순서: **현재 Release 구현 감사 → 베타 범위/수용 기준 승인 → 개발·자체검증 → QA 재검수 → PM 완료 판정 → 내부 테스트 → 제한 베타 → 별도 정식 출시 승인**
- `AUD-01` Developer 감사 및 `QA-01` 정적 재검토는 완료됐으나 실제 기기/Play 검증은 미완료다. 전체 제품 출시 준비는 **BLOCKED**다.
- 실제 시청시간 목표는 유지한다. Android 13+ 범용 목표와 측정 불가 시 `미측정`을 승인했고, Galaxy S23/Z Fold7은 초기 시험 후보로만 정했다. 기기별 전력 허용치·정확도 기준·시험 절차는 미승인이다.
- 현재 단계: **`BATCH-UNMEASURED-01`, final `67c4dd4` PM 검토 — PARTIAL PASS, 센서 재작업 대기**. Gradle XML 65 passed/0 skipped/0 failed. 미측정 calculator 및 detector 생성 직후 `isFresh=false` 테스트를 확인했으나 callback freshness 경계는 테스트되지 않았다. `SmartDevicePoseDetector`가 `Throwable`을 포착해 조용히 null fallback하는 처리는 진단 불가/기능 저하 우려로 수정이 필요하다. 근거: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md`.
- 실기기 상태: 이번 PM 검토에서 callback freshness, 실제 viewing/posture accuracy 및 battery/heat를 실행하지 않았다. 모두 NOT RUN. 근거: `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md`.
- 출시 Hold 해제, 내부 테스트 업로드 및 Production 배포는 별도 PM 승인 대상이다.

## 문서 인덱스

| 문서 | 상태 | 용도 |
|---|---|---|
| `..\Walkthrough.md` / `IrisFit_PM_User_Walkthrough.md` | 통합 Walkthrough 기준 | 저장소 루트 진입점과 PM 로드맵·개발 이력 통합 본문 |
| `IrisFit_PM_Dev_QA_Requirements_Process.md` | 사용자 승인 공통 프로세스 | 요구사항 ID, PM 기본설계/QA 협의, Developer 상세설계, QA 검증, 변경관리 및 완료 판정 절차 |
| `IrisFit_PM_Kickoff_Current_State_Audit_2026-11-15.md` | 지시됨 — 읽기 전용 감사 | 현재 앱 및 Release 구성, 데이터/권한, 결함, 베타 MVP 후보 감사 |
| `IrisFit_Current_State_Audit_2026-11-15.md` | Developer 감사 완료 | 기준점, 기능/Release 인벤토리, 데이터 흐름, finding, 검증 로그와 베타 후보 |
| `IrisFit_Current_State_Audit_QA_Review_2026-11-15.md` | QA 검토 완료 — 전체 QA BLOCKED | 수용 기준과 finding 재검토; 실기기/Play 검증 미완료 |
| `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md` | PM 기본설계 초안 | 베타 요구사항; 실제 시청시간 감지·저전력/실기기 검증 접근, 운동/데이터 세부 결정 대기 |
| `IrisFit_PM_Actual_Viewing_Time_Low_Power_Feasibility_2026-11-15.md` | 정적 가능성 검토 완료 — 실기기 미검증 | 소스/API/Play 요구사항, 실제 시청 감지와 센서·카메라 지속 사용의 제약, 실기기 비교 시험 계획 |
| `IrisFit_PM_Beta_MVP_Basic_Design_QA_Review_2026-11-15.md` | QA 협의 진행 중 — 기본설계 BLOCKED | 요구사항별 테스트 가능성 및 실제 시청시간/전력 증거 대기 |
| `IrisFit_PM_TopDown_Modular_Workplan_2026-11-15.md` | 운영안 | Top-Down 전체 목차, 작은 독립 작업, 담당자별 간결 브리프, 완료 영역 고정 및 동일 AI의 개발/QA 역할 분리 절차 |
| `IrisFit_PM_Documentation_Index_and_Decision_Log.md` | 현재 문서 | 인덱스, 기록 규칙, PM 결정 및 다음 단계 추적 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Feasibility_Test_Plan_2026-11-15.md` | 계획 초안 — 기준 미승인/실기기 미실시 | 기기별 기준선 비교와 실제 시청 정확도 타당성 시험 제안 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Second_Review_2026-11-15.md` | PM 판정 — PARTIAL PASS/재개방 | 개발·QA 제출 결과의 코드/테스트 증거 검토 및 수정 지시 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_2026-11-15.md` | PM 사전 검토 완료 — QA 대기 | 최신 Developer 인계/commit 및 테스트 결과 확인, QA 검수 초점 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_66aa763_2026-11-15.md` | QA PARTIAL PASS — PM 완료 보류 | `66aa763`, 61개 JVM 단위 테스트 및 운영 데이터/실기기 한계에 대한 PM 판정 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_5018854_2026-11-15.md` | PM 부분 수용 — 후속 수정/QA 재검수 대기 | typed 결과 계약 및 61개 테스트 확인; 호출처의 실패-0 변환과 센서 freshness 공백 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_aad01ee_2026-11-15.md` | QA PARTIAL PASS — PM 완료 보류 | `aad01ee`와 61개 JVM 테스트 재확인; 실패-0 운영 호출처, 첫 이벤트 전 freshness 및 테스트 범위 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_b808392_2026-11-15.md` | QA PARTIAL PASS — PM 완료 보류 | `b808392`와 61개 JVM 테스트 재확인; 활성 Widget 1/AI 코치의 실패-0 경로, 미측정 score, 센서 이벤트 경계 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_8a72672_2026-11-15.md` | QA PARTIAL PASS — PM 완료 보류 | `8a72672`와 61개 JVM 테스트 재확인; 미측정 점수/status 노출, 센서 payload 검증 전 freshness 갱신 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md` | PM PARTIAL PASS — 잔여 결함 재개방 | Calculator의 UNMEASURED는 확인했으나 위젯/AI/공유/추세의 -1 점수 처리 및 payload 검증 전 freshness 갱신을 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md` | PM PARTIAL PASS — 실기기/UI/센서 잔여 결함 재개방 | 61 JVM tests; basic device UI and beta dialog observed; bitmap widget/AI/trend score paths and type-specific payload freshness unresolved |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_d301df4_2026-11-16.md` | 과거 PM PARTIAL PASS | `d301df4`의 당시 상태 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_311ab4c_2026-11-16.md` | 과거 PM PARTIAL PASS | `311ab4c` 기준 모델 매핑 및 센서 payload guard 결과 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_59bb238_2026-11-16.md` | 이전 PM PARTIAL PASS | `59bb238` 기준 proximity bound 및 당시 trend/UI 미측정 결함 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_efcd86b_2026-11-16.md` | 이전 PM PARTIAL PASS | `efcd86b`의 일간 미측정 처리 및 주간/월간 임의 점수 결함 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce6d320_2026-11-16.md` | 이전 PM PARTIAL PASS | `ce6d320` 전체 trend placeholder 및 기간별 가짜 점수 검토 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_25fea7e_2026-11-16.md` | 이전 PM PARTIAL PASS | `25fea7e` 당시 120분 가정/월간 수식 등 기록 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_44c1606_2026-11-16.md` | 이전 PM PARTIAL PASS | `44c1606`의 trend 공식 제거 및 잔여 consumer 결함 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f05cb53_2026-11-16.md` | 이전 PM PARTIAL PASS | `f05cb53` 당시 Goal/AiCoach guard 확인, relay/Wellness 미측정 처리 잔여 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_23ee202_2026-11-16.md` | 최신 PM PARTIAL PASS — Release Hold 유지 | 63 JVM tests; relay/Wellness/Goal/AiCoach 미측정 code path 확인; UI/sensor boundary tests 및 relay 중립 문구 검토 잔여 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce8ac49_2026-11-16.md` | 최신 PM PARTIAL PASS — Release Hold 유지 | 63 JVM tests; relay 제목 중립화 확인, Toast 개인화 표현/Wellness happy mascot 및 UI/sensor boundary tests 잔여 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f7a8d2d_2026-11-16.md` | 최신 PM PARTIAL PASS — Release Hold 유지 | 63 JVM tests; relay Toast와 Wellness 중립 아이콘 코드 확인; UI/sensor boundary tests 및 실기기 검증 잔여 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md` | 최신 배치 PM PARTIAL PASS — 재작업 필요 | 65 JVM tests; calculator/초기 freshness 테스트 확인; callback boundary tests 및 `Throwable` 조용한 fallback 수정 필요 |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Device_Smoke_Check_2026-11-15.md` | 제한된 실기기 스모크 완료 | Galaxy S23 Android 16/API 36 앱 실행 및 센서 하드웨어 가용성만 확인; 정확도/이벤트 freshness/배터리는 NOT RUN |
| `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_QA_Role_and_Handoff_Standard_2026-11-15.md` | 사용자 정의 역할 반영 | QA 책임, 산출물, 출력 형식 및 판정 용어 |

저장소 루트 `Walkthrough.md`를 공동 진입점으로 하고, `task/IrisFit_PM_User_Walkthrough.md`에 전체 제품 로드맵과 기존 Walkthrough의 개발 이력을 통합 보관한다. 중복 과거 파일 `docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`는 통합 후 삭제했다. 실제 PM 개발 지시 및 추가 검토 보고서는 사용자가 지정한 `C:\Users\youns\AndroidStudioProjects\task`에도 보관한다. 기존 기준·QA 문서는 작업 시 원본 파일과 기준 날짜를 확인한 후 새 감사 보고서의 입력 자료로 등록한다. 과거 보고의 검사 결과는 현재 상태의 증거로 재사용하지 않는다.

## PM 결정 로그

### DEC-2026-11-15-01 — 먼저 현재 구현을 감사

- **상태:** 승인된 작업 방향
- **결정:** 기능을 추가하기 전에 현재 Release 구현·권한·데이터·사용자 흐름을 읽기 전용으로 조사한다.
- **이유:** 기존 자료는 서로 다른 시점의 상태를 담고 있으며 현재 작업 트리도 먼저 식별해야 한다.
- **근거:** `IrisFit_PM_Kickoff_Current_State_Audit_2026-11-15.md`
- **제약:** 코드 수정·정책 확정·키 작업·Play 배포 승인은 포함하지 않는다.

### DEC-2026-11-15-02 — 단계별 상품화

- **상태:** 진행 방향 제안; 각 트랙 배포 승인은 별도
- **결정:** 감사 및 PM 범위 결정 후 내부 테스트, 제한 베타, 정식 출시 순으로 검토한다.
- **완료 기준:** 각 단계에 대한 실제 증거를 검토하고 다음 단계 진행을 별도로 승인한다.

### DEC-2026-11-15-03 — Top-Down 모듈식 전달과 완료 영역 고정

- **상태:** PM 운영 원칙
- **결정:** 전체 목차와 의존관계를 먼저 정한 뒤 하위 작업을 점진적으로 상세화한다. 개발자와 QA는 자신에게 필요한 작업 맥락만 받되, 관련 위험과 의존성은 누락하지 않는다. 검증 완료 영역은 변경 통제 대상으로 고정한다.
- **근거:** `IrisFit_PM_TopDown_Modular_Workplan_2026-11-15.md`
- **영향:** 구현 작업은 PM이 범위와 우선순위를 승인한 뒤에만 분해·착수한다.

### DEC-2026-11-15-04 — 동일 AI의 개발·QA 단계 분리

- **상태:** PM 운영 원칙
- **결정:** 동일 AI는 먼저 Developer 단계에서 구현 및 자체검증/인계를 완료한 뒤, 코드를 추가 수정하지 않고 별도 QA 단계에서 acceptance criteria에 따라 검토한다. QA는 코드를 직접 고치지 않고 실패를 Developer에게 반환한다.
- **한계:** 동일 AI의 QA는 다른 사람이 수행한 독립 검토와 같지 않다. 필요 시 사람 또는 별도 검토자의 승인을 받기 전까지 이를 미완료로 표시한다.
- **근거:** `IrisFit_PM_TopDown_Modular_Workplan_2026-11-15.md` 8절.

### DEC-2026-11-15-05 — 사용자와 PM의 공동 진행 안내서

- **상태:** 대체됨 — DEC-2026-11-15-29
- **결정:** 당시에는 `IrisFit_PM_User_Walkthrough.md`를 제품 진행 기준, 루트 `Walkthrough.md`를 기술 변경 로그로 분리했다. 후속 사용자 요청으로 주요 개발 이력을 PM 로드맵에 합치고 루트 파일을 통합 문서 진입점으로 변경했다.
- **이유:** 제품 상태, 구현 이력, QA 증거와 다음 단계를 한 Walkthrough 체계에서 함께 확인한다.
- **갱신 책임:** PM은 단계·결정·다음 행동을 갱신하고, 개발자/QA는 요약 및 상세 증거 문서 위치를 제출한다.

### DEC-2026-11-15-06 — 요구사항 ID 기반 PM·개발·QA 프로세스

- **상태:** 사용자가 제시한 공통 프로세스 승인
- **결정:** PM이 요구사항과 기능별 기본설계/우선순위/목표를 정리하고 QA와 설계를 확정한다. 확정 후 Developer가 상세설계를 작성·갱신하여 구현한다. QA는 기본설계의 완료 기준으로 검증하고 결과를 PM에게 전달한다.
- **추적 규칙:** 요구사항 ID를 기본설계, 상세설계, 개발 작업, 테스트, 결함, 변경 요청 및 PM 판정에 연결한다.
- **도구 권고:** 문서를 승인된 기준선으로 유지하고, 가능하면 GitHub Issues/Projects를 상태·담당·의존성 보드로 함께 사용한다. 보드는 요구사항 문서를 대체하지 않는다.
- **근거:** `IrisFit_PM_Dev_QA_Requirements_Process.md`
- **적용:** 현재 감사는 기존 작업 ID `AUD-01`로 추적하고, 감사 결과로 생성되는 구현 요구사항은 `IRIS-REQ-*`로 발급한다.

### DEC-2026-11-15-07 — 현재 상태 감사 및 QA 결과 제출

- **상태:** 조사/재검토 결과 기록; 베타 범위 및 결함 처리 결정은 대기
- **결정/현황:** Developer 감사와 QA 정적 재검토가 완료됐다. QA 전체 상태는 실기기·Play 증거가 없어 `BLOCKED`이며 기존 Release Hold는 유지한다.
- **주요 근거:** `IrisFit_Current_State_Audit_2026-11-15.md`, `IrisFit_Current_State_Audit_QA_Review_2026-11-15.md`
- **제약:** 제품 범위, 점수 의미, 개인정보 처리, 코드 수정 및 Play 배포에 대한 추가 승인은 포함하지 않는다.

### DEC-2026-11-15-08 — 베타 MVP 범위 선택

- **상태:** 사용자가 범위 선택; 상세 기본설계 및 개발 승인 전
- **결정:** 베타 범위 후보는 홈, 선택적 Usage Access, 30초 휴식, 눈 운동, 기기 내 로컬 기록으로 한다. 커뮤니티/릴레이, AI Coach, PRO는 베타에서 숨기는 방향으로 설계한다.
- **미결:** 점수의 사용자-facing 의미, 운동 모드/완료 조건, 로컬 기록 보존/삭제 범위, 알림 및 배터리 최적화 설정.
- **근거:** 사용자의 2026-11-15 범위 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 코드 수정·정책 확정·Release Hold 해제·배포 승인은 포함하지 않는다.

### DEC-2026-11-15-09 — 배터리 컨셉과 휴식 목적

- **상태:** 사용자 승인 원칙; 명칭 체계는 `DEC-2026-11-15-11`로 대체
- **결정:** 배터리 컨셉을 사용해 화면 사용에 따른 소모와 눈 휴식/눈 운동에 따른 충전을 표현한다. 앱의 핵심 목적은 사용자의 휴식을 돕는 것이다.
- **미결:** 구체적인 수치 표시, 산식·임계값, 미측정 표시 및 사용자 문구.
- **근거:** 사용자의 2026-11-15 제품 목적 설명; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 기존 산식 승인, 의료 효능 주장 또는 Release 승인으로 해석하지 않는다.

### DEC-2026-11-15-10 — 베타 눈 운동 범위 및 동시 실행

- **상태:** 사용자 승인
- **결정:** 베타에 Eye Gym과 Smooth Pursuit를 모두 포함한다. 두 운동 중 한 번에 하나만 동작하도록 한다.
- **미결:** 각 모드의 완료 조건, 진행 중 다른 모드 진입 시 처리와 저장/중단 세부 기준은 기본설계 및 상세설계에서 확정한다.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 동시 실행 불가 외에 세션 UX와 완료 판정은 승인된 것으로 간주하지 않는다.

### DEC-2026-11-15-11 — 아이핏 브랜드 및 배터리 명칭 체계 (대체됨)

- **상태:** 사용자 승인 후 `DEC-2026-11-15-12`로 대체
- **결정:** 앱 브랜드 “아이핏 (Eye Fit)”, 핵심 수치 “아이핏 배터리”, 설명 “눈 에너지”, 감소 “배터리 소모”, 회복 “배터리 충전”, 활동 “눈 휴식 / 눈 운동”을 사용하기로 했다.

- **근거:** 사용자의 2026-11-15 명칭 체계 제안; 현재 용어의 기준은 아래 DEC-2026-11-15-12를 참조한다.

### DEC-2026-11-15-12 — IrisFit 브랜드 및 Battery 명칭 체계

- **상태:** 사용자 승인; `DEC-2026-11-15-11`을 대체
- **결정:** 제품 용어를 다음처럼 통일한다.

  | 구분 | 승인 명칭 |
  |---|---|
  | 앱 브랜드 | IrisFit (아이리스핏) |
  | 핵심 기능 | IrisFit Battery |
  | 한글 기능명 | 아이리스핏 배터리 |
  | 핵심 수치 | 눈 에너지 |
  | 회복 활동 | 눈 휴식 / 눈 운동 |

- **미결:** 수치 표시 여부, 산식·임계값, 미측정 표현 및 최종 UI 설명/상태 문구.
- **근거:** 사용자의 2026-11-15 갱신된 명칭 체계 제안; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 용어 승인만을 뜻하며 점수 규칙, 효능 주장, 소스 변경 및 Release 승인은 포함하지 않는다.

### DEC-2026-11-15-13 — 눈 에너지의 비수치 표현 (대체됨)

- **상태:** 사용자 승인 후 `DEC-2026-11-15-14`로 대체
- **결정:** 눈 에너지를 숫자값 없이 배터리 그래픽과 안내 문구로 표현하기로 했으나, 이후 숫자 지표를 표시하기로 변경했다.
- **근거:** 사용자의 2026-11-15 이전 선택; 현재 기준은 DEC-2026-11-15-14 참조.

### DEC-2026-11-15-14 — 눈 에너지 숫자 지표 및 사용자 목적

- **상태:** 사용자 승인; `DEC-2026-11-15-13` 대체
- **결정:** 눈 에너지를 숫자로 표시한다. 제품 목적은 걸음 수 앱이 걸음 수를 실시간으로 보여주는 것처럼, 사용자가 휴대전화를 많이 사용했는지 지표로 알 수 있도록 하는 것이다.
- **미결:** 표시 숫자가 실제 누적 화면 사용시간인지 사용량에 따라 산출한 별도 눈 에너지 값인지, 단위/범위/산식, “실시간” 갱신 주기, 사용량 조회 불가 시 표시, 그래픽을 함께 쓸지 여부.
- **그래픽 검토:** 그래픽도 동일 목적을 지원할 수 있지만, 숫자와 같은 지표·시간 범위를 일관되게 나타내는 보조 표현이어야 한다. 그래픽 단독으로 정확한 수치를 대신하는 것은 승인되지 않았다.
- **근거:** 사용자의 2026-11-15 설명과 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 표시 목적 승인만을 뜻한다. 측정 정의/산식, 권한 범위, 구현 및 Release 승인은 포함하지 않는다.

### DEC-2026-11-15-15 — 눈 에너지 숫자의 의미 (목표 정의 변경으로 대체됨)

- **상태:** 초기 선택은 `DEC-2026-11-15-21`의 사용자 목표 명확화로 대체됨
- **결정:** 눈 에너지 숫자는 별도 산식으로 만든 점수가 아니라 **오늘의 누적 화면 사용시간**을 뜻한다. 사용자는 이 지표로 휴대전화를 얼마나 사용했는지 알 수 있어야 한다.
- **그래픽:** 함께 표시할 경우 오늘 누적 사용시간을 보조하는 시각화로만 사용한다. 그래픽의 채움이 숫자와 다른 의미나 시간 범위를 나타내지 않도록 한다. 그래픽 포함 자체는 아직 결정되지 않았다.
- **미결:** Usage Access 미허용/조회 실패 시 표시, 오늘의 날짜 경계, 데이터 갱신 주기와 지연 안내, 최종 문구.
- **근거:** 사용자의 2026-11-15 선택 및 제품 목적 설명; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 지표 의미의 승인이다. 구현 방식, 백그라운드 모니터링, 그래픽 선택 및 Release 승인은 포함하지 않는다.

### DEC-2026-11-15-16 — 사용시간 갱신 시점

- **상태:** 사용자 승인
- **결정:** IrisFit Battery는 앱이 사용 중일 때 주기적으로 갱신하고, 앱을 다시 열거나 foreground로 돌아올 때도 최신 사용시간을 다시 조회한다.
- **미결:** 앱 사용 중 정확한 갱신 주기, 시스템 집계 지연 안내, Usage Access 미허용/조회 실패 시 표시.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 앱 사용 중 갱신 및 복귀 시 새로고침의 UX 방향 승인이다. 백그라운드 연속 추적, 시스템이 보장하지 않는 초 단위 실시간 정확성, 구현 및 Release 승인은 포함하지 않는다.

### DEC-2026-11-15-17 — 앱 사용 중 사용시간 갱신 간격

- **상태:** 사용자 승인
- **결정:** 앱이 열린 동안 오늘 누적 화면 사용시간을 1분마다 새로고침한다. 앱 복귀 시에도 새로고침한다.
- **미결:** Android 사용량 집계 반영 지연, 시간 표기 형식, 권한/조회 불가 시 표시.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 화면 갱신 주기 결정이며 OS 집계값이 1분 이내 정확히 반영된다는 보장은 아니다.

### DEC-2026-11-15-18 — 누적 사용시간 숫자 형식

- **상태:** 사용자 승인
- **결정:** 오늘 누적 화면 사용시간은 시간과 분으로 표시한다(예: `2시간 15분`).
- **미결:** Usage Access 미허용/조회 실패 시 상태와 안내 문구, 집계 지연 안내.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 숫자 표시 형식 결정이다. 데이터 정확성, 갱신 주기 보장, 미측정/오류 정책 및 구현 승인은 포함하지 않는다.

### DEC-2026-11-15-19 — Usage Access 미허용/조회 불가 표시

- **상태:** 기존 Usage Access 측정안에 대한 선택; 실제 시청시간 목표(DEC-2026-11-15-21)로 기술 적용은 보류

- **결정:** Usage Access를 허용하지 않았거나 사용량 값을 확인할 수 없으면 `미측정`으로 표시하고 선택적 권한 안내를 제공한다. 실제 측정된 0분과 미측정 상태를 구분한다.
- **미결:** 조회 오류별 상세 안내 문구/재시도 UX 및 OS 집계 지연 표시.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 화면 상태/권한 안내 방향 승인이다. Usage Access는 선택 사항이며, 휴식·운동 등 앱 핵심 기능을 막지 않는다. 구현 및 Release 승인은 포함하지 않는다.

### DEC-2026-11-15-20 — 화면 사용시간 그래픽 병행

- **상태:** 표시 방향 사용자 선택; 실제 시청시간 센싱 및 저전력 가능성 확인에 종속
- **결정:** 오늘 누적 화면 사용시간을 숫자로 표시하고, 같은 지표를 나타내는 보조 그래픽도 함께 제공한다.
- **미결:** 그래픽 형태/눈금/눈금의 기준 범위, 데이터 지연 및 미측정 상태에서 그래픽 표시 방식.
- **근거:** 사용자의 2026-11-15 선택; `IrisFit_PM_Beta_MVP_Basic_Design_2026-11-15.md`
- **제약:** 그래픽이 숫자와 같은 오늘 누적 사용시간을 반영해야 한다. 눈 건강 상태나 진단 결과를 뜻하는 표현으로 사용하지 않는다. 구체적 UI는 상세설계 제안 후 PM 승인을 받는다.

### DEC-2026-11-15-21 — 실제 시청시간 목표와 저전력 제약

- **상태:** 사용자 목표 명확화; 정적 가능성 조사 완료, 기술 구현 및 실기기 시험 미승인
- **결정/요구:** 핵심 지표의 목표는 사용자가 휴대전화를 실제로 손에 들고 화면을 보는 시간이다. 모든 휴대전화 활동이나 단순 화면 켜짐 시간은 목표가 아니다. 책상에 놓인 기기나 잠자는 동안 켜진 화면은 시청시간으로 세면 안 된다는 사용 사례가 제시됐다.
- **제약:** 앱은 24시간 실행되더라도 배터리를 거의 소모하지 않는 가벼운 앱이어야 한다. 사용자는 카메라/센서 기반 접근의 가능성 조사를 원하지만 상시 카메라 실행이나 배터리 비용을 수용한 것은 아니다.
- **현재 기술 근거:** `ScreenTimeManager`는 모든 패키지의 foreground 사용시간을 더하며 실제 시청을 판별하지 않는다. `SmartDevicePoseDetector`는 기기 자세 신호를 분류하지만 시선을 확인하지 않는다. 디버그 `GazeTracker`는 얼굴/눈 신호를 처리하지만 현재 실제 시선 방향을 측정하지 않고, 카메라 실험 서비스는 2분 제한이다. 상세 증거 및 Android 제약은 `IrisFit_PM_Actual_Viewing_Time_Low_Power_Feasibility_2026-11-15.md` 참조.
- **다음:** 정적 검토 결과 앱이 24시간 대기하는 것과 센서/카메라로 능동 측정하는 것은 구분해야 하며, 카메라 FGS는 사용자-visible 시작/알림 및 Android/Play 선언을 수반할 수 있다. Android 공식 SensorManager 문서도 센서를 정지하지 않으면 몇 시간 내 배터리를 소모할 수 있다고 경고한다. 연결 기기와 테스트 환경이 없어 오탐/미탐/전력 측정은 아직 실행 불가 상태다. 사용자가 측정 활성화 방식과 허용 가능한 배터리·오류 한계를 승인한 뒤 실기기 시험 여부를 결정한다.
- **영향:** DEC-2026-11-15-15의 화면 사용시간 정의는 대체된다. 숫자/그래픽/갱신/표기 UX의 이전 선택(DEC-2026-11-15-16~20)은 실제 지표의 기술적 실현 가능성에 종속되며, 지금은 코드 개발 기준선이 아니다.

### DEC-2026-11-15-22 — 실제 시청시간 목표 유지 및 범용성 기준

- **상태:** 사용자 승인 — 실기기 타당성 검증 선행; 구현/배포 승인 아님
- **결정:** 실제 시청시간 목표를 유지하고 Galaxy S23 및 Galaxy Z Fold7을 초기 시험 후보로 삼는다. Android 13/API 33 이상을 범용 지원 목표로 하며, 기기/권한/센서 상태 등으로 측정할 수 없는 경우 `미측정`으로 명확히 표현한다.
- **기기 범위 한계:** 두 기기는 모두 삼성 제품이므로 결과만으로 교차 제조사·전체 Android 호환성을 주장하지 않는다.
- **전력 비교:** 모든 기기에 절대 배터리 사용 상한을 일률 적용하지 않는다. 후보와 비활성 기준선을 동일 기기/유사 조건에서 반복 비교하고 기기별 차이 및 변동을 보고한다.
- **미결:** 정확한 시험 기기 OS/One UI 버전, 허용 배터리 증가, 오탐/미탐 기준, 시험 절차와 실제 시험 승인.
- **근거:** 사용자의 2026-11-15 선택 및 `IrisFit_PM_Actual_Viewing_Time_Feasibility_Test_Plan_2026-11-15.md`.
- **제약:** 센서/카메라 측정 구현, 지속 추적, 숫자/정확도 표시, 실기기 시험, Release Hold 해제는 별도 승인 전까지 허용하지 않는다.

### DEC-2026-11-15-23 — ViewingTimeEvaluator 통합 PASS 재검토

- **상태:** PM 판정 — PARTIAL PASS, 관련 작업 재개방
- **결정:** 오늘 누적 화면 사용시간 UI에서 evaluator 추정치가 노출되지 않는 코드 경로는 정적 검토 범위에서 부분 승인한다. 그러나 evaluator의 상태 분기, 센서/사용량 실패 구분, 화면 꺼짐/구성 변경 및 테스트 근거가 충분하지 않아 개발·QA 통합 PASS 및 기능 완료를 승인하지 않는다.
- **필요 후속:** Developer 수정/증거 인계 → QA 기준별 재검수 → PM 최종 판정.
- **근거:** `IrisFit_PM_Actual_Viewing_Time_Second_Review_2026-11-15.md`.
- **제약:** 관련 실기기 정확도·배터리 검증은 `NOT RUN`; Release Hold 유지.

### DEC-2026-11-15-24 — 전체 로드맵의 Walkthrough 기준 갱신 (대체됨)

- **상태:** 대체됨 — DEC-2026-11-15-29
- **결정:** 당시에는 `IrisFit_PM_User_Walkthrough.md`를 제품 로드맵 기준으로, 저장소 `Walkthrough.md`를 별도 기술 변경 로그로 유지했다. 사용자의 후속 요청에 따라 양쪽 내용을 통합 Walkthrough로 합쳤다.
- **현재 위치:** 실제 시청시간 evaluator 개발 수정 인계 및 QA 재검수 대기. 내부 테스트 진입 단계가 아니다.
- **근거:** 사용자 요청(2026-11-15) 및 DEC-2026-11-15-23.

### DEC-2026-11-15-29 — Walkthrough 통합 및 단일 기준 갱신

- **상태:** 적용 — 중복본 제거까지 완료
- **결정:** 사용자의 요청에 따라 PM 제품 로드맵과 기존 저장소 `Walkthrough.md`의 개발 이력/검증 한계를 통합한다. 저장소 루트 `Walkthrough.md`는 진입점이며, `task/IrisFit_PM_User_Walkthrough.md`는 로드맵과 개발 이력을 합친 기준 본문이다. 두 문서가 별도 상태를 관리하지 않도록 역할을 명확히 한다.
- **정리:** 통합에 필요한 과거 이력을 본문에 옮긴 뒤 `docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`를 삭제했다. 과거 감사 보고서의 출처 기록은 변경하지 않았다.
- **근거:** 사용자 요청(2026-11-15); 통합 Walkthrough에 과거 2026-11-12/13 구현 이력 및 현재 `aad01ee` 검토 상태 반영.

### DEC-2026-11-15-30 — 중복 Walkthrough 문서 삭제

- **상태:** 완료
- **결정:** 사용자 선택에 따라 통합된 과거 중복 문서 세 개를 삭제한다. 루트 `Walkthrough.md` 및 `task/IrisFit_PM_User_Walkthrough.md` 통합 기준 문서는 유지한다.
- **대상:** `docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`.
- **영향:** 현재 기준 링크는 루트 진입점과 통합 본문으로 단순화한다. 기존 감사 문서의 역사적 출처 참조는 보존한다.

### DEC-2026-11-15-25 — Developer 재인계 및 QA 재검수 단계

- **상태:** PM 사전 검토 완료; QA 결과 대기
- **결정:** Developer의 `3218622` 인계와 전체 unit test 결과를 확인했다. 단위 테스트는 59 passed, 0 skipped, 0 failed로 재현됐으나 PM 사전 검토에서 실제 UsageStats 오류/0분 구분, 실제 센서 가용성 검증 및 주장된 분기 테스트 범위에 추가 확인이 필요하다. 사용자 UI의 추정치 비노출 경로만 정적 범위에서 부분 승인한다.
- **다음 단계:** QA가 PM의 기존 재개방 지시와 최신 소스/테스트를 독립 검수한다. 실기기 배터리·시선 정확도 시험은 NOT RUN으로 유지한다. 완료/Release 승인은 보류한다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_2026-11-15.md`.

### DEC-2026-11-15-26 — QA PARTIAL PASS 및 기능 완료 보류

- **상태:** PM 판정 — 단위 테스트/정적 UI 범위 부분 수용; 실제 지표 기능 완료 미승인
- **결정:** commit `66aa763`의 전체 JVM 단위 테스트 61 passed/0 skipped/0 failed를 확인한다. evaluator 오류/음수 입력 방어 테스트는 해당 입력 계약 범위에서 수용한다. 그러나 실제 UsageStats 서비스 부재/빈 결과는 `ScreenTimeManager`에서 0으로 축약될 수 있어 실제 0분과 완전히 구분되지 않는다. 센서 freshness는 인자로 주입되는 테스트 조건이며 하드웨어 검증이 아니다.
- **다음:** 운영 UsageStats 조회 결과를 실제 0과 구분할 필요가 있는 UI/API 경로에 대해 별도 구현 및 회귀 테스트를 수행한다. 실기기 시선 정확도·전력 시험은 NOT RUN. Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_66aa763_2026-11-15.md`.

### DEC-2026-11-15-27 — commit `5018854` Developer 인계 부분 수용

- **상태:** PM 부분 수용 — 후속 수정 및 QA 재검수 대기
- **결정:** `ScreenTimeQueryResult` 도입, formatted API의 오류 표시, evaluator public API에서 mock 인자 분리와 전체 JVM 테스트 61/0/0을 확인했다. 그러나 `getTodayScreenTimeMinutes()`가 조회 실패를 0으로 바꾸며 위젯·세션·AI 코치 소비처가 이를 사용한다. evaluator는 detector 객체 존재만으로 freshness를 판단한다. 따라서 제품 완료 및 `DEV-02` 완료는 보류한다.
- **다음:** 모든 운영 소비처에서 typed 상태를 처리하고, 실제 센서 registration/freshness 기준으로 fail-closed 처리한 뒤 QA 재검수한다. 실기기 정확도·배터리 시험 `NOT RUN`; Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_Developer_Handoff_Review_5018854_2026-11-15.md`.

### DEC-2026-11-15-28 — commit `aad01ee` QA PARTIAL PASS

- **상태:** PM 판정 — PARTIAL PASS; 개발 작업 재개방, Release Hold 유지
- **결정:** HEAD `aad01ee` 및 전체 JVM 테스트 61 passed/0 skipped/0 failed를 확인했다. typed query API와 실제 detector freshness 연결은 진전으로 수용한다. 그러나 `getTodayScreenTimeMinutes()`는 오류를 계속 0으로 바꾸며 위젯/세션/AI 코치에서 사용된다. 센서 freshness는 첫 이벤트 전 초기화 시각으로 최대 5초간 참일 수 있고, 제출된 테스트는 실제 detector freshness 경계를 검증하지 않는다. QA의 조회 실패 축약 방지 PASS 주장은 승인하지 않는다.
- **다음:** 모든 소비처의 typed 결과 처리, 센서 첫 이벤트 이후 freshness 적용 및 관련 경계 테스트를 보완한 뒤 QA 재검수한다. 실기기 정확도/배터리는 NOT RUN, Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_aad01ee_2026-11-15.md`.

### DEC-2026-11-15-31 — commit `b808392` QA PARTIAL PASS

- **상태:** PM 판정 — PARTIAL PASS; 운영 경로 수정 재개방, Release Hold 유지
- **결정:** HEAD `b808392`와 전체 JVM 테스트 61 passed/0 skipped/0 failed를 확인했다. 최초 센서 이벤트 전 guard 및 일부 typed 처리 변경은 인정한다. 그러나 Manifest 활성 Widget 1은 `BitmapWidgetProvider`를 사용하고 해당 코드에 legacy 실패-0 helper가 남아 있다. `MainScreen.AiCoachTab`도 같은 helper를 사용한다. 미측정일 때 calculator는 score/status를 계속 반환하고, detector는 timestamp/accuracy 검증 전에 freshness timestamp를 갱신한다. QA의 모든 운영 소비처 PASS는 현재 소스와 불일치해 승인하지 않는다.
- **다음:** 활성 Widget 1과 AI 코치 포함 전체 호출처 typed 처리, 미측정 점수 비노출, 유효 센서 이벤트만 freshness 갱신 및 경계 테스트 후 QA 재검수. 실기기 시험 NOT RUN, Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_b808392_2026-11-15.md`.

### DEC-2026-11-15-32 — commit `8a72672` QA PARTIAL PASS

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** HEAD `8a72672` 및 전체 JVM 테스트 61 passed/0 skipped/0 failed를 확인했다. Widget 1/AI 코치 typed query와 첫 이벤트 전 freshness guard는 수용한다. 단, `screenTimeMeasured=false`여도 score/status 계산과 사용자 표시가 이어지고, AI 코치가 QueryError도 permission 상태처럼 취급한다. 센서는 timestamp 범위만 검사한 다음 payload 유효성 검사 전에 freshness 시간을 갱신하며, 해당 경계 자동 테스트도 확인되지 않았다. QA의 모든 운영 소비처 및 유효 이벤트 PASS는 제한된 범위에서만 인정한다.
- **다음:** 미측정 score/status 노출 방지, QueryError와 PermissionDenied UI 분리, payload 검증 후에만 freshness 갱신 및 요구사항별 테스트를 추가하고 QA 재검수한다. 실기기 정확도/배터리는 NOT RUN, Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_8a72672_2026-11-15.md`.

### DEC-2026-11-15-33 — commit `9dd7059` QA PARTIAL PASS

- **상태:** PM 판정 — PARTIAL PASS; 소비처/UI 및 센서 검증 수정 재개방, Release Hold 유지
- **결정:** HEAD `9dd7059` 및 JVM 테스트 61 passed/0 skipped/0 failed를 확인했다. Calculator의 `-1`/`UNMEASURED` 모델 분기는 수용한다. 그러나 두 위젯 경로와 AI 코치가 -1%를 표시하고, 공유/목표/추천/추세가 미측정 값을 점수처럼 처리한다. 센서 freshness는 timestamp 검사 후 payload 검증 전에 갱신된다. QA의 UI·위젯 및 payload 검증 PASS는 현재 소스와 일치하지 않는다.
- **다음:** 모든 consumer의 명시적 미측정 렌더링/분기, 센서 타입별 payload 선검증, 해당 UI/센서 경계 자동 테스트를 추가한 후 재검수한다. 실기기 정확도/배터리는 NOT RUN, Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md`.

### DEC-2026-11-15-34 — Galaxy S23 연결 후 제한 스모크 체크

- **상태:** 제한된 앱 실행/센서 가용성 스모크 PASS; 타당성 시험 및 출시 승인 아님
- **결정:** Galaxy S23 `SM-S916N`, Android 16/API 36에서 앱 version 1.0.0 실행을 확인하고 가속도계·자이로·근접·조도 센서가 OS 센서 목록에 있음을 확인했다. 앱 크래시가 확인되지 않은 것은 짧은 실행 범위에 한정한다. 센서 이벤트 freshness/분류, 실제 시청 정확도는 검증하지 않았다. 기기는 USB 충전 중이므로 배터리 비교는 NOT RUN이다.
- **다음:** 앱 소비처/센서 payload 코드 수정과 JVM 경계 테스트를 먼저 완료한다. 이후 배터리 시험은 충전 해제 및 승인된 기준선·시험 조건/합격 기준을 확보한 뒤 별도 진행한다. Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Device_Smoke_Check_2026-11-15.md`.

### DEC-2026-11-15-35 — commit `6d2b718` 및 제한 실기기 결과 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** HEAD `6d2b718`, JVM 테스트 61/0/0 및 제한 실기기 UI를 확인했다. 홈 화면/AI Coach 베타 안내 동작은 기기에서 관찰했으나 화면 사용시간 표시는 UsageStats/ground-truth 대조가 아니고 자세 정확도를 입증하지 않는다. 미측정 처리는 일반 위젯/공유에 개선됐지만 활성 bitmap widget, AI Coach, 목표/릴레이/추세의 -1 점수 소비가 남는다. Detector는 finite/empty 및 timestamp를 확인하지만 타입별 길이/값 범위 확인 전에 freshness를 갱신한다. 커밋에는 관련 UI/센서 경계 테스트 변경이 없다.
- **다음:** 모든 활성 consumer를 미측정 분기로 바꾸고 센서 타입별 validation을 classifier/freshness 갱신 전에 수행, 소비처/잘못된 payload 경계 테스트를 제출한다. USB 충전 중 배터리 실측과 actual-viewing accuracy는 NOT RUN. Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md`.

### DEC-2026-11-16-36 — commit `d301df4` 및 최신 실기기 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** HEAD `d301df4`, JVM 테스트 62 passed/0 skipped/0 failed 및 현재 Debug 빌드의 Galaxy S23+ `SM-S916N` 설치/실행을 확인했다. 활성 bitmap 위젯의 미측정 렌더링과 공유 문구 개선은 수용한다. 하지만 AI Coach/Goal/추세의 `-1` 소비가 남고, proximity `maximumRange`/알 수 없는 센서 타입이 freshness 갱신 전에 완전히 거부되지 않는다. 실제 `SM-S916N`은 현재 device model mapping상 S23 profile이 아닌 generic으로 분류된다. FeasibilityEstimate mock 테스트는 `poseDetector=null`, `isSensorFresh=true`를 주입하므로 실제 listener 검증이 아니다. 기기 화면 `책상 위 놓임`, `100%`, `2분`은 관찰된 UI일 뿐 실제 시청시간/자세 정확도 검증이 아니다.
- **다음:** 모든 consumer의 미측정 분기, sensor type별 전체 사전검증, SM-S916N 모델 mapping, UI/sensor 경계 자동 테스트를 보완해 QA 재검수한다. 배터리 비교는 USB 충전 해제 및 승인 기준 전까지 NOT RUN, 실제 시청 정확도도 NOT RUN. Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_d301df4_2026-11-16.md`.

### DEC-2026-11-16-37 — commit `311ab4c` 및 QA 증거 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** HEAD `311ab4c`에서 JVM XML 기준 63/0/0을 확인했고 `SM-S916N`을 S23 계열로 분류하는 코드/테스트를 수용한다. 센서 입력의 유한성·길이·허용 type·timestamp 및 proximity `maximumRange` 유효성 검사 뒤 freshness를 갱신하는 순서도 확인했다. 단, proximity sample value가 `maximumRange`보다 큰 경우는 거르지 않으며 detector boundary 테스트가 없다. AI Coach/Goal/Wellness gauge/trend의 미측정값 노출·소비가 남아 있어 전체 기능 완료는 보류한다.
- **기기 검증:** 최신 Debug 앱을 Galaxy S23+에 재설치/실행했고 `책상 위 놓임`, `99%`, `15분`을 관찰했다. 이는 UI smoke이며 ground-truth 정확도 검증이 아니다. 배터리 status가 충전 중이므로 drain 비교는 NOT RUN. 실제 시청시간/자세 정확도와 센서 freshness 실기기 검증 역시 NOT RUN이다.
- **다음:** 모든 score consumer의 `UNMEASURED` 분기와 UI/sensor 경계 자동 테스트를 보완한 뒤 QA 재검수한다. accuracy/battery 기준과 절차 승인 전까지 해당 실기기 검증은 진행 완료로 표시하지 않는다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_311ab4c_2026-11-16.md`.

### DEC-2026-11-16-38 — commit `efcd86b` 및 일간 추세 미측정 처리 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** 일간 trend chart가 미측정 상태에서 placeholder를 보여주고 negative 라벨을 `미측정`으로 표시하는 변경은 코드 범위에서 수용한다. 그러나 주간/월간 trend는 휴식/운동 횟수만으로 가상의 30–100 점수를 만들어 실제 측정치처럼 표시한다. MainScreen AI/Goal/relay 및 Wellness gauge/legend의 미측정 소비도 남아 있다. JVM XML은 63/0/0이지만 이번 변경에 UI/sensor boundary test가 없고 QA의 UI 렌더링 PASS 주장은 독립 테스트로 입증되지 않았다.
- **기기 검증:** Debug 설치 및 MainActivity 실행 시도는 확인했으나 현재 UI dump에서 추세/점수 화면을 검증하지 못했다. Actual viewing/posture accuracy, sensor freshness 및 battery/heat는 NOT RUN이다.
- **다음:** 모든 기간별 trend data source를 실측/정의된 상태에 연결하고 모든 활성 consumer를 `UNMEASURED` 분기로 처리한다. UI 및 sensor boundary 자동 테스트 후 QA 재검수한다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_efcd86b_2026-11-16.md`.

### DEC-2026-11-16-39 — commit `ce6d320` 및 전체 추세 탭 미측정 처리 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** 현재 전역 score가 미측정이면 일/주/월 탭 본문에서 공통 placeholder를 보여주는 변경을 코드 범위에서 수용한다. 그러나 주간/월간은 현재 measured 상태를 해당 기간 데이터 존재의 증거로 사용하지 않고, 휴식/운동 기록만으로 임의 점수를 표시한다. MainScreen AI/Goal/relay와 Wellness gauge/legend의 미측정 consumer도 잔존한다. 63 JVM 테스트 통과는 재확인했으나 UI/sensor boundary test가 없어 QA 렌더링 PASS를 독립 검증하지 못했다.
- **다음:** 주간/월간 각 bucket의 측정값 유무를 실제 데이터 소스에서 확인하고, 모든 활성 consumer를 미측정 분기로 처리한다. UI 및 sensor freshness 경계 자동 테스트 후 QA 재검수한다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce6d320_2026-11-16.md`.

### DEC-2026-11-16-40 — commit `25fea7e` 및 과거 추세 점수 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** 과거 일간 chart에서 기록이 없는 날짜를 `-` 및 중립 바로 표시하고, 휴식/운동 횟수 기반 70+ 공식은 제거한 개선을 수용한다. 그러나 기록이 있는 날짜는 `screenTimeMinutes=120`을 고정 주입해 점수를 생성하며, 이는 actual measured data가 아니다. 4주 월간 차트는 기존 임의 공식이 남아 있다. MainScreen/Wellness 미측정 consumer와 UI/sensor 경계 테스트도 미완료다. JVM XML 63/0/0을 확인했으나 이번 커밋에 chart/UI 테스트는 추가되지 않았다.
- **다음:** 각 과거 날짜/주차의 실제 측정 데이터 저장·조회 상태를 데이터 모델에서 제공하고, 실측이 없으면 `미측정` 처리한다. 월간 및 기타 활성 consumer를 함께 수정하고 UI/sensor 경계 테스트로 검증한다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_25fea7e_2026-11-16.md`.

### DEC-2026-11-16-41 — commit `25fea7e` 및 주간/월간 가상 점수 재검토

- **상태:** 과거 판정 — 후속 commit `44c1606`에서 weekly/monthly synthetic formula 제거
- **결정:** `25fea7e` 당시에는 기록 있는 날짜의 120분 가정 및 월간 활동횟수 수식이 남아 있었다. 후속 commit에서 해당 산식 제거 및 미측정 placeholder 처리가 반영됐다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_25fea7e_2026-11-16.md`.

### DEC-2026-11-16-42 — commit `44c1606` PM 재검토

- **상태:** PM 판정 — PARTIAL PASS; 잔여 수정 재개방, Release Hold 유지
- **결정:** 주간/월간 activity-count 임의 점수 산식 제거와 과거 미측정 placeholder 처리를 수용한다. 과거 실제 측정 score 저장/조회는 지원되지 않아 과거 trend는 아직 미측정이다. MainScreen AI/Goal/relay 및 Wellness gauge/legend의 미측정 consumer와 UI/sensor 경계 테스트도 남는다. JVM XML 63/0/0 확인.
- **다음:** 모든 활성 consumer를 미측정 분기로 처리하고 sensor/UI boundary tests 추가 후 QA 재검수한다. 과거 measured data source가 없는 동안 historical score를 만들지 않는다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_44c1606_2026-11-16.md`.

### DEC-2026-11-16-43 — commit `f05cb53` PM 재검토

- **상태:** PM 판정 — PARTIAL PASS; 수정 재개방, Release Hold 유지
- **결정:** GoalAchievementCard는 미측정 시 잔량 안내 및 달성 판정 차단, AiCoachTab은 점수 대신 미측정 문구를 표시하는 코드 개선을 수용한다. 그러나 relay 추천은 `-1`을 위험 점수로 처리하고 Toast에 `-1%`를 노출할 수 있다. Wellness gauge/legend도 미측정 점수를 0/danger처럼 시각화할 수 있다. UI 및 sensor callback boundary 자동 테스트는 없다. JVM XML 63/0/0 확인.
- **다음:** relay 추천과 Wellness gauge/legend를 미측정 상태로 처리하고 모든 score consumer 및 경계 테스트를 완료한 뒤 QA 재검수한다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f05cb53_2026-11-16.md`.

### DEC-2026-11-16-44 — commit `23ee202` PM 재검토

- **상태:** PM 판정 — PARTIAL PASS; UI/sensor 경계 검증 재개방, Release Hold 유지
- **결정:** Relay는 미측정 또는 음수 점수에서 score 기반 위험 추천과 점수 Toast를 피하고 일반 주제를 사용한다. Wellness gauge/범례는 미측정 시 중립 처리한다. GoalAchievementCard/AiCoachTab guard도 유지됨을 코드에서 확인했다. 이 변경은 코드 경로 범위에서 수용하되 Compose 렌더링 테스트 PASS로 간주하지 않는다. relay fallback의 “맑은 눈 유지” 문구는 상태를 긍정적으로 암시하지 않도록 더 중립적인 표현을 검토한다. JVM XML 63/0/0을 확인했으나 이번 변경의 UI 분기나 detector callback/freshness를 직접 검증하는 테스트는 없다.
- **다음:** relay 문구 검토와 UI consumer 및 sensor freshness boundary 자동 테스트 후 QA 재검수한다. 실기기 viewing/posture accuracy와 battery/heat는 NOT RUN이며 임계값/절차 승인도 미완료다.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_23ee202_2026-11-16.md`.

### DEC-2026-11-16-45 — commit `ce8ac49` PM 재검토

- **상태:** PM 판정 — PARTIAL PASS; UI 표현 및 boundary 검증 재개방, Release Hold 유지
- **결정:** relay 제목이 미측정 시 눈 상태를 긍정/부정으로 단정하지 않게 바뀐 것은 수용한다. 다만 같은 분기의 Toast는 일반 추천을 “맞춤”이라고 표현하고, Wellness 미측정 상태의 mascot은 행복한 눈 자산을 사용한다. 미측정에 관한 사용자-facing 표현이 데이터 상태를 정확히 나타내는지 보완이 필요하다. JVM XML 63/0/0은 확인했으나 이번 커밋은 UI 문구 변경만 포함하며 UI 렌더링 또는 sensor callback/freshness 테스트는 없다.
- **다음:** Toast와 Wellness 미측정 표시를 정리하고 UI/sensor boundary tests 제출 후 QA 재검수한다. 실기기 정확도/전력은 NOT RUN; 기준/프로토콜 미승인.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_ce8ac49_2026-11-16.md`.

### DEC-2026-11-16-46 — commit `f7a8d2d` PM 재검토

- **상태:** PM 판정 — PARTIAL PASS; UI/sensor 경계 검증 재개방, Release Hold 유지
- **결정:** 미측정 relay Toast에서 “맞춤” 표현을 없앤 점과 Wellness UNMEASURED mascot에 중립 아이콘을 연결한 점을 소스에서 확인해 수용한다. Gradle JVM XML은 63/0/0이다. 다만 commit은 전용 UI 테스트를 추가하지 않았으므로 화면 렌더링 PASS 주장은 입증되지 않았다. sensor callback/freshness 테스트와 실기기 accuracy/battery/heat도 미실시다.
- **다음:** UI 소비처와 sensor freshness 경계 테스트를 자동화하고 QA 재검수한다. 정확도 및 전력 기준/프로토콜은 미승인, Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Actual_Viewing_Time_QA_Review_f7a8d2d_2026-11-16.md`.

### DEC-2026-11-16-47 — `BATCH-UNMEASURED-01` 배치 검토

- **상태:** PM 판정 — PARTIAL PASS; 센서 오류처리/이벤트 경계 재작업, Release Hold 유지
- **결정:** `ce8ac49`→`67c4dd4` 변경 범위의 relay/Wellness 미측정 표현, calculator 미측정 테스트, detector의 초기 `isFresh=false` 테스트를 수용한다. Gradle JVM XML은 65/0/0. 초기 상태 테스트는 callback freshness를 검증하지 않는다. Detector가 `Throwable`을 삼켜 service/Handler 오류를 조용히 null로 바꾸는 것은 진단 및 안전한 상태 전이에 충분하지 않으므로 수정이 필요하다. QA 배치의 전체 PASS는 보류한다.
- **다음:** 예상 가능한 예외를 명시적으로 처리/로깅하고 detector availability/freshness 상태를 fail-closed하게 만들며 callback 유효·무효 경계 테스트를 추가한다. 정확도/전력은 NOT RUN; Release Hold 유지.
- **근거:** `C:\Users\youns\AndroidStudioProjects\task\IrisFit_PM_Batch_Review_BATCH-UNMEASURED-01_2026-11-16.md`.

## 진행 추적

| ID | 작업 | 상태 | 담당 | 다음 결과 |
|---|---|---|---|---|
| AUD-01 | Release 구현·기능·권한·데이터 읽기 전용 감사 | 완료 — 정적 조사 | 개발자 | `IrisFit_Current_State_Audit_2026-11-15.md` |
| AUD-02 | dirty worktree/HEAD 기준점 기록 | 완료 | 개발자 | branch/HEAD, 24 modified, 34 untracked 기록 |
| QA-01 | 감사 결과 acceptance criteria 재검토 | 완료 — 전체 QA BLOCKED | QA | `IrisFit_Current_State_Audit_QA_Review_2026-11-15.md` |
| PM-01 | 베타 MVP 범위 및 제품 목표 승인 | 부분 완료 — 실제 시청시간 목표 유지, Android 13+ 및 `미측정` 결정 | PM/사용자 | 정확도·전력 기준 및 시험 절차 결정 |
| DEV-02 | ViewingTimeEvaluator/UsageStats 상태·노출 리팩터링 | `BATCH-UNMEASURED-01`, `67c4dd4`; JVM 테스트 65/0/0 | Developer | sensor init 오류 처리 및 callback freshness 경계 테스트 재작업 |
| QA-03 | DEV-02 재검수 | 배치 `BATCH-UNMEASURED-01` PARTIAL PASS — PM 대조 검토 완료 | QA/PM | 재작업 후 callback/UI boundary와 회귀 재검수 |
| TEST-01 | Galaxy S23/Z Fold7 타당성 시험 | Galaxy S23+ 기본 UI 스모크 완료; 정확도/전력 기준·시험 미승인 | PM/Developer/QA | 승인된 시험 기준, 센서 이벤트·시청 정확도 및 충전 해제 후 기기별 배터리 결과 |
| REL-01 | 내부 테스트 진입 및 Play 배포 승인 | 미승인 | PM/소유자 | 별도 명시적 승인 |

## 신규 기록 템플릿

각 신규 문서는 작성일, 역할, 앱/빌드 기준, 상태, 입력 근거, 실제 확인 증거, 미검증 항목을 머리말에 포함한다.

PM 결정은 고유 ID, 상태(제안/승인/거절/대체), 결정자, 배경, 선택안, 대안, 영향, 후속 작업 ID 및 근거를 기록한다.

---

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 분석 및 결정 이력을 지속적으로 추적할 수 있게 하여, 제품 범위와 출시 상태가 어떤 증거에 근거하는지 이후에도 확인 가능하게 한다.
* **상세 요구사항 (What):**
  - 기능 1: 실질적인 감사·개발·QA 결과를 날짜가 포함된 Markdown으로 기록하고 이 인덱스에 연결한다.
  - 기능 2: 과거 보고서는 원본 그대로 보존하고 현재 결과는 새 날짜 문서에 기록한다.
  - 기능 3: 사실·분석·제안·PM 승인·미확인 항목과 개발자 자체 검증/QA 독립 검증을 구분한다.
  - 기능 4: 문서 작성 시 테스트 명령, 실행 환경, 결과, 로그 위치, 산출물 ID 및 제한 사항을 기록한다.
* **예외 처리 및 엣지 케이스:** 상충 지시, dirty worktree, 실패/미실행 테스트, 외부 접근 제한 및 증거 부족을 숨기지 않는다. 기존 파일·변경을 승인 없이 삭제하거나 덮어쓰지 않는다.
* **Google Play 정책 체크포인트:** 정책 준수 결론은 현재 제출 시점의 기준과 증거를 기록한다. 개인정보/Data safety, 권한, SDK 및 건강 효능에서 미확인 사항은 준수 완료로 표시하지 않는다.
* **완료 조건 (Acceptance Criteria):** (1) 새 보고서가 날짜·상태·근거·한계를 포함한다. (2) 인덱스에 문서와 관련 결정/작업 상태가 연결된다. (3) 과거 결과와 최신 결과가 혼동되지 않는다. (4) 비밀정보나 민감한 사용자 데이터가 문서화되지 않는다.
