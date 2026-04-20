plugins {
    id("java-platform")
}

dependencies.constraints {
    val kotlinVersion = "2.3.0"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")

    // FIXME: This version should be the same as the one used in the platform.
    api("org.springframework.boot:spring-boot-gradle-plugin:4.0.3")

    api("io.spring.gradle.antora:spring-antora-plugin:0.0.1")
    api("org.antora:gradle-antora-plugin:1.0.0")
    // NOTE: This version should be the same as the one used in the antora plugin.
    api("com.github.node-gradle:gradle-node-plugin:3.5.1")
    api("org.openapitools:openapi-generator-gradle-plugin:7.20.0")

    api("org.gradlex:jvm-dependency-conflict-resolution:2.5")
    api("com.autonomousapps.dependency-analysis:com.autonomousapps.dependency-analysis.gradle.plugin:3.6.1")
    api("com.osacky.doctor:doctor-plugin:0.12.0")
    api("com.github.ben-manes:gradle-versions-plugin:0.53.0")
    api("dev.iurysouza:modulegraph:0.13.0")
    api("com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin:5.5.4")

    api("com.google.cloud.tools:jib-gradle-plugin:3.5.3")
    api("com.diffplug.spotless:spotless-plugin-gradle:8.3.0")
    api("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle:3.1.2")
}
