package org.orkg.graph.adapter.input.rest.testing.fixtures

import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import org.springframework.restdocs.constraints.Constraint

val statementIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to "^S[a-zA-Z0-9:_-]+$",
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)
