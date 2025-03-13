package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.NumberLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.ResourceTemplatePropertyCommand
import org.orkg.contenttypes.input.StringLiteralTemplatePropertyCommand
import org.orkg.contenttypes.input.TemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class AbstractTemplatePropertyCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
) {
    internal fun create(
        contributorId: ContributorId,
        templateId: ThingId,
        order: Int,
        property: TemplatePropertyCommand,
    ): ThingId {
        val propertyId = unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = property.label,
                classes = setOf(Classes.propertyShape)
            )
        )
        property.placeholder?.let { placeholder ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.placeholder,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = placeholder
                        )
                    )
                )
            )
        }
        property.description?.let { description ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.description,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = description
                        )
                    )
                )
            )
        }
        property.minCount?.let { min ->
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMinCount,
                    objectId = unsafeLiteralUseCases.create(
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
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shMaxCount,
                    objectId = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = max.toString(),
                            datatype = Literals.XSD.INT.prefixedUri
                        )
                    )
                )
            )
        }
        if (property is StringLiteralTemplatePropertyCommand) {
            property.pattern?.let { pattern ->
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shPattern,
                        objectId = unsafeLiteralUseCases.create(
                            CreateLiteralUseCase.CreateCommand(
                                contributorId = contributorId,
                                label = pattern
                            )
                        )
                    )
                )
            }
        } else if (property is NumberLiteralTemplatePropertyCommand) {
            property.minInclusive?.let { minInclusive ->
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shMinInclusive,
                        objectId = unsafeLiteralUseCases.create(
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
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = propertyId,
                        predicateId = Predicates.shMaxInclusive,
                        objectId = unsafeLiteralUseCases.create(
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
        if (property is LiteralTemplatePropertyCommand) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shDatatype,
                    objectId = property.datatype
                )
            )
        } else if (property is ResourceTemplatePropertyCommand) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = propertyId,
                    predicateId = Predicates.shClass,
                    objectId = property.`class`
                )
            )
        }
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = propertyId,
                predicateId = Predicates.shPath,
                objectId = property.path
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = propertyId,
                predicateId = Predicates.shOrder,
                objectId = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = order.toString(),
                        datatype = Literals.XSD.INT.prefixedUri
                    )
                )
            )
        )
        unsafeStatementUseCases.create(
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
