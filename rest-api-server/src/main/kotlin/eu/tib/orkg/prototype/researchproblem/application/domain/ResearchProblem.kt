package eu.tib.orkg.prototype.researchproblem.application.domain

import eu.tib.orkg.prototype.statements.domain.model.ResourceId

data class ResearchProblem(
    val id: ResourceId,
    val label: String
)
