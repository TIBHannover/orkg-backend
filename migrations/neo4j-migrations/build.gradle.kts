import org.gradle.jvm.tasks.Jar

plugins {
    id("java-library")
}

// Add consumable configuration for Liquibase configuration
val neo4jMigrations: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("neo4jMigrations", tasks.jar)
}

tasks {
    named("jar", Jar::class.java).configure {
        archiveBaseName.set("orkg-${project.name}")
    }
}
