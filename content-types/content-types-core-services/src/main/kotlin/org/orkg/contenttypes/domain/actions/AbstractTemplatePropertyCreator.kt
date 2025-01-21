package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.NumberLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.ResourceTemplatePropertyDefinition
import org.orkg.contenttypes.input.StringLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.TemplatePropertyDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases

class AbstractTemplatePropertyCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val literalService: LiteralUseCases,
    private val statementService: StatementUseCases
) {
    internal fun create(
        contributorId: ContributorId,
        templateId: ThingId,
        order: Int,
        property: TemplatePropertyDefinition
    ): ThingId {
        val propertyId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                label = property.label,
                classes = setOf(Classes.propertyShape),
                contributorId = contributorId
            )
        )
        property.placeholder?.let { placeholder ->
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.placeholder,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = placeholder
                    )
                )
            )
        }
        property.description?.let { description ->
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.description,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = description
                    )
                )
            )
        }
        property.minCount?.let { min ->
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.shMinCount,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = min.toString(),
                        datatype = Literals.XSD.INT.prefixedUri
                    )
                )
            )
        }
        property.maxCount?.let { max ->
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.shMaxCount,
                `object` = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = max.toString(),
                        datatype = Literals.XSD.INT.prefixedUri
                    )
                )
            )
        }
        if (property is StringLiteralTemplatePropertyDefinition) {
            property.pattern?.let { pattern ->
                statementService.add(
                    userId = contributorId,
                    subject = propertyId,
                    predicate = Predicates.shPattern,
                    `object` = literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = pattern
                        )
                    )
                )
            }
        } else if (property is NumberLiteralTemplatePropertyDefinition) {
            property.minInclusive?.let { minInclusive ->
                statementService.add(
                    userId = contributorId,
                    subject = propertyId,
                    predicate = Predicates.shMinInclusive,
                    `object` = literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = minInclusive.toString(),
                            datatype = Literals.XSD.fromClass(property.datatype)?.prefixedUri
                                ?: Literals.XSD.DECIMAL.prefixedUri
                        )
                    )
                )
            }
            property.maxInclusive?.let { maxInclusive ->
                statementService.add(
                    userId = contributorId,
                    subject = propertyId,
                    predicate = Predicates.shMaxInclusive,
                    `object` = literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = maxInclusive.toString(),
                            datatype = Literals.XSD.fromClass(property.datatype)?.prefixedUri
                                ?: Literals.XSD.DECIMAL.prefixedUri
                        )
                    )
                )
            }
        }
        if (property is LiteralTemplatePropertyDefinition) {
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.shDatatype,
                `object` = property.datatype
            )
        } else if (property is ResourceTemplatePropertyDefinition) {
            statementService.add(
                userId = contributorId,
                subject = propertyId,
                predicate = Predicates.shClass,
                `object` = property.`class`
            )
        }
        statementService.add(
            userId = contributorId,
            subject = propertyId,
            predicate = Predicates.shPath,
            `object` = property.path
        )
        statementService.add(
            userId = contributorId,
            subject = propertyId,
            predicate = Predicates.shOrder,
            `object` = literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = order.toString(),
                    datatype = Literals.XSD.INT.prefixedUri
                )
            )
        )
        statementService.add(
            userId = contributorId,
            subject = templateId,
            predicate = Predicates.shProperty,
            `object` = propertyId
        )
        return propertyId
    }
}
