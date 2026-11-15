import com.android.build.api.artifact.SingleArtifact
import javax.xml.parsers.DocumentBuilderFactory
import java.util.zip.ZipFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    signingConfigs {
        create("release") {
            val rootProp = rootProject.file("keystore.properties")
            if (rootProp.isFile) {
                val props = Properties().apply { rootProp.inputStream().use(::load) }
                storeFile = props.getProperty("storeFile")?.takeIf { it.isNotBlank() }?.let { file(it) }
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    val releaseSigning = android.signingConfigs.getByName("release")
    val signingPropertiesFile = rootProject.file("keystore.properties")
    val signingStoreFile = releaseSigning.storeFile
    val signingError = when {
        releaseSigning.storePassword.isNullOrBlank() -> "Release signing storePassword is missing."
        releaseSigning.keyAlias.isNullOrBlank() -> "Release signing keyAlias is missing."
        releaseSigning.keyPassword.isNullOrBlank() -> "Release signing keyPassword is missing."
        else -> null
    }
    val verifyReleaseSigningConfig = tasks.register("verifyReleaseSigningConfig") {
        doLast {
            check(signingPropertiesFile.isFile) { "Release signing requires root keystore.properties." }
            check(signingStoreFile?.isFile == true) { "Release signing storeFile is missing or invalid." }
            check(signingError == null) { signingError!! }
        }
    }
    tasks.matching { it.name == "validateSigningRelease" || it.name == "signReleaseBundle" }
        .configureEach { dependsOn(verifyReleaseSigningConfig) }

    defaultConfig {

        applicationId = "com.eyeoverlay.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    // Explicitly separate the sellable app from retained research prototypes.
    sourceSets {
        getByName("main") {
            java.directories.clear()
            kotlin.directories.clear()
            kotlin.directories.addAll(listOf(
                "src/production/kotlin",
                "src/main/java/com/example/myapplication/ai",
                "src/main/java/com/example/myapplication/data",
                "src/main/java/com/example/myapplication/reminder",
                "src/main/java/com/example/myapplication/ui/theme"
            ))
            assets.directories.clear()
        }
        getByName("debug") {
            kotlin.directories.addAll(listOf(
                "src/main/java/com/example/myapplication/service",
                "src/main/java/com/example/myapplication/vision"
            ))
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                // Optimize the complete app; avoid experimental cross-package partial optimization.
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Verify 64-bit ELF LOAD alignment in both installable APKs and the submitted AAB.
abstract class VerifyNativeAlignment : DefaultTask() {
    @get:InputFiles
    abstract val archives: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val packages = archives.asFileTree.matching { include("**/*.apk", "**/*.aab") }.files
        check(packages.isNotEmpty()) { "No APK/AAB available for native alignment verification" }
        packages.sortedBy { it.name }.forEach { archive ->
            ZipFile(archive).use { zip ->
                val libraries = zip.entries().asSequence().filter {
                    !it.isDirectory && it.name.endsWith(".so") &&
                        (it.name.contains("lib/arm64-v8a/") || it.name.contains("lib/x86_64/"))
                }.toList()
                libraries.forEach { entry ->
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    check(bytes.size >= 64 && bytes[0] == 0x7f.toByte() &&
                        bytes[1] == 'E'.code.toByte() && bytes[2] == 'L'.code.toByte() &&
                        bytes[3] == 'F'.code.toByte() && bytes[4] == 2.toByte()) { "Invalid ELF64: ${entry.name}" }
                    val order = when (bytes[5].toInt()) {
                        1 -> ByteOrder.LITTLE_ENDIAN
                        2 -> ByteOrder.BIG_ENDIAN
                        else -> error("Invalid ELF byte order: ${entry.name}")
                    }
                    val buffer = ByteBuffer.wrap(bytes).order(order)
                    val headerOffset = buffer.getLong(32)
                    val entrySize = buffer.getShort(54).toInt() and 0xffff
                    val count = buffer.getShort(56).toInt() and 0xffff
                    check(entrySize >= 56 && headerOffset >= 64 &&
                        headerOffset + entrySize.toLong() * count <= bytes.size.toLong()) { "Invalid ELF headers: ${entry.name}" }
                    var loadSegments = 0
                    repeat(count) { index ->
                        val offset = (headerOffset + entrySize.toLong() * index).toInt()
                        if (buffer.getInt(offset) == 1) {
                            loadSegments++
                            val fileOffset = buffer.getLong(offset + 8)
                            val address = buffer.getLong(offset + 16)
                            val alignment = buffer.getLong(offset + 48)
                            check(alignment >= 16384L && (address - fileOffset) % 16384L == 0L) {
                                "16 KB ELF LOAD alignment failed: ${archive.name}/${entry.name}, align=$alignment"
                            }
                        }
                    }
                    check(loadSegments > 0) { "No ELF LOAD segments: ${entry.name}" }
                    println("16 KB ELF verified: ${archive.name}/${entry.name}")
                }
                println("Native ELF check passed: ${archive.name}, ${libraries.size} 64-bit libraries")
            }
        }
    }
}

androidComponents.onVariants { variant ->
    val suffix = variant.name.replaceFirstChar { it.uppercaseChar() }
    tasks.register<VerifyNativeAlignment>("verify${suffix}NativeAlignment") {
        group = "verification"
        archives.from(variant.artifacts.get(SingleArtifact.APK))
    }
    if (variant.buildType == "release") {
        tasks.register<VerifyNativeAlignment>("verifyReleaseBundleNativeAlignment") {
            group = "verification"
            archives.from(variant.artifacts.get(SingleArtifact.BUNDLE))
        }
    }
}

// Inputs are serializable so the release guard also works with configuration caching.
abstract class VerifyReleaseBoundary : DefaultTask() {
    @get:InputFile
    abstract val manifestFile: RegularFileProperty
    @get:Input
    abstract val runtimeModules: ListProperty<String>

    @TaskAction
    fun verify() {
        val document = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }.newDocumentBuilder().parse(manifestFile.get().asFile)
        val permissions = document.getElementsByTagName("uses-permission")
        val permissionNames = (0 until permissions.length).map {
            permissions.item(it).attributes.getNamedItem("android:name").nodeValue
        }
        val forbidden = setOf(
            "android.permission.CAMERA",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.FOREGROUND_SERVICE_CAMERA",
            "android.permission.REQUEST_INSTALL_PACKAGES"
        )
        check(permissionNames.none { it in forbidden }) { "Forbidden research permission in release build: $permissionNames" }
        val services = document.getElementsByTagName("service")
        val serviceNames = (0 until services.length).map {
            services.item(it).attributes.getNamedItem("android:name")?.nodeValue ?: ""
        }
        check(serviceNames.none { it.contains("EyeExerciseOverlayService") }) { "Debug overlay service found in release build: $serviceNames" }
        check(runtimeModules.get().none {
            it.startsWith("com.google.mlkit:face-detection") ||
                it.startsWith("androidx.camera:") || it.startsWith("com.google.mediapipe:")
        }) { "Camera/Face-detection research vision SDK found in release build" }
        println("Release boundary verified. Permissions: $permissionNames; camera/ML SDKs excluded.")
    }
}

androidComponents.onVariants(androidComponents.selector().withBuildType("release")) { variant ->
    val modules = configurations.getByName("releaseRuntimeClasspath").incoming.artifacts.resolvedArtifacts.map {
        artifacts -> artifacts.map { it.id.componentIdentifier.displayName }
    }
    val verifyTask = tasks.register<VerifyReleaseBoundary>("verifyReleaseBoundary") {
        group = "verification"
        manifestFile.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
        runtimeModules.set(modules)
    }
    tasks.matching { (it.name.startsWith("package") || it.name.startsWith("bundle")) && it.name.contains("Release") }.configureEach {
        dependsOn(verifyTask)
    }
}

dependencies {
    // Android 기본 및 UI 라이브러리
    implementation(libs.androidx.core.ktx)

    // On-device Gemini Nano through Android AICore (no cloud API key)
    implementation("com.google.mlkit:genai-prompt:1.0.0-beta4")

    // 1. Google MediaPipe Vision & ML Kit Face Detection & On-Device GenAI Prompt API (Gemini Nano)
    debugImplementation("com.google.mlkit:face-detection:16.1.7")

    // 2. CameraX (카메라 프레임을 실시간으로 AI에 전달하기 위한 도구)
    val cameraxVersion = "1.6.2"
    debugImplementation("androidx.camera:camera-core:$cameraxVersion")
    debugImplementation("androidx.camera:camera-camera2:$cameraxVersion")
    debugImplementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    debugImplementation("androidx.camera:camera-view:$cameraxVersion")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    // Upgrade the native path library independently of the Compose BOM.
    implementation("androidx.graphics:graphics-path:1.1.0")
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.lifecycleRuntimeKtx.get()}")
    debugImplementation("androidx.lifecycle:lifecycle-service:2.8.7")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // AndroidX Test tracing references this annotation when shrinking release tests.
    androidTestImplementation("com.google.errorprone:error_prone_annotations:2.28.0")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}