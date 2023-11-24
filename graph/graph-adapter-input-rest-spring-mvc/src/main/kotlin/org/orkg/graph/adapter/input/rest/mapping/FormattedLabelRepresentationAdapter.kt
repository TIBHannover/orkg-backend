package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.common.ThingId
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.TemplateRepository

interface FormattedLabelRepresentationAdapter {
    val statementService: StatementUseCases
    val templateRepository: TemplateRepository
    val flags: FeatureFlagService

    fun countsFor(resources: List<Resource>): Map<ThingId, Long> {
        val resourceIds = resources.map { it.id }.toSet()
        return statementService.countStatementsAboutResources(resourceIds)
    }

    fun formatLabelFor(resources: List<Resource>): Map<ThingId, FormattedLabel?> =
        if (flags.isFormattedLabelsEnabled())
            resources.associate { it.id to templateRepository.formattedLabelFor(it.id, it.classes) }
        else emptyMap()
}
