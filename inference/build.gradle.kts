plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "hu.toliver.vinet.inference"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core-model"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.litert)
    testImplementation(libs.junit)
    testImplementation(libs.bytedeco.javacpp)
    testImplementation(libs.bytedeco.tensorflow.lite)
    testImplementation("org.bytedeco:tensorflow-lite:${libs.versions.tfliteBytedeco.get()}:linux-x86_64")
}
