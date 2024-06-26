package org.orkg.graph.domain

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalPredicateService
import org.orkg.graph.output.ExternalResourceService

open class ImportServiceUnitTests {
    protected val externalClassService = mockk<ExternalClassService> {
        every { this@mockk.supportsMultipleOntologies() } returns false
    }
    protected val externalResourceService = mockk<ExternalResourceService> {
        every { this@mockk.supportsMultipleOntologies() } returns false
    }
    protected val externalPredicateService = mockk<ExternalPredicateService> {
        every { this@mockk.supportsMultipleOntologies() } returns false
    }
    protected val statementService: StatementUseCases = mockk()
    protected val resourceService: ResourceUseCases = mockk()
    protected val classService: ClassUseCases = mockk()
    protected val predicateService: PredicateUseCases = mockk()
    protected val literalService: LiteralUseCases = mockk()
    protected val service = ImportService(
        externalClassRepositories = mutableListOf(externalClassService),
        externalResourceRepositories = mutableListOf(externalResourceService),
        externalPredicateRepositories = mutableListOf(externalPredicateService),
        statementService = statementService,
        resourceService = resourceService,
        classService = classService,
        predicateService = predicateService,
        literalService = literalService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(
            externalClassService,
            externalResourceService,
            externalPredicateService,
            statementService,
            resourceService,
            classService,
            predicateService,
            literalService
        )
    }

    protected fun mockDescriptionCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId
    ) {
        externalThing.description?.let { description ->
            val descriptionId = ThingId("newDescriptionId")
            every {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = description
                    )
                )
            } returns descriptionId
            every {
                statementService.add(
                    userId = contributorId,
                    subject = subjectId,
                    predicate = Predicates.description,
                    `object` = descriptionId
                )
            } just runs
        }
    }

    protected fun verifyDescriptionCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId
    ) {
        externalThing.description?.let { description ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = description
                    )
                )
            }
            verify(exactly = 1) {
                statementService.add(
                    userId = contributorId,
                    subject = subjectId,
                    predicate = Predicates.description,
                    `object` = ThingId("newDescriptionId")
                )
            }
        }
    }

    protected fun mockSameAsLiteralCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId
    ) {
        val sameAsLiteralId = ThingId("newSameAsLiteralId")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.uri.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns sameAsLiteralId
        every {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sameAs,
                `object` = sameAsLiteralId
            )
        } just runs
    }

    protected fun verifySameAsLiteralCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId
    ) {
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = externalThing.uri.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.sameAs,
                `object` = ThingId("newSameAsLiteralId")
            )
        }
    }
}
