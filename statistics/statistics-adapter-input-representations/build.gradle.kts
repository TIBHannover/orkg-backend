plugins {
    id("org.orkg.gradle.kotlin")
}

dependencies {
    api(project(":statistics:statistics-core-model"))
    implementation("com.fasterxml.jackson.core:jackson-annotations")
}
