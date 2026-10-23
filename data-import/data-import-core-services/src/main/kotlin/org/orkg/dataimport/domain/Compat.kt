package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.graph.domain.ExtractionMethod
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.step.StepExecution
import java.util.UUID

internal fun extractContributorId(jobParameters: Map<String, Any>): ContributorId =
    ContributorId(jobParameters[CONTRIBUTOR_ID_FIELD]!!.toString())

internal fun extractContributorId(jobExecution: JobExecution): ContributorId =
    jobExecution.jobParameters.get<ContributorId>(CONTRIBUTOR_ID_FIELD)

internal fun extractContributorId(stepExecution: StepExecution): ContributorId =
    stepExecution.jobParameters.get<ContributorId>(CONTRIBUTOR_ID_FIELD)

internal fun extractExtractionMethod(jobParameters: Map<String, Any>): ExtractionMethod =
    jobParameters[EXTRACTION_METHOD_FIELD] as ExtractionMethod

internal fun extractModifiable(jobParameters: Map<String, Any>): Boolean =
    jobParameters[MODIFIABLE_FIELD] as Boolean

internal fun extractCSVID(jobParameters: Map<String, Any>): CSVID =
    CSVID(jobParameters[CSV_ID_FIELD]!!.toString())

internal fun extractCSVID(jobExecution: JobExecution): CSVID =
    jobExecution.jobParameters.get<CSVID>(CSV_ID_FIELD)

internal fun extractCSVType(stepExecution: StepExecution): CSV.Type =
    stepExecution.jobParameters.get<CSV.Type>(CSV_TYPE_FIELD)

internal fun extractRdfFileId(jobParameters: Map<String, Any>): UUID =
    UUID.fromString(jobParameters[RDF_FILE_ID_FIELD]!!.toString())

internal fun extractRdfFileId(jobExecution: JobExecution): UUID =
    jobExecution.jobParameters.get<UUID>(RDF_FILE_ID_FIELD)
