// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation("org.apache.tomcat.embed:tomcat-embed-core") // for HttpServletRequest
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.data:spring-data-neo4j") // for UncategorizedNeo4jException
    implementation("org.springframework.security:spring-security-core") // for AccessDeniedException
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webmvc")
}
