package com.example.domain.model

/**
 * أنواع المشاريع المدعومة في MobileForge IDE
 */
enum class ProjectType(
    val title: String,
    val subtitle: String,
    val language: String,
    val iconName: String
) {
    COMPOSE(
        title = "تطبيق Jetpack Compose",
        subtitle = "واجهات برمجية تصريحية حديثة بـ Kotlin بنسبة 100%",
        language = "Kotlin",
        iconName = "compose"
    ),
    XML_KOTLIN(
        title = "تطبيق Kotlin + XML",
        subtitle = "تخطيطات كلاسيكية مع مصمم واجهات مرئي و Kotlin",
        language = "Kotlin",
        iconName = "xml_kt"
    ),
    XML_JAVA(
        title = "تطبيق Java + XML",
        subtitle = "تطوير أندرويد قياسي بلغة Java ومترجم ECJ",
        language = "Java",
        iconName = "java"
    ),
    WEB_APP(
        title = "تطبيق ويب هجين (HTML5)",
        subtitle = "واجهات خفيفة بـ HTML/CSS/JS مع معاينة فورية بـ WebView",
        language = "HTML/JS",
        iconName = "web"
    ),
    BLANK(
        title = "مشروع فارغ مخصص",
        subtitle = "هيكل أولي مرن لتخصيص كامل",
        language = "Kotlin",
        iconName = "blank"
    )
}
