import org.gradle.jvm.tasks.Jar

plugins {
    id("java-library")
}

tasks {
    named("jar", Jar::class.java).configure {
        archiveBaseName.set("orkg-${project.name}")
    }
}
