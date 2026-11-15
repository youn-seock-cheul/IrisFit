# IrisFit 실제 시청시간 추정 — PM 8차 QA 검토

- **검토일:** 2026-11-16
- **검토 기준 commit:** `311ab4c` (`fix(device-profile): detect SM-S916N Galaxy S23+ and validate sensor proximity bounds & unknown types in SmartDevicePoseDetector`)
- **QA 제출 판정:** PARTIAL PASS
- **PM 판정:** **PARTIAL PASS — 잔여 결함 재개방, Release Hold 유지**
- **JVM 테스트:** `.\gradlew.bat :app:testDebugUnitTest` 실행 후 XML 확인: **63 tests, 0 failures/errors, 0 skipped**
- **기기:** Samsung Galaxy S23+ `SM-S916N`, Android 16/API 36; package `com.eyeoverlay.app`, version 1.0.0, targetSdk 36

## 1. 검증 증거와 한계

1. 저장소 HEAD는 `311ab4c`이며, 해당 Debug APK를 기기에 재설치했다. 앱 실행은 성공했고 홈 화면에서 `책상 위 놓임`, `99%`, `오늘 누적 화면 사용: 15분`을 관찰했다. 이는 빌드/실행 및 화면 표시 스모크만 입증하며 실제 시청시간, 자세 분류, UsageStats 정확도를 입증하지 않는다.
2. `:app:testDebugUnitTest` Gradle XML 결과를 독립 합산해 **63 tests, 0 failures/errors, 0 skipped**를 확인했다. 추가된 `testTargetDeviceDetection_GalaxyS23Plus()`는 `SM-S916N`을 `GALAXY_S23`으로 분류한다. 테스트는 JVM 단위 테스트이며 센서 하드웨어 callback 검증은 아니다.
3. 현재 센서 처리 코드는 NaN/Infinity, 센서별 최소 배열 길이, 음수 light/proximity 값, proximity `maximumRange`의 finite/positive 여부, timestamp 및 허용 센서 타입을 검사한 뒤 freshness를 갱신한다. 알 수 없는 타입은 거부한다. 다만 `values[0] <= maximumRange` 조건은 없고, 실제 payload/freshness 경계 테스트도 없다.
4. 배터리 상태는 충전 중(`status: 2`)이라 소비량 비교는 **NOT RUN**이다. 정답 라벨 기반 자세·실제 시청 정확도, 오탐/미탐, 센서 freshness의 실기기 정확도도 **NOT RUN**이다. 정확도/전력 임계값과 실험 절차는 미승인 상태다.

## 2. PM 기준별 판정

| # | 기준 | 검증 결과 |
|---|---|---|
| 1 | 실제 기기 모델 매핑 | **PASS (코드/단위 테스트 범위)** — `SM-S916N`이 S23 계열로 분류됨. `GALAXY_S23` 프로필 이름은 S23+를 구분하지 않으므로 추후 세부 프로필 요구가 생기면 별도 결정 필요 |
| 2 | sensor payload 검증 순서 | **PARTIAL PASS** — 크기·유한성·타입·timestamp 검증 후 freshness 갱신. proximity sample 값이 센서의 `maximumRange`를 넘는 입력은 현재 허용될 수 있음 |
| 3 | 미측정값의 전 화면 처리 | **FAIL** — `MainScreen.kt`의 Goal/AI Coach는 `batteryScore`를 백분율로 렌더링하고 goal 비교에 사용한다. `WellnessHome.kt`는 음수 값을 0으로 clamp해 위험 구간처럼 보일 수 있고, `BatteryTrendCard.kt`는 미측정값이 그래프에 포함될 수 있다 |
| 4 | 단위 테스트 커버리지 | **PARTIAL PASS** — 전체 63개 통과 및 모델 매핑 테스트 확인. `SmartDevicePoseDetector` callback/freshness 및 화면별 미측정 렌더링 경계 테스트는 찾지 못함 |
| 5 | 현재 Debug 앱 실행 | **PASS (실행 스모크 한정)** — 최신 Debug 빌드 설치와 앱 실행 성공. UI 수치는 정확도 근거가 아님 |
| 6 | 실제 정확도/전력 검증 | **NOT RUN** — 라벨 기반 실제 시청/자세 평가 및 충전 해제 조건의 배터리 비교가 없음 |

## 3. 남은 개발 지시사항

### 📋 [개발 지시서] (To. 시니어 Android 개발자)

* **작업 목표 (Why):** 측정할 수 없는 상태를 정상 점수나 목표 달성으로 오인시키지 않고, 잘못된 센서 입력이 유효 freshness 신호로 취급되지 않게 한다. 추정치를 실제 시청시간으로 과장하지 않는다.
* **상세 요구사항 (What):**
  - 기능 1: AI Coach, GoalAchievementCard, Wellness gauge/legend, BatteryTrendCard 및 기타 실제 사용되는 score consumer에서 `UNMEASURED`를 독립 상태로 표시한다. 미측정이면 점수 백분율/위험 등급/목표 달성 판단/추세 plot에 포함하지 않는다.
  - 기능 2: proximity 값이 센서의 유효 범위를 벗어날 때 freshness를 갱신하지 않도록 검증한다. 기존 실제 기기의 센서 반환 의미와 classifier 요구사항에 맞춰 경계를 정하고, 잘못된 범위를 유효한 상태로 대체하지 않는다.
  - 기능 3: detector 입력 검증 및 timestamp/freshness 갱신 순서를 단위 테스트로 검증한다. 정상 이벤트는 freshness를 갱신하고, 짧은/null/NaN/Infinity/음수/범위 초과/잘못된 timestamp/미지원 type 입력은 갱신하지 않음을 확인한다.
  - 기능 4: 모든 활성 점수 소비처에서 measured 및 unmeasured 상태 렌더링을 테스트한다. `SM-S916N` 모델 테스트는 유지한다.
* **예외 처리 및 엣지 케이스:** 센서 미존재/등록 실패, listener 중지 후 callback, 여러 센서 동시 입력, proximity 값의 far-field 반환, UsageStats 권한 거부 및 query error를 유효 점수로 변환하지 않는다. 경계값은 Android 센서 의미에 맞춰 문서화하고 테스트한다.
* **Google Play 정책 체크포인트:** 새로운 권한/백그라운드 센서 수집을 추가하지 않는다. 앱 사용시간이나 posture 신호를 실제 시선/실제 시청시간으로 표현하지 않으며, 건강 진단/의료 측정처럼 오인될 수 있는 문구를 피한다.
* **완료 조건 (Acceptance Criteria):** 모든 활성 소비처에서 미측정이 점수·목표 성공·위험 등급·추세 데이터로 처리되지 않는다. 센서 입력 경계 테스트가 freshness 동작을 입증하고 정상 proximity 이벤트는 기존 의도된 분류를 보존한다. 모델/회귀 단위 테스트 통과 결과를 보고한다. 실기기 정확도 및 배터리 시험은 승인된 절차가 마련되기 전까지 별도 **NOT RUN**으로 남긴다.

## 4. PM 판정

`311ab4c`는 S23+ 모델 식별, 일부 센서 입력 검증 및 freshness 순서를 개선했고 63개 JVM 테스트가 통과했다. 하지만 미측정값의 활성 UI 소비처 처리와 직접적인 sensor/UI 경계 검증이 미완료라 기능 완료로 판정하지 않는다. 정답 라벨 기반 실제 시청 정확도와 배터리 영향은 여전히 입증되지 않았다. **Release Hold 유지; 내부 테스트/베타/Play 배포 승인 없음.**
