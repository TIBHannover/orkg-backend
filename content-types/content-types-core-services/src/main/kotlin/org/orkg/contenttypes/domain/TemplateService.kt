package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.GeneralStatement
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
class TemplateService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val templateRepository: TemplateRepository
) : TemplateUseCases {
    override fun findById(id: ThingId): Optional<Template> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.nodeShape in it.classes }
            .map { it.toTemplate() }

    override fun findAll(
        searchString: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        researchField: ThingId?,
        researchProblem: ThingId?,
        targetClass: ThingId?,
        pageable: Pageable
    ): Page<Template> =
        templateRepository.findAll(searchString, visibility, createdBy, researchField, researchProblem, targetClass, pageable)
            .pmap { it.toTemplate() }

    private fun Resource.toTemplate(): Template {
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
        return Template(
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
            relations = TemplateRelation(
                researchFields = statements[id]!!
                    .wherePredicate(Predicates.templateOfResearchField)
                    .objectIdsAndLabel(),
                researchProblems = statements[id]!!
                    .wherePredicate(Predicates.templateOfResearchProblem)
                    .objectIdsAndLabel(),
                predicate = statements[id]!!
                    .wherePredicate(Predicates.templateOfPredicate)
                    .singleOrNull()
                    .objectIdAndLabel()
            ),
            properties = statements[id]!!
                .wherePredicate(Predicates.shProperty)
                .filter { it.`object` is Resource && Classes.propertyShape in (it.`object` as Resource).classes }
                .mapNotNull { (it.`object` as Resource).toTemplateProperty(statements[it.`object`.id].orEmpty()) }
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

    private fun Resource.toTemplateProperty(statements: Iterable<GeneralStatement>): TemplateProperty? {
        val order = statements.wherePredicate(Predicates.shOrder).single().`object`.label.toLong()
        val minCount = statements.wherePredicate(Predicates.shMinCount).singleOrNull()?.`object`?.label?.toInt()
        val maxCount = statements.wherePredicate(Predicates.shMaxCount).singleOrNull()?.`object`?.label?.toInt()
        val pattern = statements.wherePredicate(Predicates.shPattern).singleOrNull()?.`object`?.label
        val path = statements.wherePredicate(Predicates.shPath).single().objectIdAndLabel()!!
        val datatype = statements.wherePredicate(Predicates.shDatatype).singleOrNull().objectIdAndLabel()
        val `class` = statements.wherePredicate(Predicates.shClass).singleOrNull().objectIdAndLabel()
        return when {
            datatype != null -> LiteralTemplateProperty(id, label, order, minCount, maxCount, pattern, path, createdBy, createdAt, datatype)
            `class` != null -> ResourceTemplateProperty(id, label, order, minCount, maxCount, pattern, path, createdBy, createdAt, `class`)
            else -> null
        }
    }
}
