plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "sp.xray.lite"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar","*.jar"))))
    api(project(":libv2ray"))
    testImplementation("junit:junit:4.13.2")

    // Androidx
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.legacy:legacy-support-v4:1.0.0")
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.9.0")
    api("androidx.cardview:cardview:1.0.0")
    api("androidx.preference:preference-ktx:1.2.0")
    api("androidx.recyclerview:recyclerview:1.3.0")
    api("androidx.fragment:fragment-ktx:1.5.7")
    api("androidx.multidex:multidex:2.0.1")
    api("androidx.viewpager2:viewpager2:1.1.0-beta01")

    // Androidx ktx
    api("androidx.activity:activity-ktx:1.7.1")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    //kotlin
    api("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    api("com.tencent:mmkv-static:1.2.15")
    api("com.google.code.gson:gson:2.10.1")
    api("io.reactivex:rxjava:1.3.8")
    api("io.reactivex:rxandroid:1.2.1")
    api("com.tbruyelle.rxpermissions:rxpermissions:0.9.4@aar")
    api("com.github.jorgecastilloprz:fabprogresscircle:1.01@aar")
    api("me.drakeet.support:toastcompat:1.1.0")
    api("com.blacksquircle.ui:editorkit:2.8.0")
    api("com.blacksquircle.ui:language-base:2.8.0")
    api("com.blacksquircle.ui:language-json:2.8.0")
    api("io.github.g00fy2.quickie:quickie-bundled:1.6.0")
    api("com.google.zxing:core:3.5.1")

    api("androidx.work:work-runtime-ktx:2.8.1")
    api("androidx.work:work-multiprocess:2.8.1")
}
