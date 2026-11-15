# IrisFit Walkthrough

최신 변경 이력과 검증 상태는 [`docs/WALKTHROUGH_P0_LATEST.md`](docs/WALKTHROUGH_P0_LATEST.md)를 기준으로 확인하세요.


## 2026-11-13 — 앱 자체 블루라이트 오버레이 제거 및 시스템 설정 바로가기 유지

### 변경 사항
- 사용자가 IrisFit 자체 블루라이트 필터를 사용하지 않는다고 확인하여 `BlueLightFilterService.kt`를 삭제했습니다.
- 해당 서비스의 불필요한 import와 앱 자체 오버레이 필터 구현을 제거했습니다.
- 기존 하단 탭의 시력 보호 화면은 유지했습니다. 이 화면은 `Settings.ACTION_DISPLAY_SETTINGS`로 휴대폰 디스플레이 설정을 여는 기능만 제공합니다.
- 화면 설명을 수정하여 IrisFit이 직접 블루라이트를 차단하거나 Eye Comfort Shield 상태를 제어한다고 오해하지 않도록 했습니다.
- 화면 색온도 기능이 안구 건조증·망막 피로를 예방한다는 식의 근거가 불명확한 문구를 제거했습니다.
- 메뉴/다국어 키를 `eye_comfort`로 정리했습니다.
- 오버레이 권한은 눈 운동 오버레이 서비스가 사용하므로 유지했습니다.

### 검증 상태
- 정적 검색에서 `BlueLightFilterService` Kotlin 구현 및 앱 코드 호출/import가 제거된 것을 확인했습니다.
- 사용자가 직전 수정본의 Release 빌드 성공을 확인했습니다. 이번 변경분은 별도 Release 빌드 및 테스트가 아직 실행되지 않았습니다.

### 로컬 재검증 명령
```powershell
.\gradlew.bat app:testDebugUnitTest
.\gradlew.bat app:lintRelease
.\gradlew.bat app:assembleRelease
```

---

## 2026-11-12 — Widget 1 단일화 및 Release 컴파일 오류 수정 (이전 단계 기록)

### 변경 사항
- 홈 화면 위젯은 `IrisFitWidget1Provider`만 남겼습니다. Widget 2~6의 Manifest receiver, Provider 선언, appwidget-info XML, 레이아웃 및 사용하지 않는 미리보기 리소스를 제거했습니다.
- 세션 완료/기록 갱신 경로는 Widget 1만 갱신하도록 변경했습니다.
- 당시에는 `BlueLightFilterService`를 Android 기본 `Service`로 변경해 Release 컴파일 오류를 우회했습니다. 이후 2026-11-13 사용자 요청으로 앱 자체 블루라이트 오버레이 서비스 전체를 삭제했으므로, 이 내용은 이전 중간 단계의 이력입니다.
- README의 위젯 개수/구조 설명을 실제 구현과 일치시켰습니다.

### 검증 상태
- 정적 확인: Manifest에 Widget 1 receiver만 남음, Widget 2~6 리소스 및 Provider 참조 제거 확인.
- 당시 중간 단계에서 `BlueLightFilterService`의 Android `Service` 상속 및 `onBind()` 구현을 확인했습니다. 해당 서비스는 2026-11-13 후속 요청으로 최종 삭제되었습니다.
- **미실행:** 이 환경에서 Android SDK/Gradle 빌드를 실행할 수 없어 `app:assembleRelease`, 단위 테스트, Lint 및 실기기 검증은 아직 실행하지 않았습니다. 따라서 Release 빌드 성공을 보장한다고 판정하지 않습니다.

### 로컬 재검증 명령
```powershell
.\gradlew.bat app:assembleRelease
.\gradlew.bat app:testDebugUnitTest
.\gradlew.bat app:lintRelease
```


## Gemini Nano 온디바이스 코치 (2026-11-13)

- `GeminiNanoCoachManager`는 Google ML Kit GenAI Prompt API (`com.google.mlkit:genai-prompt:1.0.0-beta4`)의 `Generation.getClient()`를 사용해 Android AICore의 Gemini Nano를 호출합니다. API 키나 자체 클라우드 추론 서버는 사용하지 않습니다.
- AI 탭에 실제 대화 UI를 연결했습니다. 앱은 `checkStatus()`로 `AVAILABLE / DOWNLOADABLE / DOWNLOADING / UNAVAILABLE` 상태를 확인하고, 다운로드 가능한 기기에서는 사용자가 최초 모델 다운로드를 시작할 수 있습니다.
- 미지원 기기에서는 Gemini Nano가 동작한다고 가장하지 않습니다. 지원되지 않음/준비 실패 상태를 사용자에게 표시합니다.
- 답변은 일반 눈 건강 생활습관 안내이며 진단·치료·약 처방을 금지하는 프롬프트를 사용합니다. 갑작스러운 시력 변화, 심한 통증, 외상·화학물질 노출 등은 생성형 답변보다 우선하는 로컬 안전 안내로 처리합니다.
- Prompt API는 베타이며 지원 기기, AICore 버전, 모델 다운로드 상태, 지역/언어 구성에 따라 사용 가능 여부가 달라집니다. 실제 기기에서 상태 확인과 첫 추론을 반드시 검증해야 합니다.
- Release 검증 가드는 GenAI Prompt 및 GenAI Common 모듈만 허용하고, 카메라/비전 연구 SDK 차단은 유지합니다.
- **검증 유의:** 이 수정 ZIP은 소스 정적 점검만 수행했습니다. `app:testDebugUnitTest`, `app:lintRelease`, `app:assembleRelease`는 새 ZIP에서 다시 실행해야 합니다.
