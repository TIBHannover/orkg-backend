package org.orkg.graph.domain

import org.orkg.common.ContributorId
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class NeitherOwnerNorCurator(contributorId: ContributorId) : SimpleMessageException(
    status = HttpStatus.FORBIDDEN,
    message = "Contributor <$contributorId> does not own the entity to be deleted and is not a curator."
)
