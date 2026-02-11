plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    testFixturesImplementation(project(":constants"))
    testFixturesApi("com.epages:restdocs-api-spec")
    testFixturesApi("com.github.dasniko:testcontainers-keycloak")
    testFixturesApi("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
    testFixturesApi("io.kotest:kotest-framework-engine-jvm")
    testFixturesApi("org.apache.tomcat.embed:tomcat-embed-core") // for HttpServletRequest
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi("org.neo4j.driver:neo4j-java-driver")
    testFixturesApi("org.springframework.batch:spring-batch-core")
    testFixturesApi("org.springframework.boot:spring-boot-autoconfigure")
    testFixturesApi("org.springframework.boot:spring-boot-data-jpa-test")
    testFixturesApi("org.springframework.boot:spring-boot-data-neo4j-test")
    testFixturesApi("org.springframework.boot:spring-boot-jdbc-test")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.boot:spring-boot-transaction")
    testFixturesApi("org.springframework.boot:spring-boot-webmvc-test")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("org.springframework.security:spring-security-core")
    testFixturesApi("org.springframework.security:spring-security-oauth2-jose")
    testFixturesApi("org.springframework.security:spring-security-test")
    testFixturesApi("org.springframework:spring-beans")
    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework:spring-core")
    testFixturesApi("org.springframework:spring-test")
    testFixturesApi("org.springframework:spring-tx")
    testFixturesApi("org.springframework:spring-web")
    testFixturesApi("org.springframework.data:spring-data-commons")
    testFixturesApi("org.springframework.data:spring-data-neo4j")
    testFixturesApi("com.fasterxml.jackson.core:jackson-databind")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesApi("org.testcontainers:testcontainers-neo4j")
    testFixturesApi("org.testcontainers:testcontainers-postgresql")
    testFixturesApi("org.testcontainers:testcontainers")
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":common:testing")))
    testFixturesImplementation("com.ninja-squad:springmockk")
    testFixturesImplementation("io.kotest:kotest-extensions-spring")
    testFixturesImplementation("io.mockk:mockk-dsl")
    testFixturesImplementation("io.mockk:mockk-jvm")
    testFixturesImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testFixturesImplementation("org.hamcrest:hamcrest")
    testFixturesImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testFixturesImplementation("org.springframework.security:spring-security-oauth2-core")
    testFixturesImplementation("org.springframework.security:spring-security-oauth2-resource-server")
    testFixturesImplementation("org.springframework:spring-orm")
    testFixturesImplementation("io.jsonwebtoken:jjwt-api")
    testFixturesImplementation("jakarta.validation:jakarta.validation-api")
    testFixturesImplementation(project(":common:string-utils"))
    testFixturesRuntimeOnly("io.jsonwebtoken:jjwt-impl")
    testFixturesRuntimeOnly("io.jsonwebtoken:jjwt-jackson")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.junit.jupiter:junit-jupiter-params")
            }
        }
    }
}
