plugins {
    id("com.android.test")
}

android {
    namespace = "com.eyeoverlay.releasevalidation"
    compileSdk = 36
    defaultConfig {
        minSdk = 33
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Keep the real 20-minute observation opt-in via the class runner argument.
        testInstrumentationRunnerArguments["class"] = "com.eyeoverlay.releasevalidation.ProductionUiTest"
    }
    targetProjectPath = ":app"
    // Keep the runner and its libraries in their own process/APK.
    experimentalProperties["android.experimental.self-instrumenting"] = true
    buildTypes {
        create("productionCheck") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += "release"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

androidComponents.beforeVariants {
    it.enable = it.buildType == "productionCheck"
}

dependencies {
    implementation("androidx.test:runner:1.7.0")
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
