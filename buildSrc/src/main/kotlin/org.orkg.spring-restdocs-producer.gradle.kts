// Add consumable configuration for RestDocs snippets
val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

dependencies {
    // Add all generated Asciidoc files/snippets to the configuration, so that they can be aggregated later.
    restdocs(fileTree(layout.buildDirectory.dir("generated-snippets")) {
        builtBy("test")
        include("**/*.adoc")
    })
}
