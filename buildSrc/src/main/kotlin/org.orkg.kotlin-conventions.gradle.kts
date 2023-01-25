import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    jacoco
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
        }
    }

    // Create reproducible archives
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).apply {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}
