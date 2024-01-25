plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":discussions:discussions-core-model"))

    implementation(project(":common"))

    implementation("org.springframework.data:spring-data-commons")

    testFixturesApi(libs.kotest.runner) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":discussions:discussions-core-model"))
    testFixturesImplementation("org.springframework.data:spring-data-commons")
}
