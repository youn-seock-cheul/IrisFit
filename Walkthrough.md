# IrisFit Walkthrough

이 파일은 저장소에서 제품 로드맵과 구현 이력을 함께 확인하는 진입점입니다. **현재 기준 문서는 [`task/IrisFit_PM_User_Walkthrough.md`](task/IrisFit_PM_User_Walkthrough.md)**이며, 이 문서에 제품 상태, 전체 로드맵, 주요 개발 이력, QA 근거와 결정이 통합되어 있습니다.

## 현재 상태 — 2026-11-15

- 실제 시청시간 목표는 유지하되, UsageStats의 앱 사용시간을 실제 시청시간으로 간주하지 않습니다.
- 최신 검토 기준 commit: `8a72672`; `.\gradlew.bat :app:testDebugUnitTest` 재실행 결과 61 passed, 0 skipped, 0 failed.
- PM 판정: **PARTIAL PASS**. 활성 query 경로는 typed 처리됐으나 미측정 시 점수/status가 노출될 수 있고, payload 검증 전 센서 freshness가 갱신될 수 있어 수정/QA 재검수를 대기합니다.
- 시청 정확도, 실기기 센서 및 배터리 시험은 **NOT RUN**. Release Hold 유지; 내부 테스트와 Play 배포 미승인.

## 통합 Walkthrough

- [PM 로드맵 및 개발 이력 통합본](task/IrisFit_PM_User_Walkthrough.md) — 유일한 현재 상태/제품 로드맵 기준
- [PM 결정 로그 및 문서 인덱스](task/IrisFit_PM_Documentation_Index_and_Decision_Log.md) — 승인, 작업 상태, 증거 링크
- [최신 QA 결과/PM 판정](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_8a72672_2026-11-15.md)

## 이력 및 재검증 원칙

이 Walkthrough는 기존 파일의 2026-11-12 Widget 1 정리, 2026-11-13 블루라이트 오버레이 제거 및 Gemini Nano 코치 기록을 통합 문서의 개발 이력으로 보존합니다. 기존 검증은 해당 시점의 기록이며 현재 Release 상태나 실제 시청시간의 정확도를 입증하지 않습니다. 자세한 이력과 한계는 통합 문서에 기록합니다.

중복된 과거 Walkthrough 스냅샷 3개(`docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`)는 통합 후 삭제했습니다. 과거 검증 결과는 현재 승인으로 해석하지 않습니다.
