import org.gradle.jvm.tasks.Jar

plugins {
    id("java-library")
}

// Add consumable configuration for Liquibase configuration
val liquibase: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("liquibase", tasks.jar)
}

tasks {
    named("jar", Jar::class.java).configure {
        archiveBaseName.set("orkg-${project.name}")
    }
}
