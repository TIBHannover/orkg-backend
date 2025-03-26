package org.orkg.graph.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ExternalClassService
import org.orkg.graph.output.ExternalPredicateService
import org.orkg.graph.output.ExternalResourceService

abstract class AbstractImportServiceUnitTest : MockkBaseTest {
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
    protected val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()
    protected val resourceService: ResourceUseCases = mockk()
    protected val classService: ClassUseCases = mockk()
    protected val predicateService: PredicateUseCases = mockk()
    protected val literalService: LiteralUseCases = mockk()
    protected val service = ImportService(
        externalClassRepositories = mutableListOf(externalClassService),
        externalResourceRepositories = mutableListOf(externalResourceService),
        externalPredicateRepositories = mutableListOf(externalPredicateService),
        statementService = statementService,
        unsafeStatementService = unsafeStatementUseCases,
        resourceService = resourceService,
        classService = classService,
        predicateService = predicateService,
        literalService = literalService
    )

    protected fun mockDescriptionCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId,
    ) {
        externalThing.description?.also { description ->
            val descriptionId = ThingId("newDescriptionId")
            val statementId = StatementId("SnewDescription")
            every {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = description
                    )
                )
            } returns descriptionId
            every {
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = subjectId,
                        predicateId = Predicates.description,
                        objectId = descriptionId
                    )
                )
            } returns statementId
        }
    }

    protected fun verifyDescriptionCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId,
    ) {
        externalThing.description?.also { description ->
            verify(exactly = 1) {
                literalService.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = description
                    )
                )
            }
            verify(exactly = 1) {
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = subjectId,
                        predicateId = Predicates.description,
                        objectId = ThingId("newDescriptionId")
                    )
                )
            }
        }
    }

    protected fun mockSameAsLiteralCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId,
    ) {
        val sameAsLiteralId = ThingId("newSameAsLiteralId")
        val statementId = StatementId("SsameAsLiteralId")
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
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.sameAs,
                    objectId = sameAsLiteralId
                )
            )
        } returns statementId
    }

    protected fun verifySameAsLiteralCreation(
        contributorId: ContributorId,
        externalThing: ExternalThing,
        subjectId: ThingId,
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
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.sameAs,
                    objectId = ThingId("newSameAsLiteralId")
                )
            )
        }
    }
}
