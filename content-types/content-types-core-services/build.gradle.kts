@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.jbibtex:jbibtex")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api(project(":common:functional"))
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input")) // used in LegacyPaperService
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    api(project(":statistics:statistics-core-model"))
    implementation("dev.forkhandles:values4k")
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation("org.springframework:spring-web")
    implementation(project(":common:datatypes"))
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:pagination"))
    implementation(project(":graph:graph-core-constants"))
    testFixturesApi(project(":common:core-identifiers"))
    testFixturesApi(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.junit.platform:junit-platform-commons")
                implementation("org.opentest4j:opentest4j")
                implementation(project(":content-types:content-types-core-services"))
                implementation(project(":media-storage:media-storage-core-model"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":content-types:content-types-ports-input")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
