package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchFieldService {

    fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any>

    fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor>

    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>
}
