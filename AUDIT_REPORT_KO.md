# IrisFit 소스 전수 점검 1차 진단 보고서

점검 대상: 사용자가 2026-11-12 업로드한 `MyApplication(1).zip`

## 판정 요약

- **완전 정상/출시 가능 판정은 보류**합니다. 이 실행 환경에는 프로젝트가 요구하는 Gradle 9.6.0 배포본과 Android SDK가 없고 외부 네트워크도 차단되어 있어 Gradle 빌드, Kotlin 컴파일, JUnit, Android Lint, APK 설치/실기기 검증을 실행할 수 없었습니다.
- 정적 검사에서는 `app/src` 아래 XML 83개가 모두 파싱되었습니다.
- 수정 후 소스 테스트 선언은 로컬 단위 테스트 33개(기존 28개 + 배터리 계산 신규 5개), Android UI 테스트 9개입니다. 문서의 “28개 통과”는 이번 업로드 소스의 실행 결과로 재확인된 것이 아닙니다.

## 이번 수정

1. 위젯 1~6 중 실제 Manifest에 등록되어 있던 위젯이 1과 4뿐이던 문제를 수정했습니다.
2. `IrisFitWidget2Provider`, `3`, `5`, `6` Provider 클래스를 추가했습니다.
3. 위젯 2, 3, 5, 6의 AppWidget receiver와 각 provider-info XML 연결을 Manifest에 추가했습니다.
4. 완료 기록 갱신 시 위젯 1만 갱신하던 흐름을 위젯 1~6 전체 갱신으로 변경했습니다.
5. 배터리 계산의 0분 인위적 차감, 소수점 차감 절삭, 음수 입력 처리를 보완하고 신규 단위 테스트 5개를 추가했습니다.
6. 위젯 3 터치는 홈이 아니라 눈 운동 화면(`target_screen=gym`)을 열도록 분기하고 위젯별 PendingIntent requestCode를 분리했습니다.
7. 미사용 `READ_CONTACTS` 권한을 제거했습니다.
8. XML 83개 파싱, 위젯 1~6 클래스/레이아웃/정보 XML/Manifest 매핑을 정적 검사해 통과했습니다.

## 확인된 주요 이슈 및 잔여 위험

### P0 — 빌드 및 런타임 검증 미완료

Gradle wrapper는 `gradle-9.6.0-bin.zip`을 다운로드하려 했으나 네트워크 DNS 차단으로 실패했습니다. 업로드된 `local.properties`는 사용자의 Windows SDK 절대 경로(`C:\Users\youns\AppData\Local\Android\Sdk`)를 가리켜 이 Linux 검사 환경에서 사용할 수 없습니다. 따라서 컴파일/테스트 성공을 주장할 수 없습니다.

### P1 — 문서와 실제 AI 기능 불일치

- `GeminiNanoCoachManager.kt`는 Gemini Nano SDK/API를 호출하지 않습니다. 질문 문자열의 키워드에 따라 미리 작성된 문장을 반환하는 로컬 규칙 기반 함수입니다.
- `AiCoachChatDialog.kt`도 현재 Gradle main source set에 포함되지 않습니다. main source set은 `src/production/kotlin` 및 main/java의 일부 디렉터리만 선택합니다.
- 실제 production `AiCoachTab`은 사용 시간/점수/PRO 소개 화면이며 GPT/Gemini와 대화하는 채팅 UI가 아닙니다.
- 따라서 README 및 Walkthrough의 “Gemini Nano AI 1:1 맞춤 진단 구현 완료”는 현재 판매 빌드의 실제 기능으로 표현하면 안 됩니다.
- 하드코딩된 답변 중 “안구건조증 상태”, “수정체 조절근 과수축”, “외안근 과수축 및 시신경 자극” 등은 진단처럼 단정합니다. 실제 측정/의료진 진단이 아니므로 의료적 진단 문구를 제거하고 일반 웰니스 안내로 바꿔야 합니다.

### P1 — 위젯 기능 완성도

- 위젯 1~6은 현재 Provider 등록을 보완했습니다. 다만 위젯 2 주간 그래프의 데이터가 실제 일별 기록인지, 위젯 4의 “7일째”가 실제 연속 기록인지, 위젯 3의 운동 버튼이 운동 화면으로 바로 이동하는지 등은 별도 기능 테스트가 필요합니다.
- 공통 클릭 동작은 MainActivity 홈 화면을 여는 방식입니다. 위젯 3의 “눈 운동 시작” 버튼을 눌렀을 때 바로 운동 화면으로 이동하는 동작은 아직 구현/검증되지 않았습니다.
- 위젯 렌더러가 800x380 또는 800x180 고정 비트맵을 생성하므로 기기별 위젯 셀 크기, 글자 잘림, 메모리 압박을 실기기에서 확인해야 합니다.

### P1 — 개인정보 처리방침과 실제 저장 구현 불일치

- `PRIVACY_POLICY.md`는 “Encrypted SharedPreferences / Room database”라고 설명하지만 완료 기록 저장소는 `AtomicFile` 기반의 앱 내부 일반 텍스트 파일입니다(`FileCompletionStorage.kt`).
- 문서의 “커뮤니티 데이터가 동기화될 수 있음” 표현과 실제 `RelayPreferences`의 로컬 저장 구현 간 범위를 대조해야 합니다.
- `READ_CONTACTS` 권한은 프로젝트 전체 코드에서 사용처가 검색되지 않았습니다. 연락처 기능이 없다면 Manifest에서 제거하는 것이 최소 권한 원칙에 맞습니다.

### P2 — 눈 건강 배터리 산식/표시 정합성

- README 수식은 `100 - BaseDrain + 회복 + 자세 보정`으로 설명하지만 구현은 `screenTimeMinutes`가 30분 미만이어도 최소 30분으로 계산합니다. 권한이 없거나 사용 시간이 0분인 경우에도 기본 차감 2점이 발생합니다.
- `baseDrain.toInt()` 및 `poseDelta.toInt()`로 소수점이 버려집니다. 15분 미만 사용의 점수 변화가 기대한 연속 감쇄와 다를 수 있습니다.
- 이 항목은 이번 수정에서 점수 범위 주석을 실제 clamp인 20~100으로 바로잡았습니다.
- 배터리 점수는 의학적 눈 피로 측정값이 아니라 앱이 정의한 사용시간/기록 기반 지표입니다. UI와 문서에서 실제 건강 상태 측정으로 오해되지 않게 표현해야 합니다.

### P2 — 빌드 구성/문서 정합성

- Gradle namespace는 `com.example.myapplication`, applicationId는 `com.eyeoverlay.app`입니다. 이는 기술적으로 가능하지만 README와 Walkthrough에 서로 다른 패키지명이 혼재되어 있어 역할을 명확히 문서화해야 합니다.
- app build.gradle.kts는 main source set을 `src/production/kotlin`과 main/java 일부 하위 폴더로 제한합니다. `src/main/java/.../ui/AiCoachChatDialog.kt` 등 선택되지 않은 소스는 production APK에 포함되지 않습니다.
- release Manifest는 네트워크/카메라/오버레이 권한을 제외하는 정책입니다. 따라서 release에서 온라인 GPT API 호출을 구현하려면 권한·백엔드·개인정보 고지·release boundary 검토가 별도로 필요합니다. API 키를 앱에 직접 넣어서는 안 됩니다.

## 검증 체크리스트

| 검사 | 결과 |
|---|---|
| ZIP 압축 해제 및 소스 트리 확인 | 완료 |
| README / Walkthrough 읽기 | 완료 |
| XML 83개 파싱 | 통과 |
| 위젯 1~6 클래스/레이아웃/provider-info/Manifest 매핑 | 수정 후 통과 |
| TODO/FIXME/NotImplemented 검색 | 해당 키워드 발견 안 됨 |
| Gradle configuration / Kotlin compile | 환경 제약으로 미실행 |
| Unit tests 28개 | 환경 제약으로 미실행 |
| Android UI tests 9개 | 환경 제약으로 미실행 |
| Lint / Release boundary / APK/AAB | 환경 제약으로 미실행 |
| 실기기/런처/알림/센서/권한 검증 | 환경 제약으로 미실행 |

## 사용자가 Android Studio에서 실행할 명령

```powershell
# 프로젝트 루트에서
.\gradlew.bat clean
.\gradlew.bat app:testDebugUnitTest
.\gradlew.bat app:assembleDebug
.\gradlew.bat app:lintDebug
.\gradlew.bat app:assembleRelease
```

빌드/테스트 결과의 첫 번째 `FAILURE`, `e:` 또는 `Android resource linking failed` 로그를 공유하면 해당 오류를 이어서 수정할 수 있습니다.


## 2026-11-12 정정 사항
- 본 보고서 앞부분의 Widget 2~6 Provider 추가 기록은 이전 수정 단계의 이력입니다. 최신 프로젝트에서는 사용자 요청에 따라 Widget 1만 유지하고 Widget 2~6 등록/리소스/클래스를 제거했습니다.
- Release 컴파일 오류의 중간 해결 단계에서는 `BlueLightFilterService`를 Android 기본 `Service`로 변경했습니다. 이후 2026-11-13 사용자 요청에 따라 앱 자체 오버레이 필터인 `BlueLightFilterService` 전체를 삭제했습니다. 기존 시력 보호 탭은 휴대폰 디스플레이 설정 바로가기로 유지합니다.
- 이번 정정본의 Release 빌드/테스트/Lint는 아직 로컬 Android SDK 환경에서 재실행되지 않았습니다.


## 2026-11-13 후속 변경
- 앱 자체 블루라이트 오버레이 서비스 `BlueLightFilterService.kt` 삭제.
- 기존 시력 보호 탭은 시스템 디스플레이 설정 화면으로 이동하는 기능만 유지.
- 오버레이 권한은 눈 운동 오버레이 서비스에서 사용하므로 유지.
- 이번 변경 후 빌드/테스트/Lint 재실행 필요.
