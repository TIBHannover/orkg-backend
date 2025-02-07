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
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
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
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.placeholder,
                    objectId = literalService.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = placeholder
                        )
                    )
                )
            )
        }
        property.description?.let { description ->
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.description,
                    objectId = literalService.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = description
                        )
                    )
                )
            )
        }
        property.minCount?.let { min ->
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinCount,
                    objectId = literalService.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = min.toString(),
                            datatype = Literals.XSD.INT.prefixedUri
                        )
                    )
                )
            )
        }
        property.maxCount?.let { max ->
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxCount,
                    objectId = literalService.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = max.toString(),
                            datatype = Literals.XSD.INT.prefixedUri
                        )
                    )
                )
            )
        }
        if (property is StringLiteralTemplatePropertyDefinition) {
            property.pattern?.let { pattern ->
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shPattern,
                        objectId = literalService.create(
                            CreateLiteralUseCase.CreateCommand(
                                contributorId = contributorId,
                                label = pattern
                            )
                        )
                    )
                )
            }
        } else if (property is NumberLiteralTemplatePropertyDefinition) {
            property.minInclusive?.let { minInclusive ->
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shMinInclusive,
                        objectId = literalService.create(
                            CreateLiteralUseCase.CreateCommand(
                                contributorId = contributorId,
                                label = minInclusive.toString(),
                                datatype = Literals.XSD.fromClass(property.datatype)?.prefixedUri
                                    ?: Literals.XSD.DECIMAL.prefixedUri
                            )
                        )
                    )
                )
            }
            property.maxInclusive?.let { maxInclusive ->
                statementService.add(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shMaxInclusive,
                        objectId = literalService.create(
                            CreateLiteralUseCase.CreateCommand(
                                contributorId = contributorId,
                                label = maxInclusive.toString(),
                                datatype = Literals.XSD.fromClass(property.datatype)?.prefixedUri
                                    ?: Literals.XSD.DECIMAL.prefixedUri
                            )
                        )
                    )
                )
            }
        }
        if (property is LiteralTemplatePropertyDefinition) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } else if (property is ResourceTemplatePropertyDefinition) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shClass,
                    objectId = property.`class`
                )
            )
        }
        statementService.add(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = propertyId,
                predicateId = Predicates.shPath,
                objectId = property.path
            )
        )
        statementService.add(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = propertyId,
                predicateId = Predicates.shOrder,
                objectId = literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = order.toString(),
                        datatype = Literals.XSD.INT.prefixedUri
                    )
                )
            )
        )
        statementService.add(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = templateId,
                predicateId = Predicates.shProperty,
                objectId = propertyId
            )
        )
        return propertyId
    }
}
