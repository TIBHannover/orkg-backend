plugins {
    id("base")
    id("org.orkg.gradle.dependency-rules")
}

group = "org.orkg"

// Set the version from 'version.txt'
version = providers.fileContents(rootProject.layout.projectDirectory.file("version.txt")).asText.map(String::trim).getOrElse("SNAPSHOT")
