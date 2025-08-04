package com.example.teumteum.ui.signup.viewModel

enum class OnBoardingStep {
    Agreement,               // 약관 동의
    AgreementComplete,       // 약관 동의 완료 화면
    NicknameJob,             // 닉네임/직업 입력 (API)
    ProfileImage,            // 프로필 이미지 선택 (선택적 API)
    SleepPattern,            // 수면 패턴 입력 (선택적 API)
    Schedule,                // 반복 일정 등록 (선택적 API)
    Reminder,                // 리마인드 알림 설정 (선택적 API)
    Complete                 // 온보딩 완료
}