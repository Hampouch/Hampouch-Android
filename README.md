# Hampouch-Android

Hampouch Android Repository

## 개발 환경

- JDK 17
- Android Studio 최신 안정 버전 및 Android SDK API 36 (Build Tools 포함)
- Android SDK 경로가 담긴 개인 `local.properties`

Android Studio에서 프로젝트를 열면 Gradle Wrapper(`9.4.1`)가 필요한 Gradle 버전을 내려받습니다.

## 로컬 설정

저장소 루트에 아래처럼 `local.properties`를 만듭니다. 이 파일은 `.gitignore`에 포함되어 있어 커밋하지 않습니다.

```properties
sdk.dir=/absolute/path/to/Android/sdk
KAKAO_NATIVE_APP_KEY=your_kakao_native_app_key
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id
BASE_URL=https://api.hampouch.com/
```

`KAKAO_NATIVE_APP_KEY`, `GOOGLE_WEB_CLIENT_ID`, `BASE_URL`은 로컬 실행에 필요한 값만 넣습니다. 키·토큰·실서비스 URL을 이 문서, 소스, 로그에 추가하지 않습니다.

### Firebase 설정

Firebase Analytics를 포함해 앱을 실행하려면 Firebase Console에서 이 앱의 package name(`com.example.hampouch`)에 맞는 `google-services.json`을 내려받아 `app/google-services.json`에 둡니다. 이 파일은 git에서 제외되어 있으며 공유 저장소에 올리지 않습니다.

Google Services Gradle 플러그인은 설정 파일이 있는 환경에서만 적용됩니다. 따라서 Firebase 설정이 없는 새 checkout과 CI에서도 debug compile, lint, unit test, APK assemble은 실행할 수 있습니다. Firebase Analytics를 실제로 검증하거나 release APK를 배포하기 전에는 올바른 설정 파일을 제공해야 합니다.

## 검증 명령

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:detekt :app:assembleDebug
```

PR과 `dev` push에서는 GitHub Actions가 Firebase 설정을 건너뛰는 `-PskipGoogleServices`와 함께 같은 검증을 수행합니다. lint, detekt, unit test report는 실패 시 Actions artifact에서 확인할 수 있습니다.
