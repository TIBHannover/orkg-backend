plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":data-import:data-import-core-model"))
    implementation("io.kotest:kotest-assertions-api")
    implementation("io.kotest:kotest-assertions-shared")
    implementation(project(":common:core-identifiers"))
    implementation(project(":graph:graph-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":graph:graph-core-constants"))
                implementation(testFixtures(project(":data-import:data-import-core-model")))
            }
        }
    }
}
