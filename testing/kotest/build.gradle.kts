plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    testFixturesApi(libs.kotest.runner)
    testFixturesImplementation("org.apache.commons:commons-lang3")
}
