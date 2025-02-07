package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.RealNumber
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.testing.fixtures.createNumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createOtherLiteralTemplateProperty
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.toNumberLiteralTemplatePropertyDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class AbstractTemplatePropertyUpdaterRealNumberLiteralPropertyUnitTest : AbstractTemplatePropertyUpdaterUnitTest() {
    @Test
    fun `Given an updated number literal template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = oldProperty.toNumberLiteralTemplatePropertyDefinition()

        abstractTemplatePropertyUpdater.update(emptyList(), contributorId, 2, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated number literal template property, when minInclusive has changed, it updates the minInclusive literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = oldProperty.toNumberLiteralTemplatePropertyDefinition().copy(
            minInclusive = RealNumber(1)
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated number literal template property, when old template property was of another type, it creates a minInclusive statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty().copy(
            datatype = ClassReference(Classes.decimal, "Decimal", null)
        )
        val newProperty = NumberLiteralPropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            minInclusive = RealNumber(5),
            maxInclusive = null,
            path = oldProperty.path.id,
            datatype = Classes.decimal
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated number template property, when new template property is not a number literal property, it deletes all minInclusive statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = OtherLiteralPropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            path = oldProperty.path.id,
            datatype = oldProperty.datatype.id
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every { statementService.delete(setOf(statementToRemove)) } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
    }

    @Test
    fun `Given an updated number literal template property, when maxInclusive has changed, it updates the maxInclusive literal`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = oldProperty.toNumberLiteralTemplatePropertyDefinition().copy(
            maxInclusive = RealNumber(1)
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated number literal template property, when old template property was of another type, it creates a maxInclusive statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createOtherLiteralTemplateProperty().copy(
            datatype = ClassReference(Classes.decimal, "Decimal", null)
        )
        val newProperty = NumberLiteralPropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            maxCount = oldProperty.maxCount,
            minCount = oldProperty.minCount,
            maxInclusive = RealNumber(5),
            minInclusive = null,
            path = oldProperty.path.id,
            datatype = Classes.decimal
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 3, newProperty, oldProperty)

        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        }
    }

    @Test
    fun `Given an updated number template property, when new template property is not a number literal property, it deletes all maxInclusive statements`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = OtherLiteralPropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            maxCount = oldProperty.maxCount,
            minCount = oldProperty.minCount,
            path = oldProperty.path.id,
            datatype = oldProperty.datatype.id
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.DECIMAL.prefixedUri)
            )
        )

        every { statementService.delete(setOf(statementToRemove)) } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
    }

    @Test
    fun `Given an updated number literal template property, when old number literal template property had another datatype, it updates the minInclusive and maxInclusive statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createNumberLiteralTemplateProperty()
        val newProperty = oldProperty.toNumberLiteralTemplatePropertyDefinition().copy(
            datatype = Classes.decimal
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMinInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.INT.prefixedUri)
            ),
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxInclusive),
                `object` = createLiteral(label = "1.0", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

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
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        } just runs
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(statements, contributorId, 2, newProperty, oldProperty)

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
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinInclusive,
                label = newProperty.minInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxInclusive,
                label = newProperty.maxInclusive!!.toString(),
                datatype = Literals.XSD.DECIMAL.prefixedUri
            )
        }
    }
}
