plugins {
    id("java-platform")
}

dependencies.constraints {
    val kotlinVersion = "1.9.22"
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")

    // FIXME: This version should be the same as the one used in the platform.
    //        It was moved ahead to work around a bug in the Spring Gradle plugin that lead to rebuilds which were
    //        unnecessary.
    api("org.springframework.boot:spring-boot-gradle-plugin:3.0.13")

    val asciidoctorVersion = "3.3.2"
    api("org.asciidoctor:asciidoctor-gradle-jvm:$asciidoctorVersion")
    api("org.asciidoctor:asciidoctor-gradle-jvm-gems:$asciidoctorVersion")

    api("dev.jacomet.gradle.plugins:logging-capabilities:0.11.1")
    api("org.gradlex:java-ecosystem-capabilities:1.3.1")
    api("com.autonomousapps:dependency-analysis-gradle-plugin:1.28.0")
    api("com.osacky.doctor:doctor-plugin:0.10.0")

    api("com.google.cloud.tools:jib-gradle-plugin:3.4.0")
    api("com.diffplug.spotless:spotless-plugin-gradle:6.2.1")
    //api("com.diffplug.spotless:spotless-plugin-gradle:6.23.3")
    api("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle:2.4.0")
    //api("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle:3.0.2")
}
