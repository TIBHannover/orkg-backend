package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.input.ResourcePropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toOtherLiteralTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class AbstractTemplatePropertyUpdaterOtherLiteralPropertyUnitTest : AbstractTemplatePropertyUpdaterUnitTest() {
    @Test
    fun `Given an updated literal template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand()

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 3, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated literal template property, when label has changed, it updates the label`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            label = "new label"
        )

        every {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldProperty.id,
                    contributorId = contributorId,
                    label = newProperty.label
                )
            )
        } just runs

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            unsafeResourceUseCases.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldProperty.id,
                    contributorId = contributorId,
                    label = newProperty.label
                )
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when placeholder has changed, it updates the placeholder`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            placeholder = "new placeholder"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = "old placeholder")
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when description has changed, it updates the description`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            description = "new description"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = "old description")
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when min count has changed, it updates the min count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            minCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(label = "4", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinCount,
                label = newProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinCount,
                label = newProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when max count has changed, it updates the max count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            maxCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(label = "4", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxCount,
                label = newProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxCount,
                label = newProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when datatype has changed, it updates the datatype`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            datatype = Classes.string
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shDatatype),
                `object` = createClass(Classes.integer)
            )
        )

        every { statementService.deleteAllById(setOf(statementToRemove)) } just runs
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shDatatype,
                    objectId = newProperty.datatype
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementToRemove)) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shDatatype,
                    objectId = newProperty.datatype
                )
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when changed to a resource template property, it updates the datatype statement to a class statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = ResourcePropertyCommand(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            path = oldProperty.path.id,
            `class` = Classes.paper
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shDatatype),
                `object` = createClass(Classes.integer)
            )
        )

        every { statementService.deleteAllById(setOf(statementToRemove)) } just runs
        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shClass,
                    objectId = newProperty.`class`
                )
            )
        } returns StatementId("S1")

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementToRemove)) }
        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = oldProperty.id,
                    predicateId = Predicates.shClass,
                    objectId = newProperty.`class`
                )
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when path has changed, it updates the path`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand().copy(
            path = Predicates.hasLink
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.hasLink),
                `object` = createPredicate(oldProperty.path.id)
            )
        )

        every {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPath,
                objectId = newProperty.path
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPath,
                objectId = newProperty.path
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when order has changed, it updates the order`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = oldProperty.toOtherLiteralTemplatePropertyCommand()
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(label = "0", datatype = Literals.XSD.INT.prefixedUri)
            )
        )
        val newOrder = 2

        every {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shOrder,
                label = newOrder.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, newOrder, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateRequiredProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shOrder,
                label = newOrder.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }
}
