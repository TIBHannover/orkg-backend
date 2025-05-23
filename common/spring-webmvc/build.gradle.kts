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
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.neo4j.driver:neo4j-java-driver")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core") // for AccessDeniedException, UserDetails
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-web")
    api("org.springframework:spring-webmvc")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("org.apache.tomcat.embed:tomcat-embed-core") // for HttpServletRequest
    implementation("org.slf4j:jcl-over-slf4j") // for org.apache.commons.logging.LogFactory in ResponseEntityExceptionHandler
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // for timestamp serialization
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":testing:spring")))
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                runtimeOnly("com.jayway.jsonpath:json-path")
                compileOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
            }
        }
    }
}
