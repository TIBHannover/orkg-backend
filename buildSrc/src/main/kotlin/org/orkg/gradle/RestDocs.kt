package org.orkg.gradle

fun withSnippets(path: String): Map<String, String> = mapOf("path" to path, "configuration" to "restdocs")
