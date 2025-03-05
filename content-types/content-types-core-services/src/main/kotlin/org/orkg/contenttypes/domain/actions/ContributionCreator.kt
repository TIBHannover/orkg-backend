package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
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
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        unsafeResourceUseCases = unsafeResourceUseCases,
        unsafeStatementUseCases = unsafeStatementUseCases,
        subgraphCreator = SubgraphCreator(
            classService = classService,
            unsafeResourceUseCases = unsafeResourceUseCases,
            unsafeStatementUseCases = unsafeStatementUseCases,
            literalService = literalService,
            unsafePredicateUseCases = unsafePredicateUseCases,
            statementRepository = statementRepository,
            listService = listService
        )
    )

    internal fun create(
        paperId: ThingId,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        thingDefinitions: ThingDefinitions,
        contributionDefinitions: List<ContributionDefinition>,
        validatedIds: Map<String, Either<String, Thing>>,
        bakedStatements: Set<BakedStatement>,
    ): List<ThingId> {
        val contributionLookup = mutableMapOf<String, ThingId>()
        val contributions = contributionDefinitions.mapIndexed { index, contribution ->
            val contributionId = unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    label = contribution.label,
                    classes = contribution.classes + Classes.contribution,
                    contributorId = contributorId,
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
            thingDefinitions = thingDefinitions,
            validatedIds = validatedIds,
            bakedStatements = bakedStatements,
            lookup = contributionLookup
        )

        return contributions
    }
}
