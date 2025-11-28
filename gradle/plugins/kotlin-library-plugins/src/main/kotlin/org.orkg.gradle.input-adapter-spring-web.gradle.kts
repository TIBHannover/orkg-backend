plugins {
    id("org.orkg.gradle.spring-library")
}

val restdocsSnippetsDir = layout.buildDirectory.dir("generated-snippets")

// Add consumable configuration for RestDocs snippets
val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

tasks {
    register<Zip>("restdocsSnippetsZip") {
        archiveClassifier.set("restdocs")
        from(tasks.named("test").get().outputs) {
            include("**/*.adoc")
            include("**/resource.json") // for OpenAPI documentation
            include("**/exceptions.json") // for OpenAPI documentation
        }
        includeEmptyDirs = false
    }

    tasks.named<Test>("test") {
        doFirst {
            restdocsSnippetsDir.get().asFile.deleteRecursively()
        }
        outputs.dir(restdocsSnippetsDir).withPropertyName("restdocsSnippetsDirectory")
        finalizedBy(tasks.named("restdocsSnippetsZip"))
    }
}

artifacts {
    add("restdocs", tasks.named("restdocsSnippetsZip"))
}
