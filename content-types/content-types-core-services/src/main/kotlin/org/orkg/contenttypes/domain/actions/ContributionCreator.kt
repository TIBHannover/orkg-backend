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
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.StatementRepository

class ContributionCreator(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val subgraphCreator: SubgraphCreator
) {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        predicateService: PredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        unsafeResourceUseCases = unsafeResourceUseCases,
        statementService = statementService,
        subgraphCreator = SubgraphCreator(
            classService = classService,
            unsafeResourceUseCases = unsafeResourceUseCases,
            statementService = statementService,
            literalService = literalService,
            predicateService = predicateService,
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
        bakedStatements: Set<BakedStatement>
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
            statementService.add(
                userId = contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
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
