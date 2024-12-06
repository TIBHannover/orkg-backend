plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(project(":common"))
    api(project(":discussions:discussions-core-model"))

    testFixturesApi("io.kotest:kotest-runner-junit5")
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":discussions:discussions-core-model"))
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testFixturesImplementation("org.springframework:spring-beans")
    testFixturesImplementation("io.kotest:kotest-assertions-core")
}
