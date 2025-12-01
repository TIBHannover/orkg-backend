package org.orkg.dataimport.testing.fixtures

import org.orkg.common.testing.fixtures.doiConstraint
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.dataimport.adapter.input.rest.TypedValueRepresentation
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.testing.asciidoc.allowedCSVStateValues
import org.orkg.dataimport.domain.testing.asciidoc.allowedEntityTypeValues
import org.orkg.dataimport.domain.testing.asciidoc.allowedJobStatusValues
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.polymorphicResponseFields
import org.orkg.testing.spring.restdocs.references
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath

fun jobStatusResponseFields() = listOf(
    fieldWithPath("job_id").description("The identifier of the job."),
    fieldWithPath("job_name").description("The name of the job."),
    subsectionWithPath("context").description("The context of the job. The actual contents depend on the type of job."),
    fieldWithPath("status").description("The status of the job. Either of $allowedJobStatusValues."),
)

fun csvJobStatusResponseFields() = listOf(
    *jobStatusResponseFields().toTypedArray(),
    fieldWithPath("context.csv_id").description("The id of the CSV. Only present when `job_name` is `${JobNames.VALIDATE_PAPER_CSV}` or `${JobNames.IMPORT_PAPER_CSV}`.").optional(),
)

fun csvResponseFields() = listOf(
    fieldWithPath("id").description("The identifier of the CSV."),
    fieldWithPath("name").description("The name of the CSV."),
    fieldWithPath("type").description("The type of the CSV. See <<csv-types,CSV Types>>."),
    fieldWithPath("format").description("The format of the CSV. See <<csv-formats,CSV Formats>>."),
    fieldWithPath("state").description("The state of the CSV. Either of $allowedCSVStateValues."),
    timestampFieldWithPath("created_at", "the CSV was created"),
    // TODO: Add links to documentation of special user UUIDs.
    fieldWithPath("created_by").description("The UUID of the user or service who created the CSV."),
)

fun paperCSVRecordResponseFields() = listOf(
    fieldWithPath("id").description("The id of the parsed paper record."),
    fieldWithPath("csv_id").description("The id of the csv where the parsed paper record originated from."),
    fieldWithPath("item_number").description("The item number of the parsed paper record."),
    fieldWithPath("line_number").description("The line number of the parsed paper record within the CSV."),
    fieldWithPath("title").description("The title of the parsed paper record."),
    fieldWithPath("published_month").description("The publication month of the parsed paper record. (optional)").optional(),
    fieldWithPath("published_year").description("The publication year of the parsed paper record. (optional)").optional(),
    fieldWithPath("published_in").description("The publication venue of the parsed paper record. (optional)").optional(),
    fieldWithPath("url").description("The url of the parsed paper record. (optional)").optional(),
    fieldWithPath("doi").description("The DOI of the parsed paper record. (optional)").constraints(doiConstraint).optional(),
    fieldWithPath("research_field_id").description("The id of the research field of the parsed paper record."),
    fieldWithPath("extraction_method").description("The extraction method of the parsed paper record. Either of $allowedExtractionMethodValues."),
    fieldWithPath("statements[]").description("The list of parsed statements that will be added to a new contribution of the paper."),
    *applyPathPrefix(
        "statements[].",
        polymorphicResponseFields(
            existingPredicateContributionStatementResponseFields(),
            newPredicateContributionStatementResponseFields(),
        )
    ).toTypedArray(),
    *authorListFields(type = "parsed paper record").toTypedArray(),
)

fun existingPredicateContributionStatementResponseFields() = listOf(
    fieldWithPath("predicate_id").description("The id of the predicate of the statement. If present, indicates that an already existing predicate with the provided id will be reused. Mutually exclusive with `predicate_id`.").optional(),
    fieldWithPath("object").description("The object of the statement.").references<TypedValueRepresentation>(),
    *applyPathPrefix("object.", typedValueResponseFields()).toTypedArray(),
)

fun newPredicateContributionStatementResponseFields() = listOf(
    fieldWithPath("predicate_label").description("The label of the predicate of the statemment. If present, indicates that a new predicate will be created with the provided label. Mutually exclusive with `predicate_id`.").optional(),
    fieldWithPath("object").description("The object of the statement.").references<TypedValueRepresentation>(),
    *applyPathPrefix("object.", typedValueResponseFields()).toTypedArray(),
)

fun typedValueResponseFields() = listOf(
    fieldWithPath("namespace").description("The namespace of the object. (optional)").optional(),
    fieldWithPath("value").description("The value of the object. (optional)").optional(),
    fieldWithPath("type").description("The type of the object."),
)

fun paperCSVRecordImportResultResponseFields() = listOf(
    fieldWithPath("id").description("The id of the paper import result."),
    fieldWithPath("imported_entity_id").description("The id of the created entity."),
    fieldWithPath("imported_entity_type").description("The type of the created entity. Either of $allowedEntityTypeValues."),
    fieldWithPath("csv_id").description("The id of the csv."),
    fieldWithPath("item_number").description("The item number of the entity."),
    fieldWithPath("line_number").description("The line number of the entity."),
)
