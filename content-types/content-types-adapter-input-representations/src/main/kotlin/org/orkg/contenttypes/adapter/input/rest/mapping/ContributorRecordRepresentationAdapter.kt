package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ContributorRecordRepresentation
import org.orkg.contenttypes.domain.ContributorRecord
import org.springframework.data.domain.Page

interface ContributorRecordRepresentationAdapter {
    fun Page<ContributorRecord>.mapToContributorRecordRepresentation(): Page<ContributorRecordRepresentation> =
        map { it.toContributorRecordRepresentation() }

    fun ContributorRecord.toContributorRecordRepresentation(): ContributorRecordRepresentation =
        ContributorRecordRepresentation(
            contributorId = contributorId,
            comparisonCount = comparisonCount,
            paperCount = paperCount,
            contributionCount = contributionCount,
            researchProblemCount = researchProblemCount,
            visualizationCount = visualizationCount,
            totalCount = totalCount,
        )
}
