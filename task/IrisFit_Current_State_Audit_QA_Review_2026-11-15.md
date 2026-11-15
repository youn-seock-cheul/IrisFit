# IrisFit 현재 상태 감사 QA 재검토

- 작성일: 2026-11-15
- 역할: QA — `AUD-01` 감사 산출물 기준 재검토
- 검토 대상: `IrisFit_Current_State_Audit_2026-11-15.md`
- 연계 작업: `AUD-01`, 기준점 `AUD-02`, QA 작업 `QA-01`
- 기준 HEAD: `1abd6d2402c1a63f62954efa8b333a8778d44f8d` (`master`)
- QA 상태: **BLOCKED — 정적 감사/자동 검증은 검토 가능하나 사용자 여정·기기·Play 증거 부족**
- 역할 독립성: Developer와 QA는 동일 AI다. 역할/단계는 분리했지만 사람의 독립 QA나 Play 정책 승인과 동등하지 않다.

## 1. QA 결론

Developer 감사의 주요 코드 관찰 및 테스트 결과를 재검토했다. F01–F06 및 F08–F09는 정적 코드/Gradle/lint 증거와 일치한다. F07의 경우 현재 Release 화면은 AI를 “검토 중”으로 표시하고 대화 UI 호출을 확인하지 못했으므로, 활성 AI 데이터 흐름 결함으로 단정하지 않도록 우선순위를 **P1에서 P2(활성화 전 결정)**로 낮춘 수정이 타당하다. F04는 선언만 된 community card/string을 노출 증거로 사용하지 않고, 실제 홈에서 호출되는 local demo widget과 public challenge를 근거로 삼도록 수정된 것을 확인했다.

단위 테스트 및 Release 설정 검증은 통과했지만, 실제 Android 권한·알림·위젯·센서·프로세스 종료·언어별 사용자 여정은 실행되지 않았다. 따라서 제품 QA 전체 결과를 PASS로 처리하지 않는다. Release Hold를 유지한다.

## 2. 수용 기준별 결과

| 수용 기준 | 결과 | QA 근거/제한 |
|---|---|---|
| 현재 Release의 UI→기능→저장/API 연결 인벤토리 | **PASS (정적 범위)** | Developer 보고서의 기능별 파일·클래스 근거를 재확인. 실기기 사용자 흐름은 별도 NOT RUN |
| Release 설정·권한·SDK·서명 설정 인벤토리 | **PASS (정적/로컬 검증)** | `app/build.gradle.kts`, merged manifest, runtime dependency 결과, signing-config task 확인. Play Console 업로드 설정은 BLOCKED |
| 앱 데이터와 SDK/Android 처리 구분 | **PARTIAL / BLOCKED** | local ledger·UsageStats·센서·SharedPreferences 흐름은 소스에서 구분됨. AICore/ML Kit 및 OS의 외부 처리·보관·학습 조건과 Play Data safety 신고는 확인 불가 |
| P0/P1/P2 finding에 재현/영향/수정 제안/검증 기준 포함 | **PASS (정적 finding 기준)** | `AUD-01-F01`–`F09`를 확인. 실제 재현이 필요한 항목은 미실행 상태로 표시되어 있음. 코드 결함으로 확정할 수 없는 산식/AI/정책 항목은 PM 결정으로 표시 |
| 검증 명령과 원시 결과·기존 artifact 식별정보 기록 | **PASS (로컬 증거)** | 48 unit tests 결과, Release lint/build task 결과, APK/AAB SHA-256·시각·서명 상태가 기록됨. 현재 worktree와 기존 artifact 간 build lineage는 입증되지 않음 |
| 베타 후보와 제외 후보가 승인과 구분됨 | **PASS** | MVP는 권고로만 기록되어 있고 Release Hold/Play 배포 승인은 유지됨 |
| 감사 중 기존 변경 보존 및 코드 미수정 | **PASS** | HEAD 동일, staged 0개, worktree status 항목 58개(기준점의 수정 24+미추적 34) 동일. 감사 보고서는 저장소 밖 task 폴더에 있음 |
| 완전한 제품/기기 QA | **BLOCKED** | Android 기기·OS·사용자 권한 상태·Play Console을 이용한 검증을 하지 않음 |

## 3. 재검토한 finding

| Finding | QA 판정 | 비고 |
|---|---|---|
| AUD-01-F01 — Release 알림 권한 요청 부재 | **확인 — P1** | Release source에서 런타임 요청 진입점은 찾지 못했고 scheduler는 `POST_NOTIFICATIONS` 미허가 시 반환한다. Android 13+ 실기기 동작은 미검증 |
| AUD-01-F02 — 위젯에서 미측정/저장 실패를 정상 점수처럼 표시 | **확인 — P1** | `screenTimeMeasured`를 위젯이 전달하지 않고 실패를 0분/빈 기록으로 대체한다. 표시되는 실제 화면은 미검증 |
| AUD-01-F03 — 전체 삭제가 커뮤니티 profile prefs를 지우지 않음 | **확인 — P1 후보** | 삭제 구현과 저장 위치가 분리된 것은 맞다. 사용자 기대/제품 정의는 PM이 결정하므로 우선순위 최종 승인도 PM 소관 |
| AUD-01-F04 — 커뮤니티/챌린지 실제 참여 데이터처럼 보이는 로컬 데모 | **확인 — P1 후보** | 홈에서 `CommunityCheerWidget`이 호출되고 샘플 친구·릴레이·방명록이 기본값으로 구성됨. 글로벌 챌린지는 고정 참가자 수를 사용함. 실제 서비스 표현 범위는 PM 결정 |
| AUD-01-F05 — 배터리 최적화 제외 요청 Play 정책 위험 | **확인 — P1 정책 게이트** | Lint의 `BatteryLife` 경고와 설정 Intent가 일치한다. lint 메시지만으로 Play 정책 위반을 법적으로 확정하지 않으며, 허용 사유/핵심 기능 여부를 검토해야 함 |
| AUD-01-F06 — 점수 산식 및 “눈 건강” 표현 | **PM 결정 필요** | 코드는 비의료적 웰니스 가중 합산식이다. 계산 단위 테스트는 점수의 효용·표현·건강 관련 근거를 증명하지 않음 |
| AUD-01-F07 — 비활성 AI UI와 Release GenAI SDK | **확인 — P2, 활성화 전 게이트** | UI 비활성 상태와 SDK 포함은 확인됨. 현재 사용자 질문이 해당 SDK에 전달되는 실행 경로는 확인되지 않아 즉시 전송 결함으로 판정하지 않음 |
| AUD-01-F08 — `app:tasks --all` 구성 실패 | **확인 — P2 개발환경 이슈** | 지정 명령이 Compose Preview 생성 단계에서 실패. 개별 unit test/lint 실행은 가능 |
| AUD-01-F09 — Release lint 경고/하드코딩 문자열 | **확인 — P2 품질 항목** | Release lint task는 성공했으나 SARIF에는 warning/note가 있음. 실제 4개 언어 화면 검수는 별도 필요 |

P0 finding은 확인되지 않았다. 이는 고위험 또는 출시에 필요한 정책·제품 결정이 없다는 뜻이 아니며, 현재 증거에서 P0으로 분류할 재현 가능한 치명 결함이 확인되지 않았다는 뜻이다.

## 4. 검증 증거 재확인

- `.\gradlew.bat app:testDebugUnitTest --no-configuration-cache`: **PASS**, 48 tests / 0 failures / 0 errors / 0 skipped.
- `.\gradlew.bat app:lintRelease app:verifyReleaseSigningConfig --no-configuration-cache`: **PASS with warnings**. Release Kotlin 컴파일, `verifyReleaseBoundary`, signing config, lint 완료. SARIF: 195 results (192 warning, 3 note), `BatteryLife` 정책 주의 포함.
- `.\gradlew.bat app:tasks --all --no-configuration-cache`: **FAIL** — Compose Preview task 생성 중 unit-test Android resources 제외 오류.
- 기존 APK: apksigner 서명 확인 **PASS**. 기존 AAB: `jarsigner -verify` 서명 확인은 되며 self-signed/trust-chain/timestamp 경고. `-strict` exit 4는 인증서 신뢰 오류로 해석해야 하며 손상 여부나 Play App Signing 적합성을 증명하지 않는다.
- 기존 APK 및 AAB 각각의 64-bit native ELF 2개를 읽기 전용 검사해 16 KB PT_LOAD 정렬 조건 **PASS**. 새 package 생성은 없었다.
- 기존 APK/AAB는 2026-11-14 시각의 산출물이다. 현재 worktree HEAD/변경과 일치하는지 입증되지 않았고, 이 감사에서 재빌드/업로드하지 않았다.

## 5. QA 인계

다음 단계는 코드 수정을 바로 시작하는 것이 아니라, PM이 `AUD-01-F01`–`F06`의 베타 범위·설계·우선순위를 승인하고 해당 결정을 `IRIS-REQ-*`로 정식 발급하는 것이다. F07은 AI 활성화 여부와 처리 증거를 결정하기 전까지 비활성으로 유지한다. 그 후 승인된 개발 작업을 작은 독립 단위로 분리하고, 실기기 QA 및 사람의 검토가 가능한 항목은 별도 검증한다.

이 QA 결과는 Release Hold 해제, 베타 트랙 업로드, 개인정보/Data safety 승인 또는 Production 배포 승인이 아니다.
