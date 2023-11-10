package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.UpdateListUseCase
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository

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
                    userId = contributorId,
                    label = it.value.label,
                    datatype = it.value.dataType
                ).id
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
                        userId = contributorId,
                        label = it.value.label,
                        datatype = Literals.XSD.STRING.prefixedUri
                    ).id
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
                    contributorId = contributorId
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
