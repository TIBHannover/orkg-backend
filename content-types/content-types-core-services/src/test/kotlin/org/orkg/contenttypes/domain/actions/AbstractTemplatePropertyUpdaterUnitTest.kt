package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteralTemplateProperty
import org.orkg.contenttypes.domain.testing.fixtures.createDummyResourceTemplateProperty
import org.orkg.contenttypes.input.LiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.toLiteralTemplatePropertyDefinition
import org.orkg.contenttypes.input.testing.fixtures.toResourceTemplatePropertyDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class AbstractTemplatePropertyUpdaterUnitTest {
    private val statementService: StatementUseCases = mockk()
    private val resourceService: ResourceUseCases = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val abstractTemplatePropertyUpdater = AbstractTemplatePropertyUpdater(statementService, resourceService, singleStatementPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, resourceService, singleStatementPropertyUpdater)
    }

    //
    // Literal property
    //

    @Test
    fun `Given an updated literal template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition()

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated literal template property, when label has changed, it updates the label`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
    fun `Given an updated literal template property, when placeholder has changed, it updates the placeholder`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
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
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
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
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
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
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
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
    fun `Given an updated literal template property, when pattern has changed, it updates the pattern`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
            pattern = "[0-9]+"
        )
        val statements = listOf(
            createStatement(
                subject = createResource(oldProperty.id),
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = """\w+""")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when datatype has changed, it updates the datatype`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every { statementService.delete(setOf(statementToRemove)) } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shDatatype,
                `object` = newProperty.datatype
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shDatatype,
                `object` = newProperty.datatype
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when changed to a resource template property, it updates the datatype statement to a class statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = ResourcePropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            pattern = oldProperty.pattern,
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

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every { statementService.delete(setOf(statementToRemove)) } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shClass,
                `object` = newProperty.`class`
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 1, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shClass,
                `object` = newProperty.`class`
            )
        }
    }

    @Test
    fun `Given an updated literal template property, when path has changed, it updates the path`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition().copy(
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
    fun `Given an updated literal template property, when order has changed, it updates the order`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyLiteralTemplateProperty()
        val newProperty = oldProperty.toLiteralTemplatePropertyDefinition()
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
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shOrder,
                label = newOrder.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        }
    }

    //
    // Resource property
    //

    @Test
    fun `Given an updated resource template property, when there are no changes, it does nothing`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition()

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)
    }

    @Test
    fun `Given an updated resource template property, when label has changed, it updates the label`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
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

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

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
    fun `Given an updated resource template property, when placeholder has changed, it updates the placeholder`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            placeholder = "new placeholder"
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.placeholder),
                `object` = createLiteral(label = "old placeholder")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.placeholder,
                label = newProperty.placeholder
            )
        }
    }

    @Test
    fun `Given an updated resource template property, when description has changed, it updates the description`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            description = "new description"
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.description),
                `object` = createLiteral(label = "old description")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.description,
                label = newProperty.description
            )
        }
    }

    @Test
    fun `Given an updated resource template property, when min count has changed, it updates the min count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            minCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shMinCount),
                `object` = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMinCount,
                label = newProperty.minCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
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
    fun `Given an updated resource template property, when max count has changed, it updates the max count`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            maxCount = 5
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shMaxCount),
                `object` = createLiteral(label = "5", datatype = Literals.XSD.INT.prefixedUri)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shMaxCount,
                label = newProperty.maxCount.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
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
    fun `Given an updated resource template property, when pattern has changed, it updates the pattern`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            pattern = "[0-9]+"
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shPattern),
                `object` = createLiteral(label = """\w+""")
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.update(
                statements = statements,
                contributorId = contributorId,
                subjectId = oldProperty.id,
                predicateId = Predicates.shPattern,
                label = newProperty.pattern
            )
        }
    }

    @Test
    fun `Given an updated resource template property, when class has changed, it updates the class`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            `class` = Classes.paper
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shClass),
                `object` = createClass(Classes.contribution)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every { statementService.delete(setOf(statementToRemove)) } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shClass,
                `object` = newProperty.`class`
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shClass,
                `object` = newProperty.`class`
            )
        }
    }

    @Test
    fun `Given an updated resource template property, when changed to a literal template property, it updates the class statement to a datatype statement`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = LiteralPropertyDefinition(
            label = oldProperty.label,
            placeholder = oldProperty.placeholder,
            description = oldProperty.description,
            minCount = oldProperty.minCount,
            maxCount = oldProperty.maxCount,
            pattern = oldProperty.pattern,
            path = oldProperty.path.id,
            datatype = Classes.integer
        )
        val statementToRemove = StatementId("S123")
        val statements = listOf(
            createStatement(
                id = statementToRemove,
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shClass),
                `object` = createClass(Classes.paper)
            )
        )

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every { statementService.delete(setOf(statementToRemove)) } just runs
        every {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shDatatype,
                `object` = newProperty.datatype
            )
        } just runs

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

        verify(exactly = 1) { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) }
        verify(exactly = 1) { statementService.delete(setOf(statementToRemove)) }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = oldProperty.id,
                predicate = Predicates.shDatatype,
                `object` = newProperty.datatype
            )
        }
    }

    @Test
    fun `Given an updated resource template property, when path has changed, it updates the path`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition().copy(
            path = Predicates.hasLink
        )
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
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

        abstractTemplatePropertyUpdater.update(contributorId, 2, newProperty, oldProperty)

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
    fun `Given an updated resource template property, when order has changed, it updates the order`() {
        val contributorId = ContributorId(UUID.randomUUID())
        val oldProperty = createDummyResourceTemplateProperty()
        val newProperty = oldProperty.toResourceTemplatePropertyDefinition()
        val statements = listOf(
            createStatement(
                subject = createLiteral(oldProperty.id),
                predicate = createPredicate(Predicates.shOrder),
                `object` = createLiteral(label = "1", datatype = Literals.XSD.INT.prefixedUri)
            )
        )
        val newOrder = 3

        every { statementService.findAll(subjectId = oldProperty.id, pageable = PageRequests.ALL) } returns pageOf(statements)
        every {
            singleStatementPropertyUpdater.update(
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
            singleStatementPropertyUpdater.update(
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
