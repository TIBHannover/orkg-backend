plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":common"))

    api("com.fasterxml.jackson.core:jackson-core")
    api("javax.validation:validation-api")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.kotlin)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation(libs.kotest.runner)
            }
        }
    }
}
