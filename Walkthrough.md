# IrisFit Walkthrough

이 파일은 저장소에서 제품 로드맵과 구현 이력을 함께 확인하는 진입점입니다. **현재 기준 문서는 [`task/IrisFit_PM_User_Walkthrough.md`](task/IrisFit_PM_User_Walkthrough.md)**이며, 이 문서에 제품 상태, 전체 로드맵, 주요 개발 이력, QA 근거와 결정이 통합되어 있습니다.

## 현재 상태 — 2026-11-16

목표는 실제 시청시간(손에 들고 실제 화면을 보는 시간)입니다. UsageStats의 앱 사용시간이나 자세 분류를 실제 시선 측정으로 간주하지 않습니다.

| 구분 | 현재까지 / 남은 일 |
|---|---|
| **완료** | 현재 구현 정적 감사와 QA 검토; 실제 시청시간 목표·Android 13+·측정 불가 시 `미측정` 원칙 설정; `d301df4` Debug 설치/실행; JVM 테스트 **62 passed / 0 failed / 0 skipped**; 제한적인 Galaxy S23+ 홈 화면과 베타 안내 확인 |
| **진행 중** | 개발·QA 단계. PM 판정 **PARTIAL PASS**; 작업 재개방 |
| **우선 보완** | AI 코치·목표·추세 등 전 화면에서 미측정 점수/성공 판정 차단; 센서 종류별 payload와 proximity 범위를 freshness 갱신 전에 검증; `SM-S916N`을 S23+ 프로필로 인식; 해당 경계 자동 테스트 추가 |
| **실기기에서 미검증** | 센서 callback/freshness 정확도, 자세 오탐·미탐, 실제 시청시간 정확도, 배터리/발열 비교. 기기가 USB 충전 중이어서 배터리 시험은 **NOT RUN** |
| **출시** | 내부 테스트·베타·Play 배포 모두 미승인. **Release Hold 유지** |

**다음 순서:** 개발 보완 및 테스트 → QA 재검수 → PM 완료 판정 → 사전 승인된 기준으로 충전 해제 후 실기기 정확도/배터리 시험 → 별도 내부 테스트/출시 승인. 현재 실기기 UI에서 보인 `100%`, `2분`, `책상 위 놓임`은 화면 표시 확인일 뿐 측정 정확도를 입증하지 않습니다.

## 통합 Walkthrough

- [PM 로드맵 및 개발 이력 통합본](task/IrisFit_PM_User_Walkthrough.md) — 유일한 현재 상태/제품 로드맵 기준
- [PM 결정 로그 및 문서 인덱스](task/IrisFit_PM_Documentation_Index_and_Decision_Log.md) — 승인, 작업 상태, 증거 링크
- [이전 QA 결과/PM 판정 — 9dd7059](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md)
- [이전 QA 결과/PM 판정 — 6d2b718](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md)
- [최신 QA 결과/PM 판정 — d301df4](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_d301df4_2026-11-16.md)
- [Galaxy S23 실기기 스모크 체크](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Device_Smoke_Check_2026-11-15.md)
- 상세 로드맵과 결정 이력은 아래 통합 문서를 확인하세요.

## 이력 및 재검증 원칙

이 Walkthrough는 기존 파일의 2026-11-12 Widget 1 정리, 2026-11-13 블루라이트 오버레이 제거 및 Gemini Nano 코치 기록을 통합 문서의 개발 이력으로 보존합니다. 기존 검증은 해당 시점의 기록이며 현재 Release 상태나 실제 시청시간의 정확도를 입증하지 않습니다. 자세한 이력과 한계는 통합 문서에 기록합니다.

중복된 과거 Walkthrough 스냅샷 3개(`docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`)는 통합 후 삭제했습니다. 과거 검증 결과는 현재 승인으로 해석하지 않습니다.
