plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.5.21"
    val springBootVersion = "2.5.3"
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
