package org.orkg.graph.domain

import dev.forkhandles.values.ofOrNull
import org.orkg.common.ThingId
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.stereotype.Service
import kotlin.collections.List

@Service
@TransactionalOnNeo4j
class FormattedLabelService(
    private val repository: FormattedLabelRepository,
) : FormattedLabelUseCases {
    override fun findFormattedLabels(resources: List<Resource>): Map<ThingId, FormattedLabel?> {
        val resourceIdToTemplateTargetClass = resources.filter { it.classes.isNotEmpty() }
            .associate { it.id to it.classes.first() }
        val templateSpecs = when {
            resourceIdToTemplateTargetClass.isEmpty() -> emptyMap()
            else -> repository.findTemplateSpecs(resourceIdToTemplateTargetClass)
        }
        return resources.associate { resource ->
            resource.id to templateSpecs[resource.id]?.let { FormattedLabel.ofOrNull(it.composeFormattedLabel()) }
        }
    }
}
