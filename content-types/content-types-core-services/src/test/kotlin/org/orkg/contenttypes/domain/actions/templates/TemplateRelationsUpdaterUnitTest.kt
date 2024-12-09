package org.orkg.contenttypes.domain.actions.templates

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.TemplateRelations
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyUpdater
import org.orkg.contenttypes.domain.actions.StatementCollectionPropertyUpdater
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.testing.fixtures.createStatement

internal class TemplateRelationsUpdaterUnitTest {
    private val statementCollectionPropertyUpdater: StatementCollectionPropertyUpdater = mockk()
    private val singleStatementPropertyUpdater: SingleStatementPropertyUpdater = mockk()

    private val templateRelationsUpdater = TemplateRelationsUpdater(statementCollectionPropertyUpdater, singleStatementPropertyUpdater)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementCollectionPropertyUpdater, singleStatementPropertyUpdater)
    }

    @Test
    fun `Given a template update command, when updating with empty relations, it does nothing`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            relations = null
        )
        val state = UpdateTemplateState(
            template = template
        )

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }
    }

    @Test
    fun `Given a template update command, when relations are not empty, it updates all relation properties`() {
        val relations = TemplateRelations(
            researchFields = listOf(
                ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                ObjectIdAndLabel(ThingId("R456"), "irrelevant")
            ),
            researchProblems = listOf(
                ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                ObjectIdAndLabel(ThingId("R456"), "irrelevant")
            ),
            predicate = ObjectIdAndLabel(ThingId("P456"), "irrelevant")
        )
        val template = createDummyTemplate().copy(relations = relations)
        val command = dummyUpdateTemplateCommand()
        val statements = listOf(createStatement())
        val state = UpdateTemplateState(
            template = template,
            statements = mapOf(command.templateId to statements)
        )

        every {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = any(),
                objects = any<List<ThingId>>()
            )
        } just runs
        every {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = any(),
                objectId = any()
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objects = command.relations!!.researchFields
            )
        }
        verify(exactly = 1) {
            statementCollectionPropertyUpdater.update(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objects = command.relations!!.researchProblems
            )
        }
        verify(exactly = 1) {
            singleStatementPropertyUpdater.updateOptionalProperty(
                statements = statements,
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectId = command.relations!!.predicate
            )
        }
    }
}
