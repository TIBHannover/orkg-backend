package org.orkg.community.input.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.input.CreateContributorIdentifierUseCase

fun createContributorIdentifierCommand() = CreateContributorIdentifierUseCase.CreateCommand(
    contributorId = ContributorId("9d791767-245b-46e1-b260-2c00fb34efda"),
    type = ContributorIdentifier.Type.ORCID,
    value = "0000-0001-5109-3700"
)
