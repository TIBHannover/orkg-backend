package org.orkg.contenttypes.domain.actions

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ThingDefinitions
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateClassUseCase
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

class SubgraphCreator(
    private val classService: ClassUseCases,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val predicateService: PredicateUseCases,
    private val statementRepository: StatementRepository,
    private val listService: ListUseCases
) {
    internal fun createThingsAndStatements(
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        bakedStatements: Set<BakedStatement>,
        lookup: MutableMap<String, ThingId> = mutableMapOf()
    ) {
        createThings(thingDefinitions, validatedIds, contributorId, extractionMethod, lookup)
        createStatements(bakedStatements, lookup, contributorId)
    }

    internal fun createThings(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod,
        lookup: MutableMap<String, ThingId> = mutableMapOf()
    ) {
        createClasses(thingDefinitions, validatedIds, lookup, contributorId)
        createResources(thingDefinitions, validatedIds, lookup, contributorId, extractionMethod)
        createLiterals(thingDefinitions, validatedIds, lookup, contributorId)
        createPredicates(thingDefinitions, validatedIds, contributorId, lookup)
        createLists(thingDefinitions, validatedIds, lookup, contributorId)
    }

    private fun createClasses(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId
    ) {
        thingDefinitions.classes.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                lookup[it.key] = classService.create(
                    CreateClassUseCase.CreateCommand(
                        label = it.value.label,
                        contributorId = contributorId,
                        uri = it.value.uri
                    )
                )
            }
        }
    }

    private fun createResources(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId,
        extractionMethod: ExtractionMethod
    ) {
        thingDefinitions.resources.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                lookup[it.key] = resourceService.createUnsafe(
                    CreateResourceUseCase.CreateCommand(
                        label = it.value.label,
                        classes = it.value.classes,
                        contributorId = contributorId,
                        extractionMethod = extractionMethod
                    )
                )
            }
        }
    }

    private fun createLiterals(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId
    ) {
        thingDefinitions.literals.forEach {
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
    }

    private fun createPredicates(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        contributorId: ContributorId,
        lookup: MutableMap<String, ThingId>
    ) {
        thingDefinitions.predicates.forEach {
            if (it.key.isTempId && it.key in validatedIds) {
                val predicate = predicateService.create(
                    CreatePredicateUseCase.CreateCommand(
                        label = it.value.label,
                        contributorId = contributorId
                    )
                )
                lookup[it.key] = predicate
                it.value.description?.let { description ->
                    val literal = literalService.create(
                        CreateCommand(
                            contributorId = contributorId,
                            label = description
                        )
                    )
                    statementService.add(
                        userId = contributorId,
                        subject = predicate,
                        predicate = Predicates.description,
                        `object` = literal
                    )
                }
            }
        }
    }

    private fun createLists(
        thingDefinitions: ThingDefinitions,
        validatedIds: Map<String, Either<String, Thing>>,
        lookup: MutableMap<String, ThingId>,
        contributorId: ContributorId
    ) {
        val lists = thingDefinitions.lists.filter { it.key.isTempId && it.key in validatedIds }
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
        contributorId: ContributorId
    ) {
        bakedStatements.forEach { (subjectId, predicateId, objectId) ->
            val subject = resolve(subjectId, lookup)
            val predicate = resolve(predicateId, lookup)
            val `object` = resolve(objectId, lookup)
            val hasTempId = subjectId.isTempId || predicateId.isTempId || objectId.isTempId
            if (hasTempId || statementRepository.findAll(subjectId = subject, predicateId = predicate, objectId = `object`, pageable = PageRequests.SINGLE).isEmpty) {
                statementService.add(contributorId, subject, predicate, `object`)
            }
        }
    }

    private fun resolve(id: String, lookup: Map<String, ThingId>): ThingId =
        if (id.isTempId) lookup[id]!! else ThingId(id)
}
