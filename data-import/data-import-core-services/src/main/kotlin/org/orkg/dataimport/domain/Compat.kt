package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.step.StepExecution

internal fun extractContributorId(jobExecution: JobExecution): ContributorId =
    jobExecution.jobParameters.get<ContributorId>(CONTRIBUTOR_ID_FIELD)

internal fun extractContributorId(stepExecution: StepExecution): ContributorId =
    stepExecution.jobParameters.get<ContributorId>(CONTRIBUTOR_ID_FIELD)

internal fun extractCSVID(jobParameters: Map<String, Any>): CSVID =
    CSVID(jobParameters[CSV_ID_FIELD]!!.toString())

internal fun extractCSVID(jobExecution: JobExecution): CSVID =
    jobExecution.jobParameters.get<CSVID>(CSV_ID_FIELD)

internal fun extractCSVType(stepExecution: StepExecution): CSV.Type =
    stepExecution.jobParameters.get<CSV.Type>(CSV_TYPE_FIELD)
