plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.lopo.validabuonicartecultura"
    compileSdk = 36

    defaultConfig {
        applicationId = "it.lopo.validabuonicartecultura"
        minSdk = 26
        targetSdk = 36
        // Il flusso di rilascio li passa con -PversionCode e -PversionName, ricavati dal tag.
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = (findProperty("versionName") as String?) ?: "1.0"
    }

    // La chiave di firma non sta nel repository: arriva dalle variabili d'ambiente,
    // che su GitHub sono i segreti del repository. Senza variabili la release resta non firmata.
    val chiave = System.getenv("KEYSTORE_FILE")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (chiave != null) {
            create("release") {
                storeFile = chiave
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (chiave != null) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Lettore di QR e codici a barre di Google: non richiede il permesso fotocamera.
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
}
