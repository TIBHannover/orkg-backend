@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
}

java {
    // Configure runtime environment explicitly, otherwise the Compose Gradle plugin fails.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.assertj:assertj-core")
            }
        }
    }
}

dependencies {
    api(platform("org.orkg:platform"))
    api("org.junit.jupiter:junit-jupiter-api")
    api("org.orkg:world")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("it.skrape:skrapeit:1.2.2")
    implementation("org.keycloak:keycloak-admin-client")
    implementation("org.keycloak:keycloak-client-common-synced")
}
