package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createUntypedTemplateProperty
import org.orkg.contenttypes.input.UntypedPropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toUntypedTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class AbstractTemplatePropertyUpdaterUntypedPropertyUnitTest : AbstractTemplatePropertyUpdaterUnitTest() {
    @Test
    fun `Given an updated untyped template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand()

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 0, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated untyped template property, when label has changed, it updates the label`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when placeholder has changed, it updates the placeholder`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(statements, contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when description has changed, it updates the description`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(statements, contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when min count has changed, it updates the min count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(statements, contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when max count has changed, it updates the max count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(statements, contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when old property was a typed template property, it remove all class and datatype statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty()
        val newProperty = UntypedPropertyCommand(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            path = oldProperty.path.id
        )
        val statements = listOf(
            createStatement(
                id = StatementId("S123"),
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shDatatype),
                `object` = createClass(Classes.integer)
            ),
            createStatement(
                id = StatementId("S456"),
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shClass),
                `object` = createClass(Classes.resources)
            )
        )
        val statementsToRemove = statements.map { it.id }.toSet()

        every { statementService.deleteAllById(statementsToRemove) } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) { statementService.deleteAllById(statementsToRemove) }
    }

    @Test
    fun `Given an updated untyped template property, when path has changed, it updates the path`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand().copy(
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

        abstractTemplatePropertyUpdater.update(statements, contributorId, 0, newProperty, oldProperty)

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
    fun `Given an updated untyped template property, when order has changed, it updates the order`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyCommand()
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
