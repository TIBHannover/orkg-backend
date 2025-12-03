package org.orkg.contenttypes.domain.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.contenttypes.domain.ContributorRecord

fun createContributorRecord() = ContributorRecord(
    contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    comparisonCount = 6,
    paperCount = 4,
    contributionCount = 7,
    researchProblemCount = 8,
    visualizationCount = 1,
    totalCount = 26,
)
