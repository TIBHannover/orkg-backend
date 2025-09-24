// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":data-import:data-import-core-model"))
    api(project(":data-import:data-import-ports-output"))
    api(project(":graph:graph-core-model"))
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation(project(":common:pagination"))
    implementation(project(":common:serialization"))
    implementation(project(":common:string-utils"))
    implementation(project(":content-types:content-types-core-model"))
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                compileOnly("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-orm")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation(testFixtures(project(":data-import:data-import-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly(project(":migrations:liquibase"))
            }
        }
    }
}
