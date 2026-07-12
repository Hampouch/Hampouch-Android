# 온보딩 화면 UI 정합화 변경 사항

첨부된 목업(9개 화면)과 완전히 동일하도록 온보딩 플로우 전체를 재구현했습니다. 아래는 추가/삭제/주요 수정 파일 목록입니다.

## 삭제된 파일
- `ui/onboarding/steps/SalaryDaySheet.kt` — 월급날 프리셋(1일/10일/25일) 선택 시트. 새 목업에서는 "월급날" 입력 행을 탭하면 바로 달력(화면7)이 뜨는 구조로 바뀌어 더 이상 사용되지 않아 삭제했습니다.

## 신규 추가 파일
- `docs/onboarding_ui_update.md` (본 문서)

## 주요 수정 파일
- `res/values/strings.xml` — STEP 배지, 캡션/푸트노트, 카테고리 9종(배달/외식/편의점/카페/간식/장보기/외식(중복)/술자리/직접추가), 하단 네비 라벨, 기간 프리셋(3/7/14/31일), 월급날 리셋 토글 문구, "현재 하루 한도" 등 신규 문자열 대거 추가.
- `ui/onboarding/OnboardingMockData.kt` — 카테고리 9종(목업과 동일하게 "외식"이 두 번 노출되며, 내부 id는 `dining_out`/`dining_out_home`으로 구분해 각각 독립적으로 선택 가능), 기간 프리셋을 3/7/14/31일로 변경. 월급일 프리셋 데이터는 제거.
- `ui/onboarding/OnboardingUiState.kt` — `resetOnSalaryDay: Boolean` 필드 추가(월급날 기준 리셋 토글 상태), 기간 기본값 7일로 변경.
- `ui/onboarding/components/OnboardingComponents.kt` — 대폭 재구성
  - `OnboardingTopBar`(뒤로가기+알림 아이콘), `OnboardingCaptionText`, `OnboardingFootnoteText`, `SkipText`, `OnboardingBottomNavBar`(홈/햄배틀/+/커뮤니티/마이 데코레이션 하단 네비) 신규 추가
  - `OnboardingHeaderCard`를 "STEP N 배지 + 타이틀"만 담도록 축소(서브타이틀은 카드 밖으로 이동)
  - `LabeledInputRow`에서 chevron 아이콘 제거, 연필/달력 등 선택 가능한 leading icon으로 교체
  - `CategoryChip`에 아이콘(nullable) + 강조색 원형 배지 렌더링 추가
  - 하드코딩된 `FloatingNextButton` 삭제(목업에 없음, 미사용 코드 제거)
  - `AmountInputSheet`(ModalBottomSheet)를 `DirectInputOverlay`(다이얼로그 + 딤 배경 + 마스코트 + pill형 입력 필드)로 전면 교체
- `ui/onboarding/steps/SplashStep.kt` — 로고 노출 후 유지시간을 2초로 조정.
- `ui/onboarding/steps/LoadingStep.kt` — 스피너 제거, "LOADING…" 볼드 텍스트로 교체, 마스코트 크기 축소.
- `ui/onboarding/steps/ExpenseDiagnosisStep.kt` — 상단바/프로그레스바/STEP 헤더/캡션/푸트노트/건너뛰기/하단 네비 추가, 다음 버튼을 "다음으로"로 통일, 직접입력을 `DirectInputOverlay`로 교체.
- `ui/onboarding/steps/ChallengeGoalStep.kt` — 기간 세그먼트 3/7/14/31일+직접입력, 월급날 필드가 달력 오버레이로 직결, "월급날 기준으로 리셋" 토글 추가, "현재 하루 한도" 카드 스타일 변경, 버튼 텍스트 "저장하고 홈으로" → "다음으로" 변경.
- `ui/onboarding/steps/CategorySelectStep.kt` — STEP3 헤더/캡션/카테고리 라벨/푸트노트 추가, 3x3 아이콘 칩 그리드 반영.
- `ui/onboarding/steps/SalaryCalendarSheet.kt` — ModalBottomSheet → 다이얼로그(딤 배경 + 중앙 카드) 방식으로 변경, 요일 헤더 행과 "📅 월급날 선택" 배지 타이틀 추가.
- `ui/onboarding/OnboardingScreen.kt` — 각 스텝에 `onBack` 콜백과 `onResetOnSalaryDayChange` 콜백 연결.

## 확인 사항
- `gradlew compileDebugKotlin`, `gradlew assembleDebug` 모두 성공 확인.
- 각 화면 파일에 `@Preview` 함수를 유지/추가하여 Android Studio에서 바로 미리보기 가능.
