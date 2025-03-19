package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreateThingsCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class ContributionCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val subgraphCreator: SubgraphCreator,
) {
    constructor(
        unsafeClassUseCases: UnsafeClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        unsafeResourceUseCases,
        unsafeStatementUseCases,
        SubgraphCreator(
            unsafeClassUseCases,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    internal fun create(
        paperId: ThingId,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        thingsCommand: CreateThingsCommand,
        contributionCommands: List<CreateContributionCommandPart>,
        validatedIds: Map<String, Either<String, Thing>>,
        bakedStatements: Set<BakedStatement>,
    ): List<ThingId> {
        val contributionLookup = mutableMapOf<String, ThingId>()
        val contributions = contributionCommands.mapIndexed { index, contribution ->
            val contributionId = unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = contribution.label,
                    classes = contribution.classes + Classes.contribution,
                    extractionMethod = extractionMethod
                )
            )
            contributionLookup["^$index"] = contributionId
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = paperId,
                    predicateId = Predicates.hasContribution,
                    objectId = contributionId
                )
            )
            contributionId
        }

        subgraphCreator.createThingsAndStatements(
            contributorId = contributorId,
            extractionMethod = extractionMethod,
            thingsCommand = thingsCommand,
            validatedIds = validatedIds,
            bakedStatements = bakedStatements,
            lookup = contributionLookup
        )

        return contributions
    }
}
