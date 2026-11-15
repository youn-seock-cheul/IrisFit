# IrisFit Walkthrough

이 파일은 저장소에서 제품 로드맵과 구현 이력을 함께 확인하는 진입점입니다. **현재 기준 문서는 [`task/IrisFit_PM_User_Walkthrough.md`](task/IrisFit_PM_User_Walkthrough.md)**이며, 이 문서에 제품 상태, 전체 로드맵, 주요 개발 이력, QA 근거와 결정이 통합되어 있습니다.

## 현재 상태 — 2026-11-15

- 실제 시청시간 목표는 유지하되, UsageStats의 앱 사용시간을 실제 시청시간으로 간주하지 않습니다.
- 최신 검토 기준 commit: `6d2b718`; `.\gradlew.bat :app:testDebugUnitTest` 재실행 결과 61 passed, 0 skipped, 0 failed.
- PM 판정: **PARTIAL PASS**. 일반 위젯/공유/finite payload 처리는 개선됐으나 활성 bitmap 위젯, AI 코치/목표/추세의 미측정 점수 처리가 남고 센서별 구조·범위 검증 전 freshness가 갱신될 수 있습니다.
- Galaxy S23 (SM-S916N, Android 16/API 36)에서 짧은 앱 실행·센서 가용성 스모크를 제한 범위로 통과했습니다. 기기는 USB 충전 중이어서 배터리 시험은 NOT RUN이며, 센서 이벤트 freshness·실제 시청 정확도도 NOT RUN입니다.
- 센서 이벤트 freshness/payload 정확도, 실제 시청 정확도 및 배터리 비교는 **NOT RUN**. Release Hold 유지; 내부 테스트와 Play 배포 미승인.

## 통합 Walkthrough

- [PM 로드맵 및 개발 이력 통합본](task/IrisFit_PM_User_Walkthrough.md) — 유일한 현재 상태/제품 로드맵 기준
- [PM 결정 로그 및 문서 인덱스](task/IrisFit_PM_Documentation_Index_and_Decision_Log.md) — 승인, 작업 상태, 증거 링크
- [이전 QA 결과/PM 판정 — 9dd7059](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_9dd7059_2026-11-15.md)
- [최신 QA 결과/PM 판정 — 6d2b718](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Actual_Viewing_Time_QA_Review_6d2b718_2026-11-15.md)
- [Galaxy S23 실기기 스모크 체크](C:/Users/youns/AndroidStudioProjects/task/IrisFit_PM_Device_Smoke_Check_2026-11-15.md)

## 이력 및 재검증 원칙

이 Walkthrough는 기존 파일의 2026-11-12 Widget 1 정리, 2026-11-13 블루라이트 오버레이 제거 및 Gemini Nano 코치 기록을 통합 문서의 개발 이력으로 보존합니다. 기존 검증은 해당 시점의 기록이며 현재 Release 상태나 실제 시청시간의 정확도를 입증하지 않습니다. 자세한 이력과 한계는 통합 문서에 기록합니다.

중복된 과거 Walkthrough 스냅샷 3개(`docs/WALKTHROUGH_P0_LATEST.md`, `docs/WALKTHROUGH_ROADMAP.md`, `docs/WALKTHROUGH_GOOGLE_PLAY_IRISFIT.md`)는 통합 후 삭제했습니다. 과거 검증 결과는 현재 승인으로 해석하지 않습니다.
