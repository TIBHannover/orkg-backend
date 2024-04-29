package org.orkg.contenttypes.input.testing.fixtures

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.NumberLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.OtherLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.ResourcePropertyRequest
import org.orkg.contenttypes.adapter.input.rest.StringLiteralPropertyRequest
import org.orkg.contenttypes.adapter.input.rest.UntypedPropertyRequest
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates

fun untypedTemplatePropertyRequest() =
    UntypedPropertyRequest(
        label = "property label",
        placeholder = "property placeholder",
        description = "property description",
        minCount = 1,
        maxCount = 2,
        path = Predicates.field
    )

fun stringLiteralTemplatePropertyRequest() =
    StringLiteralPropertyRequest(
        label = "string literal property label",
        placeholder = "string literal property placeholder",
        description = "string literal property description",
        minCount = 1,
        maxCount = 2,
        pattern = """\d+""",
        path = Predicates.field,
        datatype = Classes.string,
    )

fun numberLiteralTemplatePropertyRequest() =
    NumberLiteralPropertyRequest(
        label = "number literal property label",
        placeholder = "number literal property placeholder",
        description = "number literal property description",
        minCount = 1,
        maxCount = 2,
        minInclusive = 5,
        maxInclusive = 10,
        path = Predicates.field,
        datatype = Classes.integer,
    )

fun otherLiteralTemplatePropertyRequest() =
    OtherLiteralPropertyRequest(
        label = "literal property label",
        placeholder = "literal property placeholder",
        description = "literal property description",
        minCount = 1,
        maxCount = 2,
        path = Predicates.field,
        datatype = ThingId("C25"),
    )

fun resourceTemplatePropertyRequest() =
    ResourcePropertyRequest(
        label = "resource property label",
        placeholder = "resource property placeholder",
        description = "resource property description",
        minCount = 3,
        maxCount = 4,
        path = Predicates.hasAuthor,
        `class` = ThingId("C28"),
    )
