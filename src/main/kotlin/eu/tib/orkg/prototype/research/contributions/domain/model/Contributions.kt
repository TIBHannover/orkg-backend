package eu.tib.orkg.prototype.research.contributions.domain.model

data class ResearchContribution(val value: String)

interface ResearchContributionRepository {
    fun findAll(): Collection<ResearchContribution>
}
