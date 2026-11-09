# 30초 안내 세션·완료 기록 구현

2026-11-01. 초안의 30초 기본안을 실제 production 소스에 구현했다. 60초 옵션·종료 소리/진동·카메라/오버레이 운동은 후속 범위다. 최종 판매/효능·사용자 가치 검증 완료가 아니다.

## 구현 결과

홈 ‘30초 휴식 시작’ → 준비 → ‘안내 시작’ → 준비 5초/화면에서 쉬기 20초/마무리 5초 → 사용자 확인 → 완료 기록.

- 일시정지·계속하기·그만하기, 완료/중단 후 홈·다시 하기.
- 회전/Activity 재생성은 같은 세션 유지. 앱 이탈은 일시정지, 복귀 후 수동 재개. 프로세스 종료 후 미완료 세션 자동 재개/완료 없음.
- 시간 종료만으로 완료를 기록하지 않음. 사용자 확인 후 저장에 성공해야 완료 표시.
- 오늘 완료 횟수·최근 3회 요약, 최근 100회 보관. 당시 현지 날짜를 유지하며 오늘 날짜는 화면 갱신/앱 복귀 시 재평가.
- 기본 무음. 실제 눈 동작/시선/건강 개선 측정 없음.
- 기존 20분 알림과 독립적. 안내 시작/완료로 예약을 추가/초기화하지 않음.

## 저장·삭제

앱 전용 guided_sessions_v1.txt를 AtomicFile로 교체하고 IO dispatcher에서 읽고 쓴다. 작은 목록을 한 번에 저장하고 세션 ID로 중복을 제거한다. 여러 Activity가 열려도 앱 프로세스 안에서 같은 직렬화된 저장소를 공유하고, 화면 복귀 시 기록을 다시 읽어 각 화면의 오래된 캐시가 다른 완료 기록을 덮어쓰지 않게 했다. SharedPreferences의 메모리 변경과 디스크 저장 실패를 혼동하지 않도록 별도 파일로 분리했다.

필드: 세션 ID, 루틴 종류/버전, 계획 길이 30초, 완료 시각/당시 현지 날짜, user_confirmed. 미완료/중단 세션·눈 좌표는 보관하지 않는다. 파싱 불가 기록은 조용히 덮어쓰지 않고 오류·다시 읽기/전체 삭제를 제공한다.

저장 실패 시 완료 표시 없이 재시도/기록 없이 종료 가능. 전체 삭제는 새 기록과 진행 세션도 정리한다. 삭제 실패는 확인창에도 표시한다. 개인정보 안내·공개 방침/상품 설명 초안 갱신. 기존 백업·기기 이전 규칙의 file 전체 제외가 새 파일에도 적용된다.

코드: [시간·상태](../app/src/production/kotlin/com/example/myapplication/session/GuidedSession.kt), [기록 규칙](../app/src/production/kotlin/com/example/myapplication/session/CompletionRepository.kt), [파일 IO](../app/src/production/kotlin/com/example/myapplication/session/FileCompletionStorage.kt), [ViewModel](../app/src/production/kotlin/com/example/myapplication/session/GuidedSessionViewModel.kt), [안내 카드](../app/src/production/kotlin/com/example/myapplication/ui/GuidedSessionCard.kt). 실제 production MainActivity/WellnessHome에 연결했으며 보존된 미컴파일 UI는 수정하지 않았다.

## 검증

| 종류 | 결과 |
| --- | --- |
| 단위 | **18개 통과**(신규 10+기존 8). 가짜 시계로 단계/중단/중복 시작, 저장 중복/실패/100회/손상/삭제 실패 검증 |
| debug UI | **4개 통과**, failures/errors/skipped=0. 신규 2개 37.708초+기존 2개 2.000초=**39.708초**. 15:38:20 KST XML |
| 전체 R8 release 외부 UI | **1개 통과**, failures/errors/skipped=0, **53.607초**, 15:42:15 KST XML. 실제 30초 완료→프로세스 재시작→기록 유지→전체 삭제 |
| 산출물 | 기본 설정 release AAB·release 경계·AAB 네이티브 정렬 검사 성공 |
| Lint | **오류 0·경고 46**. 모든 경고를 해소한 것은 아님 |

debug UI는 실제 30초 경과 후 확인 전 기록 0→확인 후 파일 1, 회전 후 완료 화면·전체 삭제를 검증했다. 다른 신규 테스트는 회전 중 같은 세션/실행 유지, Activity 비활성화·복귀 시 일시정지·수동 재개, 중단 후 기록 없음을 검증했다. 실제 잠금/모든 기기 통과를 의미하지 않는다.

검증 중 수정한 이슈:
1. 기존 error_prone_annotations 2.51.0과 debugRuntimeClasspath의 일관 버전 2.28.0 충돌. 테스트 의존성을 2.28.0으로 맞춘 후 빌드 성공.
2. 새 테스트가 스크롤 부모 없는 삭제 확인창에 performScrollTo를 적용해 시작 단계에서 실패. 확인창은 직접 눌러 재실행 4개 통과. 첫 실패를 기능 통과로 세지 않음.

마지막 보완 후 같은 검사들을 다시 실행해 위 결과를 확인했다. 테스트 횟수를 중복 합산하지 않는다. 설치 보조 스크립트에는 APK 생성에 대한 명시적 선행 의존성을 추가했다.

보존 XML: .scratch/p0_device_20261101/guided-session-debug-ui.xml, guided-session-full-r8-ui.xml, guided-session-unit.xml, completion-ledger-unit.xml.

Galaxy S23+ / Android 16. debug XML: app/build/outputs/androidTest-results/connected/debug/TEST-SM-S916N - 16.xml. 단위 XML: app/build/test-results/testDebugUnitTest. 과거 채널/자연 알림 시험과 합산하지 않는다.

## 최종 설치·정리

정상 release APK 사본을 기존 개발용 인증서로 서명해 Galaxy에 설치·단독 시작했다. COLD 시작 421ms, 현재 PID 크래시 기록 없음. 설치 APK와 사본 해시 일치: faacc638e18a1128638758c040f1238f24e7c46f84404dc38dc5c00504d5f3f6. 개발용 서명은 스토어 업로드 서명이 아니다.

.scratch/p0_device_20261101/guided-session-final-artifact.txt에 증거 보존. 활성 시험 예약/알림 없음. USB 화면 유지=0·자동 회전=0·사용자 회전=0 복원 확인. 최종 새 설치의 알림 권한은 미허용 상태이며 30초 안내는 권한 없이 사용할 수 있다. 테스트용 완료 기록은 삭제했다.

## 최신 검증 시간 원칙

고정 5분 제한이 아니다. 관련 단위 검사·짧은 UI 확인은 진행하되 오래 걸리는 검증은 예상 시간·목적·생략 시 한계를 먼저 알려 사용자가 판단하게 한다. 20분 자연 알림·절전·배터리 장시간 시험은 실행하지 않았다.

## 다음 우선순위·남은 사항

무음 종료를 알아채기 어려운지, 안내 문구가 도움이 되는지 짧은 실제 체험으로 확인하는 것이 다음이다. 60초/소리·진동/오버레이·카메라는 후속 범위다. 이번 구현은 정확한 시선 추적·운동 마커 완성이 아니다. 다른 OS/기기·실제 잠금 등 추가 검증과 출시 서명/공개 방침 운영자/Console 조건은 남아 있다. 지금 판매·가격 결정을 요구하지 않는다.
