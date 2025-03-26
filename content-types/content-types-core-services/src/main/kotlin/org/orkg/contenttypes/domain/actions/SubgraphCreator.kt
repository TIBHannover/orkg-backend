package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.CreateThingsCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.input.UpdateListUseCase
import org.orkg.graph.output.StatementRepository

class SubgraphCreator(
    private val unsafeClassUseCases: UnsafeClassUseCases,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases,
) {
    internal fun createThingsAndStatements(
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        bakedStatements: Set<BakedStatement>,
        lookup: MutableMap<String, ThingId> = mutableMapOf(),
    ) {
        createThings(thingsCommand, validationCache, contributorId, extractionMethod, lookup)
        createStatements(bakedStatements, lookup, contributorId)
    }

    internal fun createThings(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        lookup: MutableMap<String, ThingId> = mutableMapOf(),
    ) {
        createClasses(thingsCommand, validationCache, lookup, contributorId)
        createResources(thingsCommand, validationCache, lookup, contributorId, extractionMethod)
        createLiterals(thingsCommand, validationCache, lookup, contributorId)
        createPredicates(thingsCommand, validationCache, contributorId, lookup)
        createLists(thingsCommand, validationCache, lookup, contributorId)
    }

    private fun createClasses(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
    ) {
        thingsCommand.classes.forEach {
            if (it.key.isTempId && it.key in validationCache) {
                lookup[it.key] = unsafeClassUseCases.create(
                    CreateClassUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = it.value.label,
                        uri = it.value.uri
                    )
                )
            }
        }
    }

    private fun createResources(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
    ) {
        thingsCommand.resources.forEach {
            if (it.key.isTempId && it.key in validationCache) {
                lookup[it.key] = unsafeResourceUseCases.create(
                    CreateResourceUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = it.value.label,
                        classes = it.value.classes,
                        extractionMethod = extractionMethod
                    )
                )
            }
        }
    }

    private fun createLiterals(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
    ) {
        thingsCommand.literals.forEach {
            if (it.key.isTempId && it.key in validationCache) {
                lookup[it.key] = unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = it.value.label,
                        datatype = it.value.dataType
                    )
                )
            }
        }
    }

    private fun createPredicates(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        contributorId: ContributorId,
        lookup: MutableMap<String, ThingId>,
    ) {
        thingsCommand.predicates.forEach {
            if (it.key.isTempId && it.key in validationCache) {
                val predicate = unsafePredicateUseCases.create(
                    CreatePredicateUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = it.value.label
                    )
                )
                lookup[it.key] = predicate
                it.value.description?.also { description ->
                    val literal = unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = description
                        )
                    )
                    unsafeStatementUseCases.create(
                        CreateStatementUseCase.CreateCommand(
                            contributorId = contributorId,
                            subjectId = predicate,
                            predicateId = Predicates.description,
                            objectId = literal
                        )
                    )
                }
            }
        }
    }

    private fun createLists(
        thingsCommand: CreateThingsCommand,
        validationCache: Map<String, Either<CreateThingCommandPart, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
    ) {
        val lists = thingsCommand.lists.filter { it.key.isTempId && it.key in validationCache }
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
                UpdateListUseCase.UpdateCommand(
                    id = lookup[it.key]!!,
                    contributorId = contributorId,
                    elements = it.value.elements.map { id -> resolve(id, lookup) },
                )
            )
        }
    }

    private fun createStatements(
        bakedStatements: Set<BakedStatement>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
    ) {
        bakedStatements.forEach { (subjectId, predicateId, objectId) ->
            val subject = resolve(subjectId, lookup)
            val predicate = resolve(predicateId, lookup)
            val `object` = resolve(objectId, lookup)
            val hasTempId = subjectId.isTempId || predicateId.isTempId || objectId.isTempId
            if (hasTempId || statementRepository.findAll(subjectId = subject, predicateId = predicate, objectId = `object`, pageable = PageRequests.SINGLE).isEmpty) {
                unsafeStatementUseCases.create(
                    CreateStatementUseCase.CreateCommand(
                        contributorId = contributorId,
                        subjectId = subject,
                        predicateId = predicate,
                        objectId = `object`
                    )
                )
            }
        }
    }

    private fun resolve(id: String, lookup: Map<String, ThingId>): ThingId =
        if (id.isTempId) lookup[id]!! else ThingId(id)
}
