plugins {
    id("java-platform")
}

dependencies.constraints {
    val kotlinVersion = "2.2.10"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")

    // FIXME: This version should be the same as the one used in the platform.
    api("org.springframework.boot:spring-boot-gradle-plugin:4.0.3")

    val asciidoctorVersion = "4.0.5"
    // FIXME: Workaround for dependency issue, see https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/731
    // Not fixed in 4.0.5 yet, possibly in later versions.
    api("org.asciidoctor:asciidoctor-gradle-jvm:$asciidoctorVersion")
    api("org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorVersion")
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
