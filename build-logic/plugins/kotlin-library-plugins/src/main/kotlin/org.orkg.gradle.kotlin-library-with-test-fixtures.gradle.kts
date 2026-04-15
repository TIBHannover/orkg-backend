plugins {
    id("org.orkg.gradle.kotlin-library")
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(platform("org.orkg:platform"))
}
