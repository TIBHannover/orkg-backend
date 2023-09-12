package eu.tib.orkg.prototype.statements.adapter.output.facade

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
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
            createdBy = list.createdBy
        )
        resourceRepository.save(listResource)
        val existingStatements = statementRepository.findAllBySubjectAndPredicate(
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
                toRemove += existingStatement.id!!
            } else {
                val target = list.elements[index]
                if (target != existingStatement.`object`.id) {
                    toRemove += existingStatement.id!!
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
        resourceRepository.findById(id).map { it.toList() }

    override fun findAllElementsById(id: ThingId, pageable: Pageable): Page<Thing> =
        statementRepository.findAllBySubjectAndPredicate(
            subjectId = id,
            predicateId = Predicates.hasListElement,
            pageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by("index"))
        ).map { it.`object` }

    override fun nextIdentity(): ThingId = resourceRepository.nextIdentity()

    override fun exists(id: ThingId): Boolean = resourceRepository.exists(id)

    override fun delete(id: ThingId)  {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
        if (!statements.isEmpty) {
            statementRepository.deleteByStatementIds(statements.map { it.id!! }.toSet())
        }
        resourceRepository.deleteById(id)
    }

    private fun Resource.toList(): List = List(
        id = id,
        label = label,
        elements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .sortedBy { it.index }
            .map { it.`object`.id },
        createdAt = createdAt,
        createdBy = createdBy
    )
}
