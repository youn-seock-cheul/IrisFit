# [PM 앱 현황 종합 감사 보고서] IrisFit 프로덕션 및 베타 MVP 점검

- 감사 일자: 2026-11-15
- 작성 역할: 수석 PM (`[PM]`)
- 대상 모듈: `:app` (`com.eyeoverlay.app`)
- 연계 지시서: `IRIS-REQ-BETA-001`
- 출시 상태: **Release Hold**

---

## 1. 개요 및 감사 목적

본 감사는 IrisFit 앱의 프로덕션 소스 코드(`src/production`), 빌드 구성(`build.gradle.kts`), 매니페스트(`AndroidManifest.xml`), 보안 및 Google Play 정책 준수 여부를 읽기 전용으로 검증하고, 베타 MVP 출시 가능성을 파악하기 위해 수행되었습니다.

---

## 2. 매니페스트 및 권한 감사 (Google Play 정책 체크)

| 권한 / 컴포넌트 | 선언 위치 | 사용 목적 | 정책 및 보안성 평가 |
|---|---|---|---|
| `android.permission.POST_NOTIFICATIONS` | `main/AndroidManifest.xml` | 눈 휴식 알림 발송 | Android 13+ 런타임 권한 준수 |
| `android.permission.VIBRATE` | `main/AndroidManifest.xml` | 휴식 완료 피드백 | 일반 권한 |
| `android.permission.PACKAGE_USAGE_STATS` | `main/AndroidManifest.xml` | 오늘 누적 사용시간 측정 | 선택적 런타임 권한. 미허용 시 `미측정`으로 대체하여 유저 경험 저해 방지 |
| `CAMERA`, `SYSTEM_ALERT_WINDOW` | **`release` 빌드 제외** | 실험용 비전/오버레이 | `VerifyReleaseBoundary` Gradle 가더를 통해 릴리즈 빌드에서 완벽 차단됨 |

* **특이사항:** 10가지 동적 앱 아이콘 테마(`MainActivityDefault` ~ `MainActivityGalaxy`)가 `activity-alias`로 정상 선언되어 유저 스트릭 및 레벨에 따른 동적 아이콘 전환이 안전하게 지원됩니다.

---

## 3. 기능 구현 및 품질 인벤토리

1. **IrisFit Battery (눈 에너지 지표):**
   * `ScreenTimeManager.kt`를 통해 오늘 자정 이후 누적 화면 사용 시간을 계산합니다.
   * `WellnessHome.kt` `EyePedometerCard`에 `오늘 누적 화면 사용: X시간 Y분` (권한 미허용 시 `미측정`) 문자열과 4단계(Optimal, Caution, Warning, Danger) 색상 및 배터리 그래픽이 정상 바인딩되어 있습니다.
2. **30초 눈 휴식 및 눈 운동 2종:**
   * `GuidedSessionViewModel.kt`에서 휴식 및 Eye Gym, Smooth Pursuit 세션을 상호 배타적(Mutex)으로 관리합니다.
   * `CompletionRepository.kt` 및 `FileCompletionStorage.kt`를 통해 기기 내 로컬 저장이 멱등하게 작동합니다.
3. **미구현 기능 차단 팝업 (베타 가드):**
   * 하단 탭의 AI 코치 및 서브 진입점 클릭 시 `"베타 준비 중입니다."` `AlertDialog`가 노출되어 크래시 및 미완료 기능 노출을 방지합니다.

---

## 4. 빌드 및 배포 안정성 검증

* **16 KB ELF Page Alignment:** Android 15/16 대응 64비트 ELF 정렬 검증 가드(`VerifyNativeAlignment`) 포함.
* **Release Boundary Isolation:** Debug 전용 연구용 SDK(CameraX, ML Kit Face Detection, Overlay Service)가 Release 빌드 타겟에 포함되지 않음을 정적 검증 완료.

---

## 5. 결론 및 다음 단계

* **감사 결과:** **PASS (베타 MVP 준비 완료)**
* **현재 상태:** `Release Hold` 유지 중
* **PM 권고사항:** 사용자와의 협의를 거쳐 내부 테스트 트랙 빌드(`verifyReleaseSigningConfig` 실행)를 승인하고 다음 단계로 진행합니다.
