package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.ResourceTemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase

class AbstractTemplatePropertyUpdater(
    private val statementService: StatementUseCases,
    private val resourceService: ResourceUseCases,
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater
) {
    constructor(
        literalService: LiteralUseCases,
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
    ) : this(statementService, resourceService, SingleStatementPropertyUpdater(literalService, statementService))

    internal fun update(
        contributorId: ContributorId,
        order: Int,
        newProperty: TemplatePropertyDefinition,
        oldProperty: TemplateProperty
    ) {
        val statements by lazy {
            statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL).content
        }

        if (newProperty.label != oldProperty.label) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldProperty.id,
                    label = newProperty.label
                )
            )
        }

        if (newProperty.placeholder != oldProperty.placeholder) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        }

        if (newProperty.description != oldProperty.description) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        }

        if (newProperty.minCount != oldProperty.minCount) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinCount,
                label = newProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }

        if (newProperty.maxCount != oldProperty.maxCount) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxCount,
                label = newProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }

        if (newProperty.pattern != oldProperty.pattern) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        }

        if (newProperty is LiteralTemplatePropertyDefinition && (oldProperty is LiteralTemplateProperty && newProperty.datatype != oldProperty.datatype.id || oldProperty !is LiteralTemplateProperty)) {
            val toRemove = statements.filter { it.predicate.id == Predicates.shDatatype || it.predicate.id == Predicates.shClass }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shDatatype,
                `object` = newProperty.datatype
            )
        } else if (newProperty is ResourceTemplatePropertyDefinition && (oldProperty is ResourceTemplateProperty && newProperty.`class` != oldProperty.`class`.id || oldProperty !is ResourceTemplateProperty)) {
            val toRemove = statements.filter { it.predicate.id == Predicates.shDatatype || it.predicate.id == Predicates.shClass }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shClass,
                `object` = newProperty.`class`
            )
        }

        if (newProperty.path != oldProperty.path.id) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPath,
                objectId = newProperty.path
            )
        }

        if (order.toLong() != oldProperty.order) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shOrder,
                label = order.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }
}
