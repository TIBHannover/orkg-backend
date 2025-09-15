plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesApi("io.kotest:kotest-framework-engine-jvm")
    testFixturesImplementation("org.apache.commons:commons-lang3")
}
