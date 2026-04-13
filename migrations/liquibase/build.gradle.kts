import org.gradle.jvm.tasks.Jar

plugins {
    id("org.orkg.gradle.kotlin")
}

tasks {
    named("jar", Jar::class.java).configure {
        archiveBaseName.set("orkg-${project.name}")
    }
}
