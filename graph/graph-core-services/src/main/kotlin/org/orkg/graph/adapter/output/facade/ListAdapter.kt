package org.orkg.graph.adapter.output.facade

import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class ListAdapter(
    private val thingRepository: ThingRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ListRepository {
    override fun save(list: List, contributorId: ContributorId) {
        val listResource = Resource(
            id = list.id,
            classes = setOf(Classes.list),
            label = list.label,
            createdAt = list.createdAt,
            createdBy = list.createdBy,
            modifiable = list.modifiable
        )
        resourceRepository.save(listResource)
        val existingStatements = statementRepository.findAll(
            subjectId = list.id,
            predicateId = Predicates.hasListElement,
            pageable = PageRequests.ALL
        )
            .filter { it.index != null }
            .sortedBy { it.index }
        val toRemove = mutableSetOf<StatementId>()
        val toAdd = list.elements.mapIndexed { index, element -> element to index }.toMap(LinkedHashMap())
        existingStatements.forEachIndexed { index, existingStatement ->
            if (index >= list.elements.size) {
                toRemove += existingStatement.id
            } else {
                val target = list.elements[index]
                if (target != existingStatement.`object`.id) {
                    toRemove += existingStatement.id
                } else {
                    toAdd.remove(target)
                }
            }
        }
        if (toAdd.isNotEmpty()) {
            val predicate = predicateRepository.findById(Predicates.hasListElement)
                .orElseThrow { IllegalStateException("""Cannot find built-in predicate "${Predicates.hasListElement}". This is a bug!""") }
            val statements = toAdd.mapTo(mutableSetOf()) { entry ->
                GeneralStatement(
                    id = statementRepository.nextIdentity(),
                    subject = listResource,
                    predicate = predicate,
                    `object` = thingRepository.findByThingId(entry.key).get(),
                    createdAt = OffsetDateTime.now(clock),
                    createdBy = contributorId,
                    index = entry.value
                )
            }
            statementRepository.saveAll(statements)
        }
        if (toRemove.isNotEmpty()) {
            statementRepository.deleteByStatementIds(toRemove)
        }
    }

    override fun findById(id: ThingId): Optional<List> =
        resourceRepository.findById(id)
            .filter { Classes.list in it.classes }
            .map { it.toList() }

    override fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing> =
        statementRepository.findAll(
            subjectId = id,
            predicateId = Predicates.hasListElement,
            pageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by("index"))
        ).map { it.`object` }

    override fun nextIdentity(): ThingId = resourceRepository.nextIdentity()

    override fun exists(id: ThingId): Boolean =
        resourceRepository.findById(id)
            .filter { Classes.list in it.classes }
            .isPresent

    override fun delete(id: ThingId) {
        if (exists(id)) {
            resourceRepository.deleteById(id)
        }
    }

    private fun Resource.toList(): List = List(
        id = id,
        label = label,
        elements = statementRepository.findAll(
            subjectId = id,
            predicateId = Predicates.hasListElement,
            pageable = PageRequests.ALL
        )
            .filter { it.index != null }
            .sortedBy { it.index }
            .map { it.`object`.id },
        createdAt = createdAt,
        createdBy = createdBy,
        modifiable = modifiable
    )
}
