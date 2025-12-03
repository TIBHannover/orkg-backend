// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.github.multiformats:java-multihash")
    api("org.springframework:spring-web")
    implementation(project(":common:string-utils"))
    implementation("com.github.multiformats:java-multibase")
    implementation("dev.forkhandles:values4k")
    api(project(":common:core-identifiers"))
    api(project(":common:external-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":graph:graph-core-model"))
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind")

    testFixturesApi(project(":common:core-identifiers"))
    testFixturesApi(project(":common:external-identifiers"))
    testFixturesApi(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation("dev.forkhandles:values4k")
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesImplementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.redfin:contractual")
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":community:community-core-model"))
                implementation(testFixtures(project(":community:community-core-model")))
            }
        }
    }
}
