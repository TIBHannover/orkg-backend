plugins {
    kotlin("jvm")
    jacoco
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))
}

tasks {
    withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }

    jacocoTestReport {
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
