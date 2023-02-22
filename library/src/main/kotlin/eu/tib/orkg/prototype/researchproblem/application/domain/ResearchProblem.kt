package eu.tib.orkg.prototype.researchproblem.application.domain

import eu.tib.orkg.prototype.statements.domain.model.ThingId

data class ResearchProblem(
    val id: ThingId,
    val label: String
)
