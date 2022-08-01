package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class InMemoryThingRepository(
    // These are required to delegate to, because we do not have a shared database.
    // The injected collaborators should be in-memory implementations as well.
    private val classRepository: ClassRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val literalRepository: LiteralRepository,
) : ThingRepository {
    override fun findByThingId(id: String?): Optional<Thing> = Optional.ofNullable(
        listOfNotNull(
            classRepository.findByClassId(ClassId(id!!)).orElse(null),
            predicateRepository.findByPredicateId(PredicateId(id)).orElse(null),
            resourceRepository.findByResourceId(ResourceId(id)).orElse(null),
            literalRepository.findByLiteralId(LiteralId(id)).orElse(null),
        ).singleOrNull()
    )

    override fun findAll(): Iterable<Thing> {
        TODO("Not yet implemented")
    }

    override fun findAll(pageable: Pageable): Page<Thing> {
        TODO("Not yet implemented")
    }
}
