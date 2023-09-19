plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    compileOnly(project(":feature-flags:feature-flags-ports"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(libs.bundles.jaxb)
}
