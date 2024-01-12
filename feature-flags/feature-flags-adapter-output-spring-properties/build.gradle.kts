plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    compileOnly(project(":feature-flags:feature-flags-ports"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(libs.bundles.jaxb)
}
