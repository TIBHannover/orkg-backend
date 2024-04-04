package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.RosettaTemplateUseCases
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class RosettaTemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : RosettaTemplateUseCases {
    override fun findById(id: ThingId): Optional<RosettaTemplate> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.rosettaNodeShape in it.classes }
            .map { it.toRosettaTemplate() }

    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        pageable: Pageable
    ): Page<RosettaTemplate> =
        resourceRepository.findAll(
            label = searchString,
            visibility = visibility,
            createdBy = createdBy,
            includeClasses = setOf(Classes.rosettaNodeShape),
            pageable = pageable
        ).pmap { it.toRosettaTemplate() }

    private fun Resource.toRosettaTemplate(): RosettaTemplate {
        val statements = statementRepository.fetchAsBundle(
            id = id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 2,
                blacklist = emptyList(),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ).groupBy { it.subject.id }
        return RosettaTemplate(
            id = id,
            label = label,
            description = statements[id]!!
                .wherePredicate(Predicates.description)
                .singleOrNull()?.`object`?.label,
            formattedLabel = statements[id]!!
                .wherePredicate(Predicates.templateLabelFormat)
                .singleOrNull()
                ?.let { FormattedLabel.of(it.`object`.label) },
            targetClass = statements[id]!!
                .wherePredicate(Predicates.shTargetClass)
                .single()
                .`object`.id,
            properties = statements[id]!!
                .wherePredicate(Predicates.shProperty)
                .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                .map { TemplateProperty.from(it.`object` as Resource, statements[it.`object`.id].orEmpty()) }
                .sortedBy { it.order },
            isClosed = statements[id]!!
                .wherePredicate(Predicates.shClosed)
                .singleOrNull()
                .let { it?.`object`?.label.toBoolean() },
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = listOf(organizationId),
            observatories = listOf(observatoryId),
            visibility = visibility,
            unlistedBy = unlistedBy
        )
    }
}
