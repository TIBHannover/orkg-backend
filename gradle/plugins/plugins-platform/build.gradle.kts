plugins {
    id("java-platform")
}

dependencies.constraints {
    val kotlinVersion = "2.1.10"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")

    // FIXME: This version should be the same as the one used in the platform.
    api("org.springframework.boot:spring-boot-gradle-plugin:3.4.0")

    val asciidoctorVersion = "4.0.3"
    // FIXME: Workaround for dependency issue, see https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/731
    // Not fixed in 4.0.2 yet, possibly in later versions.
    api("org.asciidoctor:asciidoctor-gradle-jvm:$asciidoctorVersion")
    api("org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorVersion")
    val restdocsSpecVersion = "0.19.4"
    api("com.epages.restdocs-api-spec:com.epages.restdocs-api-spec.gradle.plugin:$restdocsSpecVersion")

    api("dev.jacomet.gradle.plugins:logging-capabilities:0.11.1")
    api("org.gradlex:jvm-dependency-conflict-resolution:2.1.2")
    api("com.autonomousapps:dependency-analysis-gradle-plugin:2.8.0")
    api("com.osacky.doctor:doctor-plugin:0.10.0")
    api("com.github.ben-manes:gradle-versions-plugin:0.51.0")
    api("dev.iurysouza:modulegraph:0.10.1")

    api("com.google.cloud.tools:jib-gradle-plugin:3.4.0")
    api("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
    api("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle:3.1.2")
}
