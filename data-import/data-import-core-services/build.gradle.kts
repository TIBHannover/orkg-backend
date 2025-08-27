plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework.batch:spring-batch-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-tx")
    api(project(":common:core-identifiers"))
    api(project(":community:community-ports-output"))
    api(project(":data-import:data-import-core-model"))
    api(project(":data-import:data-import-ports-input"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.kotest:kotest-assertions-api")
    implementation("io.kotest:kotest-assertions-shared")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.batch:spring-batch-infrastructure")
    implementation(project(":community:community-core-model"))
    implementation(project(":graph:graph-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(project(":graph:graph-core-constants"))
                implementation(testFixtures(project(":data-import:data-import-core-model")))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
