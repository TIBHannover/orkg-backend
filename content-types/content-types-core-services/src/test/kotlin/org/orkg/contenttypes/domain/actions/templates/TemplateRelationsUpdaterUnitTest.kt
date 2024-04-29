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
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.TemplateRelations
import org.orkg.contenttypes.domain.actions.UpdateTemplateState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.input.TemplateRelationsDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTemplateCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class TemplateRelationsUpdaterUnitTest {
    private val statementUseCases: StatementUseCases = mockk()
    private val templateRelationsCreator: TemplateRelationsCreator = mockk()

    private val templateRelationsUpdater = TemplateRelationsUpdater(statementUseCases, templateRelationsCreator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementUseCases, templateRelationsCreator)
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
    fun `Given a template update command, when updating research fields with the same value, it does nothing`() {
        val relations = TemplateRelations(
            researchFields = listOf(
                ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                ObjectIdAndLabel(ThingId("R456"), "irrelevant")
            )
        )
        val template = createDummyTemplate().copy(
            relations = relations
        )
        val command = dummyUpdateTemplateCommand().copy(
            relations = relations.toTemplateRelationsDefinition()
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
    fun `Given a template update command, when updating research fields with an empty list, it deletes the old statements`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchFields = emptyList()
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfResearchField),
                `object` = createResource(
                    id = template.relations.researchFields.first().id,
                    classes = setOf(Classes.researchField)
                )
            )
        )
        every { statementUseCases.delete(setOf(statementId)) } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementUseCases.delete(setOf(statementId)) }
    }

    @Test
    fun `Given a template update command, when updating research fields with a new value, it creates a new statement`() {
        val template = createDummyTemplate().copy(relations = TemplateRelations())
        val researchField = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchFields = listOf(researchField)
            )
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            templateRelationsCreator.linkResearchFields(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchFields = listOf(researchField)
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            templateRelationsCreator.linkResearchFields(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchFields = listOf(researchField)
            )
        }
    }

    @Test
    fun `Given a template update command, when updating research fields with a new value, it replaces the old statement`() {
        val template = createDummyTemplate()
        val researchField = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchFields = listOf(researchField)
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfResearchField),
                `object` = createResource(
                    id = template.relations.researchFields.first().id,
                    classes = setOf(Classes.researchField)
                )
            )
        )
        every { statementUseCases.delete(setOf(statementId)) } just runs
        every {
            templateRelationsCreator.linkResearchFields(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchFields = listOf(researchField)
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchField,
                objectClasses = setOf(Classes.researchField),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementUseCases.delete(setOf(statementId)) }
        verify(exactly = 1) {
            templateRelationsCreator.linkResearchFields(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchFields = listOf(researchField)
            )
        }
    }

    @Test
    fun `Given a template update command, when updating research problems with the same value, it does nothing`() {
        val relations = TemplateRelations(
            researchProblems = listOf(
                ObjectIdAndLabel(ThingId("R123"), "irrelevant"),
                ObjectIdAndLabel(ThingId("R456"), "irrelevant")
            )
        )
        val template = createDummyTemplate().copy(
            relations = relations
        )
        val command = dummyUpdateTemplateCommand().copy(
            relations = relations.toTemplateRelationsDefinition()
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
    fun `Given a template update command, when updating research problems with an empty list, it deletes the old statements`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchProblems = emptyList()
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objectClasses = setOf(Classes.problem),
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfResearchProblem),
                `object` = createResource(
                    id = template.relations.researchProblems.first().id,
                    classes = setOf(Classes.problem)
                )
            )
        )
        every { statementUseCases.delete(setOf(statementId)) } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objectClasses = setOf(Classes.problem),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementUseCases.delete(setOf(statementId)) }
    }

    @Test
    fun `Given a template update command, when updating research problems with a new value, it creates a new statement`() {
        val template = createDummyTemplate().copy(relations = TemplateRelations())
        val researchProblem = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchProblems = listOf(researchProblem)
            )
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            templateRelationsCreator.linkResearchProblems(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchProblems = listOf(researchProblem)
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            templateRelationsCreator.linkResearchProblems(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchProblems = listOf(researchProblem)
            )
        }
    }

    @Test
    fun `Given a template update command, when updating research problems with a new value, it replaces the old statement`() {
        val template = createDummyTemplate()
        val researchProblem = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                researchProblems = listOf(researchProblem)
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objectClasses = setOf(Classes.problem),
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfResearchProblem),
                `object` = createResource(
                    id = template.relations.researchProblems.first().id,
                    classes = setOf(Classes.problem)
                )
            )
        )
        every { statementUseCases.delete(setOf(statementId)) } just runs
        every {
            templateRelationsCreator.linkResearchProblems(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchProblems = listOf(researchProblem)
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfResearchProblem,
                objectClasses = setOf(Classes.problem),
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) { statementUseCases.delete(setOf(statementId)) }
        verify(exactly = 1) {
            templateRelationsCreator.linkResearchProblems(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                researchProblems = listOf(researchProblem)
            )
        }
    }

    @Test
    fun `Given a template update command, when updating predicate with the same value, it does nothing`() {
        val relations = TemplateRelations(
            predicate = ObjectIdAndLabel(ThingId("P456"), "irrelevant")
        )
        val template = createDummyTemplate().copy(
            relations = relations
        )
        val command = dummyUpdateTemplateCommand().copy(
            relations = relations.toTemplateRelationsDefinition()
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
    fun `Given a template update command, when updating predicate with an empty list, it deletes the old statements`() {
        val template = createDummyTemplate()
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                predicate = null
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectClasses = setOf(Classes.predicate),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfPredicate),
                `object` = createPredicate(template.relations.predicate!!.id)
            )
        )
        every { statementUseCases.delete(statementId) } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectClasses = setOf(Classes.predicate),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementUseCases.delete(statementId) }
    }

    @Test
    fun `Given a template update command, when updating predicate with a new value, it creates a new statement`() {
        val template = createDummyTemplate().copy(relations = TemplateRelations())
        val predicate = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                predicate = predicate
            )
        )
        val state = UpdateTemplateState(
            template = template
        )

        every {
            templateRelationsCreator.linkPredicate(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = predicate
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            templateRelationsCreator.linkPredicate(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = predicate
            )
        }
    }

    @Test
    fun `Given a template update command, when updating predicate with a new value, it replaces the old statement`() {
        val template = createDummyTemplate()
        val predicate = ThingId("R213548")
        val command = dummyUpdateTemplateCommand().copy(
            relations = template.relations.toTemplateRelationsDefinition().copy(
                predicate = predicate
            )
        )
        val state = UpdateTemplateState(
            template = template
        )
        val statementId = StatementId("S1")

        every {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectClasses = setOf(Classes.predicate),
                pageable = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                id = statementId,
                subject = createResource(command.templateId),
                predicate = createPredicate(Predicates.templateOfPredicate),
                `object` = createPredicate(template.relations.predicate!!.id)
            )
        )
        every { statementUseCases.delete(statementId) } just runs
        every {
            templateRelationsCreator.linkPredicate(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = predicate
            )
        } just runs

        val result = templateRelationsUpdater(command, state)

        result.asClue {
            it.template shouldBe template
        }

        verify(exactly = 1) {
            statementUseCases.findAll(
                subjectId = command.templateId,
                predicateId = Predicates.templateOfPredicate,
                objectClasses = setOf(Classes.predicate),
                pageable = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { statementUseCases.delete(statementId) }
        verify(exactly = 1) {
            templateRelationsCreator.linkPredicate(
                contributorId = command.contributorId,
                subjectId = command.templateId,
                predicateId = predicate
            )
        }
    }

    private fun TemplateRelations.toTemplateRelationsDefinition(): TemplateRelationsDefinition =
        TemplateRelationsDefinition(
            researchFields = researchFields.map { it.id },
            researchProblems = researchProblems.map { it.id },
            predicate = predicate?.id
        )
}
