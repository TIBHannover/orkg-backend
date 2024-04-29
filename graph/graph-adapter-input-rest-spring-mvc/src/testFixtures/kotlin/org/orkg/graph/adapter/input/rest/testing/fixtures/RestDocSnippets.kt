package org.orkg.graph.adapter.input.rest.testing.fixtures

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

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
