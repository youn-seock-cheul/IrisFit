# IrisFit 실제 시청시간 추정 — PM 17차 검토

- **검토 기준 commit:** `f7a8d2d` (`fix(ui-copy): remove '맞춤' wording from unmeasured relay toast and use neutral icon for UNMEASURED mascot in WellnessHome`)
- **기준점:** HEAD `f7a8d2d`; 검토 시작 시 worktree clean
- **PM 판정:** **PARTIAL PASS — 코드 수정 수용, 자동 UI 검증 및 실기기 검증 미완료, Release Hold 유지**
- **테스트:** `.\gradlew.bat :app:testDebugUnitTest` 성공. XML 결과 independently 합산 **63 tests, 0 failures, 0 errors, 0 skipped** (13 suites).

## 독립 검토 결과

| 항목 | 판정 | 확인 범위 |
|---|---|---|
| 미측정 AI relay Toast | **PASS — 소스 확인** | `MainScreen.kt` 미측정 경로에서 “맞춤” 표현을 제거하고 “눈 건강 휴식 릴레이 주제를 추천” 문구로 교체했다. 개인화 상태/점수 기반 추천을 암시하던 직전 잔여 문구가 정리됐다. |
| Wellness 미측정 아이콘 | **PASS — 소스 확인** | `WellnessHome.kt`에서 `EyeFatigueStatus.UNMEASURED`를 `ic_nav_eye_comfort`에 연결하고 happy mascot 분기와 분리했다. score 라벨, 게이지/범례는 이전 중립 처리대로 유지된다. |
| 자동 테스트 근거 | **PARTIAL** | 전체 JVM 단위 테스트는 63/0/0. 이번 커밋의 diff는 UI 문구/리소스 선택 2줄만 변경하고 전용 Compose/UI 테스트를 추가하지 않았다. 따라서 QA가 말한 “토스트가 출력됨/아이콘이 올바르게 출력됨”은 소스 조건 확인으로만 인정하며 실제 UI 렌더링 PASS를 독립 입증하지 않는다. |
| 센서 freshness 경계 | **NOT VERIFIED** | 이번 커밋에서 detector가 수정되지 않았다. 이전 코드의 proximity guard는 존재하나 실제 callback/freshness 경계 테스트는 계속 미확인이다. |
| 실기기 정확도/전력 | **NOT RUN** | 이번 검토에서 UI 기기 재현, 실제 viewing/posture accuracy, battery/heat 측정을 수행하지 않았다. 승인된 정량 acceptance threshold 및 protocol도 아직 없다. |

QA가 제시한 두 개선사항은 소스에서 확인되어 수정 자체는 수용한다. 그러나 63개 JVM 테스트는 이번 UI 변경을 검증하는 전용 테스트가 아니므로 UI 렌더링 검증 완료로 해석하지 않는다. 보고된 USB 충전 상태 또한 이번 PM 검토에서 다시 측정한 사실로 취급하지 않는다.

## 잔여 개발·QA 지시

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 미측정 상태의 중립 표현이 앱 화면에서 실제로 유지되고, Android 코드의 sensor freshness가 검증된 이벤트에만 의존하도록 근거를 확보한다.
* **상세 요구사항 (What):**
  - 기능 1: AI relay 및 Wellness 미측정 UI에 대해 가능한 테스트 체계에 맞춰 Compose/UI 테스트를 추가한다. 미측정 제목/토스트 및 중립 아이콘을 검증하고, 측정 상태의 기존 기능도 회귀 확인한다.
  - 기능 2: SmartDevicePoseDetector의 유효/무효 payload, proximity maximumRange 경계 및 freshness 타임스탬프 갱신/무효화 경계 테스트를 추가한다.
  - 기능 3: 미측정 flag와 score 불일치, 음수 점수, permission 거부/query 실패 분기를 포함해 기존 Goal/AiCoach/relay UI 소비처를 검증한다.
* **예외 처리 및 엣지 케이스:** UI는 `screenTimeMeasured=false`와 nonnegative score가 동시인 상태 및 score < 0 상태를 각각 다룬다. 센서는 malformed/non-finite/out-of-range 값, 등록 전/해제 후 callback, invalid timestamp를 freshness로 인정하지 않아야 한다.
* **Google Play 정책 체크포인트:** 새 권한이나 백그라운드 센서 수집을 추가하지 않는다. UsageStats/자세를 실제 시선 또는 실제 시청시간으로 주장하지 않는다.
* **완료 조건 (Acceptance Criteria):** UI 테스트에서 측정 불가 제목/토스트/아이콘이 상태 기반 점수나 긍정 컨디션을 주장하지 않음을 검증한다. sensor boundary 테스트에서 유효한 이벤트에서만 freshness가 갱신됨을 검증한다. 관련 테스트와 전체 JVM 테스트가 통과한다. 실기기 viewing accuracy/배터리 시험은 별도 승인된 기준과 절차가 마련되기 전까지 NOT RUN으로 관리한다.

## PM 최종 판정

`f7a8d2d`의 relay Toast 문구와 Wellness 미측정 아이콘 변경은 **코드 수준에서 수용**한다. QA의 화면별 PASS 표현은 전용 UI 테스트나 실기기 관찰을 이번 검토에서 재현하지 않았으므로 코드 확인으로 한정한다. 전체 63개 JVM 테스트는 통과했으나 이번 변경의 UI 분기 및 detector callback을 직접 검증하지 않는다. **PARTIAL PASS; Release Hold 유지; 내부 테스트/베타/Play 배포 미승인.**
