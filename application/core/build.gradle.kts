plugins {
    id("org.orkg.kotlin-conventions")

    // FIXME: work-around for Spring stuff. Remove when refactored.
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    api(platform(project(":platform")))
    // FIXME: work-around for Spring stuff. Remove when refactored.
    // This whole ceremony is so that the Spring plugin does not fiddle with the jar. (It will break kapt.)
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
    implementation("org.springframework:spring-web")
}
