package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.testing.fixtures.createDummyOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyUntypedTemplateProperty
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.toUntypedTemplatePropertyDefinition
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
import org.orkg.testing.pageOf

class AbstractTemplatePropertyUpdaterUntypedPropertyUnitTest : AbstractTemplatePropertyUpdaterUnitTest() {
    @Test
    fun `Given an updated untyped template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition()

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated untyped template property, when label has changed, it updates the label`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            label = "new label"
        )

        every {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldProperty.id,
                    label = newProperty.label
                )
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) {
            resourceService.update(
                UpdateResourceUseCase.UpdateCommand(
                    id = oldProperty.id,
                    label = newProperty.label
                )
            )
        }
    }

    @Test
    fun `Given an updated untyped template property, when placeholder has changed, it updates the placeholder`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            placeholder = "new placeholder"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = "old placeholder")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
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
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            description = "new description"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = "old description")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
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
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            minCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
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

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
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
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            maxCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
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

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
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
        val oldProperty = createDummyOtherLiteralTemplateProperty()
        val newProperty = UntypedPropertyDefinition(
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

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every { statementService.delete(statementsToRemove) } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 4, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(statementsToRemove) }
    }

    @Test
    fun `Given an updated untyped template property, when path has changed, it updates the path`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition().copy(
            path = Predicates.hasLink
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.hasLink),
                `object` = createPredicate(oldProperty.path.id)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPath,
                objectId = newProperty.path
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
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
        val oldProperty = createDummyUntypedTemplateProperty()
        val newProperty = oldProperty.toUntypedTemplatePropertyDefinition()
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(label = "1", datatype = Literals.XSD.INT.prefixedUri)
            )
        )
        val newOrder = 3

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
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

        abstractTemplatePropertyUpdater.update(contributorId, newOrder, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
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
