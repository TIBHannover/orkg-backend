plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common:core-identifiers"))
    api(project(":community:community-core-model"))
}
