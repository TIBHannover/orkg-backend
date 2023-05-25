package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.FormattedLabel
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository

interface FormattedLabelRepresentationAdapter {
    val statementRepository: StatementUseCases
    val templateRepository: TemplateRepository
    val flags: FeatureFlagService

    fun countsFor(resources: List<Resource>): Map<ThingId, Long> {
        val resourceIds = resources.map { it.id }.toSet()
        return statementRepository.countStatementsAboutResources(resourceIds)
    }

    fun formatLabelFor(resources: List<Resource>): Map<ThingId, FormattedLabel?> =
        if (flags.isFormattedLabelsEnabled())
            resources.associate { it.id to templateRepository.formattedLabelFor(it.id, it.classes) }
        else emptyMap()
}
