// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":common:datatypes")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation(project(":common:external-identifiers"))
                implementation(project(":common:serialization"))
                implementation(project(":content-types:content-types-core-model"))
                implementation(project(":graph:graph-core-constants"))
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("org.assertj:assertj-core")
                implementation("com.ninja-squad:springmockk")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("jakarta.validation:jakarta.validation-api")
    api("org.apache.tomcat.embed:tomcat-embed-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-adapter-input-representations"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output")) // uses repository directly
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-input"))
    implementation("com.github.multiformats:java-multibase")
    implementation("com.github.multiformats:java-multihash")
    implementation("org.springframework:spring-core")
    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi(project(":community:community-adapter-input-rest-spring-mvc"))
    testFixturesApi(project(":common:serialization"))
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesImplementation(testFixtures(project(":community:community-core-model")))
}
