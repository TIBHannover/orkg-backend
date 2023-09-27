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

    // Create reproducible archives
    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

val jacocoTestReport by tasks.getting(JacocoReport::class) {
    reports {
        xml.required.set(true)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

/*
java {
    consistentResolution {
        useCompileClasspathVersions()
    }
}
*/
