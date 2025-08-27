package org.orkg.dataimport.testing.fixtures

import org.orkg.dataimport.domain.testing.asciidoc.allowedJobStatusValues
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

fun jobStatusResponseFields(): List<FieldDescriptor> = listOf(
    fieldWithPath("job_id").description("The identifier of the job."),
    fieldWithPath("job_name").description("The name of the job."),
    fieldWithPath("context").description("The context of the job."),
    fieldWithPath("context.csv_id").description("The id of the CSV."),
    fieldWithPath("status").description("The status of the job. Either of $allowedJobStatusValues."),
)
