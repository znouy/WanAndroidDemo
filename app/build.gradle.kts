plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.parcelize)// 确保引入了官方的 Kotlin Parcelize 插件（无缝自动生成模版代码）
}

android {
    namespace = "com.example.wanandroiddemo"
    compileSdk = 37

    configurations {
        all {
            exclude(group = "com.intellij", module = "annotations")
        }
    }

    defaultConfig {
        applicationId = "com.example.wanandroiddemo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true // 明确开启 BuildConfig 生成
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(libs.androidx.swiperefreshlayout)
    // Hilt 依赖注入框架
    implementation(libs.hilt.android)
    "ksp"(libs.hilt.compiler)
    // Paging 3 分页库
    implementation(libs.paging.runtime)
    // 基础核心库
    implementation(libs.androidx.core.ktx) // Kotlin 扩展库
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.appcompat) // 兼容库
    implementation(libs.material) // Material Design 组件
    implementation(libs.androidx.constraintlayout) // 约束布局


    // Navigation 组件 (单 Activity 架构核心)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)


    implementation(libs.paging.runtime)

    // Retrofit 网络请求
    implementation(libs.retrofit) // 网络请求核心
    implementation(libs.converter.moshi) // Moshi 转换器
    implementation(libs.logging.interceptor) // 网络日志拦截器

    // Moshi JSON 解析 (替代 Gson，更轻量高效)
    implementation(libs.moshi.kotlin) // Moshi Kotlin 支持
    "ksp"(libs.moshi.kotlin.codegen) // Moshi 代码生成器

    // 生命周期与协程
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel 协程支持
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle 协程支持
    implementation(libs.kotlinx.coroutines.android) // 协程支持

    // Coil 图片加载库 (轻量级，性能好)
    implementation(libs.coil)

    // 测试库
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 1. 至少需要添加 Compose 运行时（使用plugins.kotlin.compose）
    implementation(libs.androidx.compose.runtime) // 请根据你的项目版本调整版本号

    //room
    implementation(libs.androidx.room.ktx)//// 支持协程和 Flow
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    "ksp"(libs.androidx.room.compiler)// KSP 编译器

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    //timber
    implementation(libs.timber)

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // 引入 Google 官方折行布局
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
}
