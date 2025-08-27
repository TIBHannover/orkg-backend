package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.springframework.batch.core.JobExecution

internal fun extractContributorId(jobExecution: JobExecution): ContributorId =
    jobExecution.jobParameters.get<ContributorId>(CONTRIBUTOR_ID_FIELD)
