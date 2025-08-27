plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework.batch:spring-batch-core")
    api("org.springframework.batch:spring-batch-infrastructure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-tx")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":data-import:data-import-core-model"))
    api(project(":data-import:data-import-ports-input"))
    api(project(":data-import:data-import-ports-output"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("dev.forkhandles:values4k")
    implementation("io.kotest:kotest-assertions-api")
    implementation("io.kotest:kotest-assertions-shared")
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation("org.springframework:spring-web")
    implementation("org.slf4j:slf4j-api")
    implementation(project(":common:functional"))
    implementation(project(":common:pagination"))
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:string-utils"))
    implementation(project(":community:community-core-model"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":graph:graph-core-constants"))
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
                implementation(testFixtures(project(":data-import:data-import-core-model")))
                implementation(testFixtures(project(":data-import:data-import-ports-input")))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly(testFixtures(project(":content-types:content-types-adapter-output-web"))) // for doiLookupSuccess.json
            }
        }
    }
}
