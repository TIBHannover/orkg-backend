plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesImplementation("io.mockk:mockk-dsl")
    testFixturesImplementation("io.mockk:mockk-jvm")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
}
