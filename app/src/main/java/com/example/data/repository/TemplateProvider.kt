package com.example.data.repository

import com.example.domain.model.ProjectFile
import com.example.domain.model.ProjectType

/**
 * مزود القوالب الجاهزة للمشاريع البرمجية
 */
object TemplateProvider {

    fun generateInitialFiles(
        projectId: Long,
        projectName: String,
        packageName: String,
        type: ProjectType
    ): List<ProjectFile> {
        val packagePath = packageName.replace('.', '/')
        return when (type) {
            ProjectType.COMPOSE -> listOf(
                ProjectFile(
                    projectId = projectId,
                    name = "MainActivity.kt",
                    path = "app/src/main/java/$packagePath/MainActivity.kt",
                    extension = "kt",
                    content = """
                        package $packageName

                        import android.os.Bundle
                        import androidx.activity.ComponentActivity
                        import androidx.activity.compose.setContent
                        import androidx.activity.enableEdgeToEdge
                        import androidx.compose.foundation.layout.*
                        import androidx.compose.material3.*
                        import androidx.compose.runtime.*
                        import androidx.compose.ui.Alignment
                        import androidx.compose.ui.Modifier
                        import androidx.compose.ui.unit.dp

                        class MainActivity : ComponentActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                enableEdgeToEdge()
                                setContent {
                                    MaterialTheme {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            color = MaterialTheme.colorScheme.background
                                        ) {
                                            AppScreen()
                                        }
                                    }
                                }
                            }
                        }

                        @Composable
                        fun AppScreen() {
                            var counter by remember { mutableIntStateOf(0) }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "مرحباً بك في $projectName!",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "تم بناء هذا التطبيق بواسطة MobileForge IDE مباشرة من الهاتف 🚀",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { counter++ }) {
                                    Text("نقرات العداد: ${'$'}counter")
                                }
                            }
                        }
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    extension = "xml",
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                            <application
                                android:allowBackup="true"
                                android:label="$projectName"
                                android:theme="@style/Theme.Material3.DayNight.NoActionBar">
                                <activity
                                    android:name=".MainActivity"
                                    android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                        </manifest>
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "build.gradle.kts",
                    path = "app/build.gradle.kts",
                    extension = "kts",
                    content = """
                        plugins {
                            id("com.android.application")
                            kotlin("android")
                        }

                        android {
                            namespace = "$packageName"
                            compileSdk = 34

                            defaultConfig {
                                applicationId = "$packageName"
                                minSdk = 26
                                targetSdk = 34
                                versionCode = 1
                                versionName = "1.0"
                            }
                            buildFeatures {
                                compose = true
                            }
                        }

                        dependencies {
                            implementation("androidx.core:core-ktx:1.12.0")
                            implementation("androidx.compose.material3:material3:1.2.0")
                        }
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "strings.xml",
                    path = "app/src/main/res/values/strings.xml",
                    extension = "xml",
                    content = """
                        <resources>
                            <string name="app_name">$projectName</string>
                            <string name="welcome_message">أهلاً بك في تطبيقك الجديد</string>
                        </resources>
                    """.trimIndent()
                )
            )

            ProjectType.XML_KOTLIN -> listOf(
                ProjectFile(
                    projectId = projectId,
                    name = "MainActivity.kt",
                    path = "app/src/main/java/$packagePath/MainActivity.kt",
                    extension = "kt",
                    content = """
                        package $packageName

                        import android.os.Bundle
                        import android.widget.Button
                        import android.widget.TextView
                        import androidx.appcompat.app.AppCompatActivity

                        class MainActivity : AppCompatActivity() {
                            private var count = 0

                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                setContentView(R.layout.activity_main)

                                val txtTitle = findViewById<TextView>(R.id.txtTitle)
                                val btnAction = findViewById<Button>(R.id.btnAction)

                                btnAction.setOnClickListener {
                                    count++
                                    txtTitle.text = "تم الضغط ${'$'}count مرات!"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "activity_main.xml",
                    path = "app/src/main/res/layout/activity_main.xml",
                    extension = "xml",
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:gravity="center"
                            android:orientation="vertical"
                            android:padding="24dp">

                            <TextView
                                android:id="@+id/txtTitle"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="$projectName"
                                android:textSize="22sp"
                                android:textStyle="bold" />

                            <Button
                                android:id="@+id/btnAction"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:layout_marginTop="20dp"
                                android:text="اضغط هنا للتشغيل" />

                        </LinearLayout>
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    extension = "xml",
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                            <application
                                android:label="$projectName"
                                android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
                                <activity
                                    android:name=".MainActivity"
                                    android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                        </manifest>
                    """.trimIndent()
                )
            )

            ProjectType.XML_JAVA -> listOf(
                ProjectFile(
                    projectId = projectId,
                    name = "MainActivity.java",
                    path = "app/src/main/java/$packagePath/MainActivity.java",
                    extension = "java",
                    content = """
                        package $packageName;

                        import android.os.Bundle;
                        import android.widget.Button;
                        import android.widget.TextView;
                        import androidx.appcompat.app.AppCompatActivity;

                        public class MainActivity extends AppCompatActivity {
                            private int clickCount = 0;

                            @Override
                            protected void onCreate(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);
                                setContentView(R.layout.activity_main);

                                TextView tv = findViewById(R.id.txtTitle);
                                Button btn = findViewById(R.id.btnAction);

                                btn.setOnClickListener(v -> {
                                    clickCount++;
                                    tv.setText("Java Clicked: " + clickCount);
                                });
                            }
                        }
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "activity_main.xml",
                    path = "app/src/main/res/layout/activity_main.xml",
                    extension = "xml",
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent"
                            android:gravity="center"
                            android:orientation="vertical">

                            <TextView
                                android:id="@+id/txtTitle"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="تطبيق Java: $projectName"
                                android:textSize="20sp" />

                            <Button
                                android:id="@+id/btnAction"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="انقر للتفاعل" />

                        </LinearLayout>
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "AndroidManifest.xml",
                    path = "app/src/main/AndroidManifest.xml",
                    extension = "xml",
                    content = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                            <application android:label="$projectName">
                                <activity android:name=".MainActivity" android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                        </manifest>
                    """.trimIndent()
                )
            )

            ProjectType.WEB_APP -> listOf(
                ProjectFile(
                    projectId = projectId,
                    name = "index.html",
                    path = "app/src/main/assets/index.html",
                    extension = "html",
                    content = """
                        <!DOCTYPE html>
                        <html lang="ar" dir="rtl">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>$projectName</title>
                            <link rel="stylesheet" href="style.css">
                        </head>
                        <body>
                            <div class="container">
                                <h1>🚀 $projectName</h1>
                                <p>تطبيق هجين HTML5 مبني بالكامل على الهاتف!</p>
                                <button id="btnClick">انقر هنا لتشغيل التأثير</button>
                                <div id="output" class="box">جاهز للعمل...</div>
                            </div>
                            <script src="app.js"></script>
                        </body>
                        </html>
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "style.css",
                    path = "app/src/main/assets/style.css",
                    extension = "css",
                    content = """
                        body {
                            font-family: system-ui, -apple-system, sans-serif;
                            background: #0f172a;
                            color: #f8fafc;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            min-height: 100vh;
                            margin: 0;
                            text-align: center;
                        }
                        .container {
                            padding: 2rem;
                            background: #1e293b;
                            border-radius: 16px;
                            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
                            max-width: 90%;
                        }
                        button {
                            background: #00e5ff;
                            color: #0b0f17;
                            font-weight: bold;
                            padding: 12px 24px;
                            border: none;
                            border-radius: 8px;
                            font-size: 1rem;
                            cursor: pointer;
                            transition: transform 0.2s;
                        }
                        button:active {
                            transform: scale(0.95);
                        }
                        .box {
                            margin-top: 1.5rem;
                            padding: 12px;
                            background: #334155;
                            border-radius: 8px;
                        }
                    """.trimIndent()
                ),
                ProjectFile(
                    projectId = projectId,
                    name = "app.js",
                    path = "app/src/main/assets/app.js",
                    extension = "js",
                    content = """
                        let count = 0;
                        document.getElementById('btnClick').addEventListener('click', () => {
                            count++;
                            const output = document.getElementById('output');
                            output.innerText = '⚡ عدد التفاعلات: ' + count + ' في ' + new Date().toLocaleTimeString();
                            output.style.borderColor = '#00e5ff';
                        });
                    """.trimIndent()
                )
            )

            ProjectType.BLANK -> listOf(
                ProjectFile(
                    projectId = projectId,
                    name = "Main.kt",
                    path = "src/Main.kt",
                    extension = "kt",
                    content = """
                        fun main() {
                            println("Hello, MobileForge IDE!")
                        }
                    """.trimIndent()
                )
            )
        }
    }
}
