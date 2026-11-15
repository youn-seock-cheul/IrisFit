# [PM 2차 재검수 보고서] 실제 시청시간 평가기 상태 세분화 및 측정 왜곡 방지

- 검수 일자: 2026-11-15
- 작성 역할: 수석 PM (`[PM]`)
- 검수 대상: `ViewingTimeEvaluator.kt`, `ViewingTimeEvaluatorTest.kt`
- 출시 상태: **Release Hold 유지**

---

## 1. 2차 재검수 배경 및 지시 사항

이전 1차 개발 시 `ViewingTimeStatus` 클래스의 이름만 세분화하고, 실제 evaluator의 입출력 분기 검증 및 추정 수치의 명확한 성격 구분이 다소 부족했습니다. 이에 따라 다음 4가지 핵심 보완 사항을 반영했습니다.

1. **상태 분기 실질적 1:1 검증:** 단위 테스트가 단순 객체 생성을 넘어 `ViewingTimeEvaluator.evaluateViewingTime(...)` 메서드의 실제 입출력(권한, 전경 여부, 화면 꺼짐, 센서 미등록/stale, mock 사용시간, 분할화면/구성변경)을 실측 검증하도록 개선.
2. **측정 예외 상태 세분화:** 권한 미허용(`PermissionDenied`), 백그라운드/화면꺼짐(`BackgroundPaused`), 센서 미지원/데이터 중단(`SensorUnavailable`), UsageStats 오류(`QueryError`), 정상 측정된 0분(`MeasuredZero`), 화면 회전/Fold 전환(`ConfigurationChanging`)을 완전히 분리.
3. **추정 수치의 모델 명시:** 자세 할인율(95%, 85%, 50%)과 배터리 상수를 실측값이 아닌 `FeasibilityEstimate` (이론적 타당성 추정 모델) 수치로 명시하고, 모델 내에 면책 문구(`disclaimer`) 포함.
4. **유저 UI 왜곡 방지:** `WellnessHome.kt` 등 유저 UI에는 `ScreenTimeManager.getTodayScreenTimeFormatted(context)` (UsageStats 공식 측정값)만 표시되며 추정 수치가 실측 시청시간으로 표기되지 않음을 캡슐화 검증.

---

## 2. PM 완료 기준별 검증 분류 표기

| 완료 기준 및 검증 항목 | 담당 | 판정 구분 | 증거 및 세부 내용 |
|---|---|---|---|
| **Evaluator 입출력 분기 단위 테스트** | 개발자/QA | **`PASS`** | `ViewingTimeEvaluatorTest.kt` 7개 테스트 케이스 100% 통과 |
| **실기기 센서 하드웨어 전력 소모 실측** | 개발자/QA | **`NOT RUN`** | 현재 개발 환경에 연결된 물리 센서 전류계 미존재 |
| **실기기 카메라 시선 추적 광학 정확도 실측** | 개발자/QA | **`NOT RUN`** | 물리 카메라 안구 추적 벤치마크 환경 미존재 |
| **Google Play / Android 13+ 권한 및 백그라운드 정책** | PM / 개발자 | **`PASS`** | 백그라운드 카메라 서비스 배제, 100% 온디바이스 센서 로컬 연산 준수 |

---

## 3. 결론

* **재검수 판정:** **`PASS` (개선 개발 및 단위 테스트 통과)**
* **출시 상태:** **`Release Hold` 유지** (실기기 하드웨어 검증 전까지 배포 보류 유지)
