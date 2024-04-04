package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourceTemplatePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.contenttypes.input.UntypedPropertyDefinition
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
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        }

        if (newProperty.description != oldProperty.description) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        }

        if (newProperty.minCount != oldProperty.minCount) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinCount,
                label = newProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }

        if (newProperty.maxCount != oldProperty.maxCount) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxCount,
                label = newProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }

        if (newProperty is StringLiteralTemplatePropertyDefinition) {
            val oldPattern = if (oldProperty is StringLiteralTemplateProperty) oldProperty.pattern else null
            if (newProperty.pattern != oldPattern) {
                singleStatementPropertyUpdater.updateOptionalProperty(
                    statements = statements,
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shPattern,
                    label = newProperty.pattern
                )
            }
        } else if (oldProperty is StringLiteralTemplateProperty) {
            val toRemove = statements.wherePredicate(Predicates.shPattern)
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
        }

        if (newProperty is NumberLiteralPropertyDefinition<*>) {
            val oldMinInclusive = if (oldProperty is NumberLiteralTemplateProperty<*>) oldProperty.minInclusive else null
            if (newProperty.minInclusive != oldMinInclusive || oldProperty is NumberLiteralTemplateProperty<*> && oldProperty.datatype.id != newProperty.datatype) {
                singleStatementPropertyUpdater.updateOptionalProperty(
                    statements = statements,
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shMinInclusive,
                    label = newProperty.minInclusive.toString(),
                    datatype = Literals.XSD.fromClass(newProperty.datatype)?.prefixedUri
                        ?: Literals.XSD.DECIMAL.prefixedUri
                )
            }
            val oldMaxInclusive = if (oldProperty is NumberLiteralTemplateProperty<*>) oldProperty.maxInclusive else null
            if (newProperty.maxInclusive != oldMaxInclusive || oldProperty is NumberLiteralTemplateProperty<*> && oldProperty.datatype.id != newProperty.datatype) {
                singleStatementPropertyUpdater.updateOptionalProperty(
                    statements = statements,
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shMaxInclusive,
                    label = newProperty.maxInclusive.toString(),
                    datatype = Literals.XSD.fromClass(newProperty.datatype)?.prefixedUri
                        ?: Literals.XSD.DECIMAL.prefixedUri
                )
            }
        } else if (oldProperty is NumberLiteralTemplateProperty<*>) {
            val toRemove = statements.filter { it.predicate.id == Predicates.shMinInclusive || it.predicate.id == Predicates.shMaxInclusive }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
        }

        if (newProperty is LiteralTemplatePropertyDefinition) {
            if (oldProperty is LiteralTemplateProperty && newProperty.datatype != oldProperty.datatype.id || oldProperty !is LiteralTemplateProperty) {
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
            }
        } else if (newProperty is ResourceTemplatePropertyDefinition) {
            if (oldProperty is ResourceTemplateProperty && newProperty.`class` != oldProperty.`class`.id || oldProperty !is ResourceTemplateProperty) {
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
        } else if (newProperty is UntypedPropertyDefinition && oldProperty !is UntypedTemplateProperty) {
            val toRemove = statements.filter { it.predicate.id == Predicates.shDatatype || it.predicate.id == Predicates.shClass }
            if (toRemove.isNotEmpty()) {
                statementService.delete(toRemove.map { it.id }.toSet())
            }
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
            singleStatementPropertyUpdater.updateRequiredProperty(
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
