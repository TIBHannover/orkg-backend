package org.orkg.graph.adapter.input.rest.testing.fixtures

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath

fun classResponseFields(): List<FieldDescriptor> = listOf(
    fieldWithPath("id").description("The class ID").optional(),
    fieldWithPath("label").description("The class label"),
    fieldWithPath("uri").description("An optional URI to describe the class (RDF)").optional(),
    fieldWithPath("created_at").description("The class creation datetime"),
    fieldWithPath("created_by").description("The ID of the user that created the class. All zeros if unknown."),
    fieldWithPath("description").description("The description of the class, if exists.").optional(),
    fieldWithPath("_class").optional().ignored(),
    fieldWithPath("featured").description("Featured Value").optional().ignored(),
    fieldWithPath("unlisted").description("Unlisted Value").optional().ignored(),
    fieldWithPath("modifiable").description("Whether this class can be modified."),
)

fun statementResponseFields(): List<FieldDescriptor> = listOf(
    fieldWithPath("id").description("The statement ID"),
    fieldWithPath("created_at").description("The statement creation datetime"),
    fieldWithPath("created_by").description("The ID of the user that created the statement. All zeros if unknown."),
    fieldWithPath("modifiable").description("Whether the statement can be modified."),
    subsectionWithPath("subject").type("Object").description("The representation of the subject entity of the statement. Either a <<classes-fetch,class>>, <<resources-fetch,resource>> or <<predicates-fetch,predicate>>."),
    subsectionWithPath("predicate").type("Object").description("The representation of the <<predicates-fetch,predicate>> of the statement."),
    subsectionWithPath("object").type("Object").description("The representation of the object entity of the statement. Either a <<classes-fetch,class>>, <<resources-fetch,resource>>, <<predicates-fetch,predicate>> or <<literals-fetch,literal>>."),
)
