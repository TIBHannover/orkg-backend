package org.orkg.testing.spring.restdocs.snippets

import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.snippet.TemplatedSnippet

class DescriptionSnippet private constructor(
    private val description: String,
) : TemplatedSnippet("description", null) {
    protected override fun createModel(operation: Operation): MutableMap<String, Any> =
        mutableMapOf("description" to description)

    companion object {
        fun description(description: String) = DescriptionSnippet(description)
    }
}
