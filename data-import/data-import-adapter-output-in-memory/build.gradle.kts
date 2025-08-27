plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":data-import:data-import-core-model"))
    api(project(":data-import:data-import-ports-output"))
    implementation(project(":common:pagination"))
    implementation(project(":common:string-utils"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":data-import:data-import-ports-output")))
            }
        }
    }
}
