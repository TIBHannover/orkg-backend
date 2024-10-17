package org.orkg.contenttypes.adapter.input.rest

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.*

fun authorListFields(type: String, path: String = "authors"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The list of authors that originally contributed to the $type."),
    fieldWithPath("$path[].id").type("String").description("The ID of the author. (optional)").optional(),
    fieldWithPath("$path[].name").description("The name of the author."),
    fieldWithPath("$path[].identifiers").type("Object").description("The unique identifiers of the author."),
    fieldWithPath("$path[].identifiers.open_alex").type("Array").description("The list of Open Alex IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.orcid").type("Array").description("The list of ORCIDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.google_scholar").type("Array").description("The list of Google Scholar IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.research_gate").type("Array").description("The list of ResearchGate IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.linked_in").type("Array").description("The list of LinkedIn IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.wikidata").type("Array").description("The list of Wikidata IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].identifiers.web_of_science").type("Array").description("The list of Web of Science IDs of the author. (optional)").optional(),
    fieldWithPath("$path[].homepage").type("String").description("The homepage of the author. (optional)").optional(),
)

fun paperIdentifierFields(path: String = "identifiers"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The unique identifiers of the paper. (optional)"),
    fieldWithPath("$path.doi").type("Array").description("The list of DOIs of the paper. (optional)").optional(),
    fieldWithPath("$path.isbn").type("Array").description("The list of ISBNs of the paper. (optional)").optional(),
    fieldWithPath("$path.issn").type("Array").description("The list of ISSNs of the paper. (optional)").optional(),
    fieldWithPath("$path.open_alex").type("Array").description("The list of OpenAlex IDs of the paper. (optional)").optional(),
)

fun publicationInfoFields(type: String, path: String = "publication_info"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The publication info of the paper.").optional(),
    fieldWithPath("$path.published_month").description("The month in which the $type was published. (optional)").optional(),
    fieldWithPath("$path.published_year").description("The year in which the $type was published. (optional)").optional(),
    fieldWithPath("$path.published_in").description("The venue where the $type was published. (optional)").optional(),
    fieldWithPath("$path.published_in.id").description("The ID of the venue."),
    fieldWithPath("$path.published_in.label").description("The label of the venue."),
    fieldWithPath("$path.url").description("The URL to the original $type. (optional)").optional(),
)

fun sustainableDevelopmentGoalsFields(type: String, path: String = "sdgs"): List<FieldDescriptor> = listOf(
    fieldWithPath(path).description("The list of sustainable development goals that the $type belongs to."),
    fieldWithPath("$path[].id").description("The ID of the sustainable development goal."),
    fieldWithPath("$path[].label").description("The label of the sustainable development goal."),
)
