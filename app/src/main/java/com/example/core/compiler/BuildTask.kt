package com.example.core.compiler

/**
 * مهام الـ Gradle المدعومة في MobileForge IDE
 */
enum class GradleTask(val taskName: String, val description: String) {
    PRE_BUILD(":app:preBuild", "تهيئة وفحص مساحة العمل والأذونات"),
    CHECK_AAR_METADATA(":app:checkDebugAarMetadata", "التحقق من توافق ملفات AAR والـ Min SDK"),
    AAPT2_COMPILE(":app:compileDebugAapt2Resources", "معالجة ملفات XML والـ Drawables إلى حزم ثنائية .flat"),
    AAPT2_LINK(":app:linkDebugResources", "توليد ملف R.java وربط الموارد مع AndroidManifest.xml"),
    COMPILE_JAVA(":app:compileDebugJavaWithJavac", "ترجمة كلاسات Java بواسطة ECJ داخل ART"),
    COMPILE_KOTLIN(":app:compileDebugKotlin", "ترجمة ملفات Kotlin المصدرية بواسطة Kotlinc"),
    D8_DEX(":app:dexBuilderDebug", "تحويل كلاسات JVM Bytecode إلى ملفات classes.dex بواسطة D8"),
    PACKAGE_APK(":app:packageDebug", "حزم ملفات dex والموارد داخل هيكل أرشيف ZIP الأولي"),
    ZIPALIGN(":app:zipalignDebug", "محاذاة الموارد والبيانات على حدود 4-byte لسرعة قراءة الذاكرة"),
    APKSIGNER(":app:signDebugApk", "توقيع حزمة الـ APK الرقمي بشهادة V1/V2/V3")
}

data class BuildLogEntry(
    val text: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO,
    TASK,
    TASK_UP_TO_DATE,
    SUCCESS,
    WARNING,
    ERROR
}
