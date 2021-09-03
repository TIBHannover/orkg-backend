plugins {
    id("org.orkg.kotlin-conventions")
    `java-test-fixtures`

    // FIXME: work-around for Spring stuff. Remove when refactored.
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":application:shared"))
    // FIXME: work-around for Spring stuff. Remove when refactored.
    // This whole ceremony is so that the Spring plugin does not fiddle with the jar. (It will break kapt.)
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")

    testImplementation("org.junit.jupiter:junit-jupiter")

    testFixturesImplementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    testFixturesImplementation("org.springframework.data:spring-data-commons") // for Page/Pageable classes
    testFixturesImplementation("org.assertj:assertj-core")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
}
