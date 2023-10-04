plugins {
    kotlin("jvm")
    jacoco
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.slf4j") {
            useVersion("1.7.36")
            because("we have different versions in the classpath, but this one is used in Spring Boot <= 3.0")
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
