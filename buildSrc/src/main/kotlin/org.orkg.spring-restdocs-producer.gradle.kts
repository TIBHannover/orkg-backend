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
        }
        includeEmptyDirs = false
    }

    val test by getting {
        outputs.dir(restdocsSnippetsDir).withPropertyName("restdocsSnippetsDirectory")
        finalizedBy(tasks.named("restdocsSnippetsZip"))
    }
}

artifacts {
    add("restdocs", tasks.named("restdocsSnippetsZip"))
}
