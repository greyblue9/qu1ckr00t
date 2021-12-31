import java.util.Properties

plugins {
    id("com.android.application")
}

val buildMinVersion: Int by extra
val buildTargetVersion: Int by extra

val buildVersionCode: Int by extra
val buildVersionName: String by extra

android {
    compileSdkVersion(buildTargetVersion)

    defaultConfig {
        applicationId = "de.hernan.qu1ckr00t"

        minSdk = buildMinVersion
        targetSdk = buildTargetVersion

        versionCode = buildVersionCode
        versionName = buildVersionName
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            signingConfig = signingConfigs.create("release")
        }
    }

    ndkVersion = "23.0.7123448"

    signingConfigs {
        named("release") {
            val properties = Properties().apply {
                rootProject.file("keystore.properties").inputStream().use(this::load)
            }

            storeFile(rootProject.file(properties.getProperty("storeFile") ?: error("keystore.properties invalid")))
            storePassword(properties.getProperty("storePassword") ?: error("keystore.properties invalid"))
            keyAlias(properties.getProperty("keyAlias") ?: error("keystore.properties invalid"))
            keyPassword(properties.getProperty("keyPassword") ?: error("keystore.properties invalid"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":shared"))

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("com.android.tools.build:gradle:4.2.1")
    implementation("org.javassist:javassist:3.27.0-GA")
    // implementation("dev.rikka.ndk:riru:25.0.1")
}

repositories {
    mavenCentral()
}
