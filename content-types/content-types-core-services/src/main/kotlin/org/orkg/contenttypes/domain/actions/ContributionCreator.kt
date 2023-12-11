package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.StatementRepository

abstract class ContributionCreator(
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases
) {
    internal fun create(
        paperId: ThingId,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        contents: CreatePaperUseCase.CreateCommand.PaperContents,
        validatedIds: Map<String, Either<String, Thing>>,
        bakedStatements: Set<BakedStatement>
    ): List<ThingId> {
        val lookup = mutableMapOf<String, ThingId>()
        contents.resources.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                lookup[it.key] = resourceService.create(
                    CreateResourceUseCase.CreateCommand(
                        label = it.value.label,
                        classes = it.value.classes,
                        contributorId = contributorId
                    )
                )
            }
        }
        contents.literals.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                lookup[it.key] = literalService.create(
                    CreateCommand(
                        contributorId = contributorId,
                        label = it.value.label,
                        datatype = it.value.dataType
                    )
                )
            }
        }
        contents.predicates.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                val predicate = predicateService.create(
                    CreatePredicateUseCase.CreateCommand(
                        label = it.value.label,
                        contributorId = contributorId
                    )
                )
                lookup[it.key] = predicate
                if (it.value.description != null) {
                    val description = literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = it.value.label
                        )
                    )
                    statementService.add(
                        userId = contributorId,
                        subject = predicate,
                        predicate = Predicates.description,
                        `object` = description
                    )
                }
            }
        }
        val lists = contents.lists.filter { it.key.isTempId && it.key in validatedIds }
        // create all lists without contents first, so other lists can reference them
        lists.forEach {
            lookup[it.key] = listService.create(
                CreateListUseCase.CreateCommand(
                    label = it.value.label,
                    elements = emptyList(),
                    contributorId = contributorId
                )
            )
        }
        lists.forEach {
            listService.update(
                lookup[it.key]!!,
                UpdateListUseCase.UpdateCommand(
                    elements = it.value.elements.map { id -> resolve(id, lookup) },
                )
            )
        }
        val contributions = contents.contributions.mapIndexed { index, contribution ->
            val contributionId = resourceService.create(
                CreateResourceUseCase.CreateCommand(
                    label = contribution.label,
                    classes = contribution.classes + Classes.contribution,
                    contributorId = contributorId,
                    extractionMethod = extractionMethod
                )
            )
            lookup["^$index"] = contributionId
            statementService.add(
                userId = contributorId,
                subject = paperId,
                predicate = Predicates.hasContribution,
                `object` = contributionId
            )
            contributionId
        }
        bakedStatements.forEach { (subjectId, predicateId, objectId) ->
            val subject = resolve(subjectId, lookup)
            val predicate = resolve(predicateId, lookup)
            val `object` = resolve(objectId, lookup)
            val hasTempId = subjectId.isTempId || predicateId.isTempId || objectId.isTempId
            if (hasTempId || statementRepository.findBySubjectIdAndPredicateIdAndObjectId(subject, predicate, `object`).isEmpty) {
                statementService.add(contributorId, subject, predicate, `object`)
            }
        }
        return contributions
    }

    private fun resolve(id: String, lookup: Map<String, ThingId>): ThingId =
        if (id.isTempId) lookup[id]!! else ThingId(id)
}
