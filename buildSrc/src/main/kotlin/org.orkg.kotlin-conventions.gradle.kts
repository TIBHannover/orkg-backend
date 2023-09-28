plugins {
    kotlin("jvm")
    jacoco
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "ch.qos.logback") {
            useVersion("1.3.11")
            because("must be compatible with slf4j-api 2.x")
        }
    }
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
