// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common:core-identifiers"))
    implementation(project(":common:string-utils"))
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("tools.jackson.core:jackson-databind")
    api("org.apache.tomcat.embed:tomcat-embed-core") // for HttpServletRequest
    api("org.springframework.boot:spring-boot-webmvc")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-config")
    api("org.springframework.security:spring-security-core")
    api("org.springframework.security:spring-security-oauth2-jose")
    api("org.springframework.security:spring-security-web")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-web")
    api("org.springframework:spring-webmvc")
    implementation("tools.jackson.core:jackson-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                compileOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
                compileOnly("org.jspecify:jspecify")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:core-identifiers")))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":testing:spring")))
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("org.hamcrest:hamcrest")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}
