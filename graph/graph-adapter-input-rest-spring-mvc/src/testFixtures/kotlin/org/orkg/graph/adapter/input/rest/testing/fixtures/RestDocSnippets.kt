package org.orkg.graph.adapter.input.rest.testing.fixtures

import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.testing.spring.restdocs.deprecated
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath

fun classResponseFields(): List<FieldDescriptor> = listOf(
    fieldWithPath("id").description("The class ID").optional(),
    fieldWithPath("label").description("The class label"),
    fieldWithPath("uri").type("URI").description("An optional URI to describe the class (RDF)").optional(),
    fieldWithPath("created_at").description("The class creation datetime"),
    fieldWithPath("created_by").description("The ID of the user that created the class. All zeros if unknown."),
    fieldWithPath("description").type("String").description("The description of the class, if exists.").optional(),
    fieldWithPath("_class").optional().ignored(),
    fieldWithPath("modifiable").description("Whether this class can be modified."),
)

fun resourceResponseFields(): List<FieldDescriptor> = listOf(
    fieldWithPath("id").description("The identifier of the resource."),
    fieldWithPath("label").description("The label of the resource. It is intended to be read by humans and should be used for displaying the resource."),
    fieldWithPath("formatted_label").type("String").description("The formatted label of the resource. See <<content-negotiation,Content Negotiation>> for information on how to obtain this value.").ignored(),
    fieldWithPath("classes").description("The set of classes of which this resources is an instance of."),
    fieldWithPath("shared").description("The number of statements that have this resource in their object position."),
    fieldWithPath("featured").description("Determine if the resource is featured. Defaults to `false`.").deprecated("visibility"),
    fieldWithPath("unlisted").description("Determine if the resource is unlisted. Defaults to `false`.").deprecated("visibility"),
    fieldWithPath("visibility").description("""Determines the visibility of the resource. Can be one of $allowedVisibilityValues."""),
    fieldWithPath("verified").description("Determine if the resource is verified. Defaults to `false`."),
    fieldWithPath("extraction_method").description("Determines how the resource was created. Can be one of $allowedExtractionMethodValues."),
    fieldWithPath("observatory_id").description("The UUID of the observatory to which this resource belongs."),
    fieldWithPath("organization_id").description("The UUID of the organization to which this resource belongs."),
    timestampFieldWithPath("created_at", "the resource was created"),
    fieldWithPath("created_by").description("The UUID of the user or service who created this resource."),
    fieldWithPath("modifiable").description("Whether this resource can be modified."),
    fieldWithPath("_class").description("An indicator which type of entity was returned. Always has the value `resource`."),
)

fun predicateResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the predicate."),
    fieldWithPath("label").description("The label of the predicate."),
    timestampFieldWithPath("created_at", "the predicate was created"),
    fieldWithPath("created_by").description("The UUID of the user or service who created this predicate."),
    fieldWithPath("description").type("String").description("The description of the predicate, if exists."),
    fieldWithPath("modifiable").description("Whether this predicate can be modified."),
    fieldWithPath("_class").description("An indicator which type of entity was returned. Always has the value `predicate`."),
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
