package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.ExtractionMethod

interface CreateVisualizationUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val description: String,
        val authors: List<Author>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val extractionMethod: ExtractionMethod
    )
}
