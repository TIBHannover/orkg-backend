package org.orkg.common

import jakarta.validation.Payload
import jakarta.validation.constraints.Pattern
import org.springframework.restdocs.constraints.Constraint

val thingIdConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to "^[a-zA-Z0-9:_-]+$",
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)

internal val nullableNotBlankPatternConstraint = Constraint(
    Pattern::class.qualifiedName,
    mapOf(
        "regexp" to """\s*\S[\s\S]*""",
        "flags" to emptyArray<Pattern.Flag>(),
        "message" to "{jakarta.validation.constraints.Pattern.message}",
        "groups" to emptyArray<Class<*>>(),
        "payload" to emptyArray<Class<out Payload>>(),
    )
)
