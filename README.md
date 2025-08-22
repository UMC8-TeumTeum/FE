# UMC 8th 틈틈(teumteum) Frontend
" Micro-break × 콘텐츠 × 사회적 연결
하루의 빈틈을 가치 있는 시간으로 채우는 플랫폼
<img width="2880" height="1620" alt="image" src="https://github.com/user-attachments/assets/871115d8-c1a0-4107-ad39-5f76538bdd56" />
*틈틈*은 매번 새로운 계획을 고민하고 실행하는 것이 번거롭게 느껴져 무의미하게 시간을 흘려보낸 사람들을 위한 서비스입니다.
단순한 일정 관리 캘린더에서 벗어나, *나의 일정을 기반으로 하루의 빈틈을 찾아 삶의 질을 향상*시켜 보세요!

### 🚀 Tech Stack

### Framework & Library

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![XML](https://img.shields.io/badge/XML-555555?logo=w3c&logoColor=white)
![Jetpack_ViewBinding](https://img.shields.io/badge/Jetpack%20ViewBinding-34A853?logo=android&logoColor=white)
![Glide](https://img.shields.io/badge/Glide-0FAAFF?logo=android&logoColor=white)
![Retrofit2](https://img.shields.io/badge/Retrofit2-2C3E50)
![Gson](https://img.shields.io/badge/Gson-4285F4?logo=google&logoColor=white)
![Kotlin_Coroutines](https://img.shields.io/badge/Kotlin%20Coroutines-7F52FF?logo=kotlin&logoColor=white)
![MVVM](https://img.shields.io/badge/MVVM-6C63FF)

### Push & Messaging

![Firebase%20Cloud%20Messaging](https://img.shields.io/badge/Firebase%20Cloud%20Messaging-FFCA28?logo=firebase&logoColor=black)


# 💻 Code Convention

## 네이밍 룰(Naming Rules)

1. **패키지 네이밍** - 항상 소문자로 작성하고 언더스코어( _ )를 사용하지 않으며, 여러 단어를 연결하는 경우 카멜 케이스를 사용합니다.
2. 클래스 네이밍 - 대문자로 시작하는 파스칼 케이스를 사용합니다.

```kotlin
class MyClass : MyInterface {
    ...
}
```

3. 메소드, 변수 네이밍 - 소문자로 시작하는 카멜 케이스를 사용합니다.

```kotlin
//변수 예시
private var myProperty: Int

//메소드 예시
fun myMethod() {
    ...
}
```

4. 상수 네이밍 - 대문자와 언더스코어로 구분된 어퍼 케이스를 사용합니다.

```kotlin
const val MAX_COUNT = 8
val USER_NAME_FIELD = "UserName
```

## 소스코드 구성(Source code organization)

클래스의 내용은 다음 순서로 작성합니다.

1. 속성 선언과 초기화 블록
2. 보조 생성자
3. 메소드 선언

```kotlin
//클래스 예시
class MyClass {

    // 속성 선언
    private val property1: String
    private val property2: Int

    // 초기화 블록
    init {
        property1 = "Hello"
        property2 = 42
    }

    // 보조 생성자
    constructor(param1: String) { ... }

    // 보조 생성자
    constructor(param1: String, param2: Int) { ... }

    // 메소드 선언
    fun method1() { ... }

    // 메소드 선언
    fun method2() {
    	InternalNestedClass()
    	... 
    }

    // 내부에서 사용된 Nested classes
    private class InternalNestedClass { ... }

    // Companion object
    companion object { ... }

    // Nested classes
    class NestedClass { ... }

    // 외부에서 사용된 Nested classes 
    class ExternalNestedClass { ... }
}
```

## 형식 (Formatting)

1. 들여쓰기 - 들여쓰기에는 네 개의 공백을 사용합니다.
2. 콜론( : ) - 다음 경우에는 콜론 앞에 공백을 넣습니다.
    - 타입과 상위 타입을 구분하는 데 사용될 때
    - 슈퍼클래스 생성자로 위임하거나 동일한 클래스의 다른 생성자로 위임할 때
    - "object" 키워드 뒤에 사용될 때
    - 선언과 해당하는 타입을 구분하는 콜론 앞에는 공백을 넣지 마세요.
    - 콜론 뒤에는 항상 공백을 넣으세요.

참고 자료: https://kotlinlang.org/docs/coding-conventions.html#verify-that-your-code-follows-the-style-guide,  https://effortguy.tistory.com/257

# 🛠️ Branch Strategy

## 브랜치 유형

| 브랜치 유형 | 내용 |
| --- | --- |
| `main` | 완성된 버전의 코드를 저장하는 브랜치 |
| `dev` | 개발이 진행되는 동안 완성된 코드를 저장하는 브랜치 |
| `feat` | 작은 단위의 작업이 진행되는 브랜치 |
| `hotfix` | 긴급한 오류를 해결하는 브랜치 |

## 브랜치 명

- 유형/#이슈번호-what
    
    ex) feat/#30-home-ui,  init/#1-add-font
    

| 카테고리 | 내용 |
| --- | --- |
| `feat` | 구현 |
| `mod` | 수정 |
| `add` | 추가 |
| `del` | 삭제 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |

# 📔 Git Convention

## Git Flow

1. Issue 생성
2. Branch 생성
3. Add - Commit - Push - Pull Request(PR)
    1. Commit은 최대한 자주, 적은 양
    2. Commit시에 Issue를 연결
4. PR이 작성되면 작성자 이외의 다른 팀원이 Code Review를 진행합니다.
5. Code Review가 완료되면 PR 작성자가 dev Branch로 Merge 합니다.
    1. Merge 후 카톡방에 무조건 공유합니다.
6. Merge 된 작업이 있으면 다른 브랜치에서 작업을 진행 중이던 개발자는 본인의 브랜치로 Merge된 작업을 Pull 받아옵니다. (최신화 습관 들이기!)

## 협업 규칙

- dev 브랜치에서의 작업은 금지합니다. 단, 초기 세팅 및 README 작성은 dev 브랜치에서 수행 가능합니다.
- 본인의 PR은 본인이 Merge합니다.
- Commit, Push, Merge, PR 등 모든 작업은 앱이 정상적으로 실행되는지 확인 후 수행합니다.

## Issue Convention

[카테고리] 제목 

ex) [INIT] 프로젝트 초기 세팅 

## Commit Convention

[커밋 카테고리/#이슈번호] 커밋 내용 (대문자)

ex) [FEAT/#30] 홈 뷰 구현, [ADD/#1] 폰트 파일 추가

| 커밋 카테고리 | 내용 |
| --- | --- |
| `feat` | 기능 (feature) |
| `fix` | 버그 수정 |
| `docs` | 문서 작업 (documentation) |
| `style` | 포맷팅, 세미콜론 누락 등, 코드 자체의 변경이 없는 경우 |
| `refactor` | 리팩토링 : 결과의 변경 없이 코드의 구조를 재조정 |
| `test` | 테스트 |
| `chore` | 변수명, 함수명 등 사소한 수정 *ex) .gitignore* |

## PR Convention

[카테고리/#이슈번호] 제목

ex) [FEAT/#6] 로그인 뷰 구현


# 📑 사용 기술 스택 및 라이브러리

| 구분 | 기술 / 라이브러리 | 설명 |
| --- | --- | --- |
| 언어 | Kotlin | 안드로이드 앱 개발을 위한 주요 언어
| UI 구성 | XML / Jetpack ViewBinding | 뷰 레이아웃 구성 및 바인딩 처리
| 이미지 로딩 | Glide | 네트워크 또는 리소스에서 이미지 비동기 로딩 및 캐싱 
| 네트워크 통신 | Retrofit2 + Gson | REST API 요청 및 JSON 파싱
| 비동기 처리 | Kotlin Coroutines | 비동기 작업을 효율적으로 처리하기 위한 코루틴 사용
| 앱 아키텍처 | MVVM | Model-View-ViewModel 아키텍처 적용
| 푸시 알림 | FCM(Firebase Cloud Messaging) | 알림 메시지 수신 처리


# ⚙️Android Studio 환경 설정
버전 : Meerkat

targetSDK : 35

minSDK : 26

테스트 환경 : Emulator(Pixel 8a)

