plugins {
    id("org.orkg.gradle.kotlin-library")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-tx")
    api("org.jbibtex:jbibtex")
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input")) // used in LegacyPaperService
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation("dev.forkhandles:values4k")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-runner-junit5")
                implementation(project(":content-types:content-types-core-services"))
                implementation(project(":media-storage:media-storage-core-model"))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":content-types:content-types-ports-input")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
