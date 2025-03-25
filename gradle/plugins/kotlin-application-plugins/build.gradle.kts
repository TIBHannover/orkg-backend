plugins {
    `kotlin-dsl`
    groovy
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation(project(":kotlin-library-plugins"))
    //implementation("org.orkg.gradle:build-parameters-plugins")
    //implementation("org.owasp:dependency-check-gradle")
    //implementation("io.fuchs.gradle.classpath-collision-detector:classpath-collision-detector")

    implementation("org.springframework.boot:spring-boot-gradle-plugin")
    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("com.google.cloud.tools:jib-gradle-plugin")
}

gradlePlugin {
    plugins {
        register("print-coverage") {
            id = "org.orkg.gradle.print-coverage"
            implementationClass = "org.orkg.gradle.plugins.PrintCoveragePlugin"
        }
    }
}
