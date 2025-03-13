package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createStringLiteralTemplateProperty
import org.orkg.contenttypes.input.NumberLiteralPropertyCommand
import org.orkg.contenttypes.input.StringLiteralPropertyCommand
import org.orkg.contenttypes.input.testing.fixtures.toStringLiteralTemplatePropertyCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class AbstractTemplatePropertyUpdaterStringLiteralPropertyUnitTest : AbstractTemplatePropertyUpdaterUnitTest() {
    @Test
    fun `Given an updated string literal template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createStringLiteralTemplateProperty()
        val newProperty = oldProperty.toStringLiteralTemplatePropertyCommand()

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 1, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated string literal template property, when pattern has changed, it updates the pattern`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createStringLiteralTemplateProperty()
        val newProperty = oldProperty.toStringLiteralTemplatePropertyCommand().copy(
            pattern = "[0-9]+"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = """\w+""")
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        }
    }

    @Test
    fun `Given an updated string literal template property, when old template property was of another type, it creates a pattern statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty().copy(
            datatype = ClassReference(Classes.string, "String", null)
        )
        val newProperty = StringLiteralPropertyCommand(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            pattern = "[0-9]+",
            path = oldProperty.path.id,
            datatype = Classes.string
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = """\w+""")
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when new template property is not a string literal property, it deletes all pattern statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createStringLiteralTemplateProperty()
        val newProperty = NumberLiteralPropertyCommand(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            minInclusive = null,
            maxInclusive = null,
            path = oldProperty.path.id,
            datatype = Classes.string
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = """\w+""")
            )
        )

        every { statementService.deleteAllById(setOf(statementToRemove)) } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.deleteAllById(setOf(statementToRemove)) }
    }
}
