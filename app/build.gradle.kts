plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Asegúrate de que este plugin esté incluido si usas Google Services.
}

android {
    namespace = "com.sistema.conexionasqlserver"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sistema.conexionasqlserver"
        minSdk = 24
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
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.google.play.services.location)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.sourceforge.jtds)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    // Agregar dependencias para ViewModel y LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")  // Usa la versión correspondiente
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
