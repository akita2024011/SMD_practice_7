# FramePhases — Jetpack Compose Phases

## Android Studio'da ochish
1. `FramePhases_Project.zip` arxivini chiqaring.
2. Android Studio'da **Open** tugmasini bosing va `FramePhases` papkasini tanlang.
3. Gradle Sync tugashini kuting.
4. Run (▶) orqali emulator yoki telefoningizda ishga tushiring.

## JVM target tuzatishi
`app/build.gradle.kts` faylida Java va Kotlin uchun bir xil JVM target (`1.8`) o'rnatilgan. Bu `compileDebugJavaWithJavac (1.8)` va `compileDebugKotlin (22)` mos kelmasligi xatosini tuzatadi.

Agar Android Studio JDK 25 haqida ogohlantirish bersa, `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK` bo'limidan JDK 17 yoki Android Studio tavsiya qilgan mos JDK'ni tanlang. So'ng **Sync Project with Gradle Files** qiling.
