package org.orkg.contenttypes.adapter.input.rest

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.*

fun authorListFields(type: String, path: String = "authors"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The list of authors that originally contributed to the $type."),
    fieldWithPath("$path[].id").description("The ID of the author. (optional)").optional(),
    fieldWithPath("$path[].name").description("The name of the author."),
    fieldWithPath("$path[].identifiers").description("The unique identifiers of the author."),
    fieldWithPath("$path[].identifiers.orcid").type("Array").description("The list ORCIDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.google_scholar").type("Array").description("The list of Google Scholar IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.research_gate").type("Array").description("The list of ResearchGate IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.linked_in").type("Array").description("The list of LinkedIn IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.wikidata").type("Array").description("The list of Wikidata IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.web_of_science").type("Array").description("The list of Web of Science IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].homepage").description("The homepage of the author. (optional)").optional(),
)
