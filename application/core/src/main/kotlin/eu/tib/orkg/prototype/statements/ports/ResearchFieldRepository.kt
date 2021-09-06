package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ResearchFieldRepository : FindResearchFieldsQuery {

    fun findById(id: ResourceId): Optional<Resource>

    fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any>

    fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource>

    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    fun getContributorIdsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    fun findResearchFieldsWithBenchmarks(): Iterable<Resource>
}
