plugins {
    id("org.orkg.gradle.kotlin")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api(project(":statistics:statistics-core-model"))
}
