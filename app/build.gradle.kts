plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")  // Плагин Google Services для Firebase
}

android {
    namespace = "com.example.avto"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.avto"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Добавьте если используете viewBinding или dataBinding
    buildFeatures {
        viewBinding = true
        // dataBinding = true  // раскомментируйте если нужно
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Firebase BoM (Bill of Materials) - автоматически управляет версиями
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase библиотеки (оставьте только нужные)
    implementation("com.google.firebase:firebase-analytics-ktx")      // Аналитика
    implementation("com.google.firebase:firebase-storage-ktx")        // Хранилище
    implementation("com.google.firebase:firebase-auth-ktx")           // Аутентификация
    implementation("com.google.firebase:firebase-firestore-ktx")      // Firestore
    // implementation("com.google.firebase:firebase-database-ktx")    // Realtime DB (если нужно)
    // implementation("com.google.firebase:firebase-messaging-ktx")   // Cloud Messaging

    // Picasso для загрузки изображений
    implementation("com.squareup.picasso:picasso:2.8")

    // Room для базы данных
   // implementation("androidx.room:room-runtime:2.6.0")
    //annotationProcessor("androidx.room:room-compiler:2.6.0")
    // Для Kotlin используйте kapt вместо annotationProcessor:
    // kapt("androidx.room:room-compiler:2.6.0")

    // Gson для сериализации
    implementation("com.google.code.gson:gson:2.10.1")

    // Lifecycle компоненты (рекомендуется добавить)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Coroutines для асинхронных операций
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    // Navigation Component (если нужно)
    // implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    // implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}