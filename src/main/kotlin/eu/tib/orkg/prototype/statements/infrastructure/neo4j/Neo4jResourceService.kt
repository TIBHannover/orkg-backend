package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.*
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.*

@Service
@Transactional
class Neo4jResourceService(
    private val neo4jResourceRepository: Neo4jResourceRepository
) : ResourceService {

    override fun create(label: String): Resource {
        return neo4jResourceRepository.save(Neo4jResource(label = label))
            .toResource()
    }

    override fun findAll() =
        neo4jResourceRepository.findAll()
            .map(Neo4jResource::toResource)

    override fun findAllByLabel(label: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabel(label)
            .map(Neo4jResource::toResource)

    override fun findAllByLabelContaining(part: String): Iterable<Resource> =
        neo4jResourceRepository.findAllByLabelContaining(part)
            .map(Neo4jResource::toResource)
}
