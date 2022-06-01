import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.6.10"
    val springBootVersion = "2.3.4.RELEASE"
    // Kotlin conventions
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    // Spring conventions
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion") {
        because("""contains "kotlin.spring" plug-in""")
    }
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion") {
        because("""contains "kotlin.jpa" plug-in""")
    }
    api("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
