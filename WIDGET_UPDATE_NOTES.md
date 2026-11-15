# IrisFit - 첫 번째 눈 건강 요약 위젯 적용본

## 변경 내용
- 기존 `IrisFitWidget1Provider`를 그대로 사용했습니다. 새 위젯 receiver나 중복 위젯 등록은 추가하지 않았습니다.
- 첫 번째 위젯의 화면을 단색 네이비 기반의 평면 디자인으로 정리했습니다.
- 눈 건강 배터리 점수, 오늘 휴식 완료 횟수, 눈 운동 완료 횟수를 기존 앱 데이터에서 읽습니다.
- 휴식/운동 완료 목표(휴식 5회, 운동 3회)에 대한 평균 진행률을 달성률로 표시합니다.
- 세션 기록 변경 시 `BitmapWidgetProvider.updateWidget1()`을 호출해 첫 번째 위젯만 갱신합니다.
- 그림자, 그라데이션, 애니메이션은 사용하지 않습니다.

## 적용 방법
1. ZIP 압축을 풉니다.
2. 기존 프로젝트를 백업합니다.
3. ZIP 안 `MyApplication` 폴더의 내용을 기존 `C:\Users\youns\AndroidStudioProjects\MyApplication`에 덮어씁니다.
4. Android Studio에서 프로젝트를 열고 Sync Project with Gradle Files를 실행합니다.
5. Build > Make Project로 빌드합니다.
6. 휴대폰에 설치한 뒤 홈 화면을 길게 누름 → 위젯 → IrisFit → 첫 번째 눈 건강 요약 위젯을 추가합니다.

## 위젯 데이터
- 눈 건강 배터리: 기존 `EyeBatteryCalculator`의 점수
- 휴식 완료: 오늘 `screen_rest` 완료 기록 / 목표 5회
- 눈 운동 완료: 오늘 `eye_gym` 및 `smooth_pursuit` 완료 기록 / 목표 3회
- 오늘 목표 달성률: 휴식 진행률과 운동 진행률의 평균

## 참고
- Android AppWidget은 호스트 런처가 크기와 배치를 결정합니다. 제공된 위젯은 기존 4×2에 가까운 중형 구성을 사용합니다.
- 실제 기기에서의 표시 크기와 줄바꿈은 런처/화면 밀도에 따라 달라질 수 있습니다.
- 이 환경에서는 Android Studio가 설치된 사용자 PC의 실제 기기/런처를 실행할 수 없으므로, 최종 기기 확인은 프로젝트를 열어 수행해야 합니다.


## 2026-11-12 정리
- Widget 2~6은 사용자 요청으로 제거했습니다. 현재 홈 화면 위젯 선택 목록에는 Widget 1만 노출됩니다.
- `IrisFitWidget1Provider`만 Manifest에 등록되어 있습니다.
