// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spotless)
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation(project(":content-types:content-types-core-model")) // TODO: move to correct module
    implementation(project(":community:community-core-model"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":community:community-ports-output"))
    implementation(project(":community:community-adapter-output-spring-data-jpa")) // TODO: break dependency
    implementation(project(":identity-management:idm-ports-input"))
    implementation(project(":identity-management:idm-ports-output"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":identity-management:idm-core-services"))
    implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // for JpaUserRepository in observatories

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-core") // Spring MimeType
    implementation(libs.javax.activation)
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Disable JUnit 4 (aka Vintage)
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }

    testApi(enforcedPlatform(libs.junit5.bom))
    testImplementation(project(mapOf("path" to ":content-types:content-types-core-services"))) // TODO: Remove after upgrade
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":identity-management:idm-core-model")))
                implementation(project(":media-storage:media-storage-core-model"))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation(project(":media-storage:media-storage-ports-input"))
                implementation(libs.spring.mockk)
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            }
        }
    }
}
