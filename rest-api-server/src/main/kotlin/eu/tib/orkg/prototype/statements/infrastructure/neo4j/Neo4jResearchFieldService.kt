package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.auth.persistence.ORKGUserEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.OrkgUserRepository
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.domain.model.ResearchField
import eu.tib.orkg.prototype.statements.domain.model.ResearchFieldService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.ports.ResearchFieldRepository
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jResearchFieldService(
    private val researchFieldRepository: ResearchFieldRepository,
    private val userRepository: UserRepository,
    private val orkgUserRepository: OrkgUserRepository
) : ResearchFieldService, RetrieveResearchFieldUseCase {
    override fun findById(id: ResourceId): Optional<Resource> =
        researchFieldRepository.findById(id)

    override fun getResearchProblemsOfField(id: ResourceId, pageable: Pageable): Page<Any> {
        return researchFieldRepository.getResearchProblemsOfField(id, pageable)
    }

    override fun getResearchProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getResearchProblemsIncludingSubFields(id, pageable)

    override fun getContributorsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable)
            .map(ContributorId::value)
        //return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
        return PageImpl(orkgUserRepository.findUserListInOldIDOrKeycloakID(contributors.content.toTypedArray()).map(ORKGUserEntity::toContributor))
    }

    override fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getPapersIncludingSubFields(id, pageable)

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getComparisonsIncludingSubFields(id, pageable)

    override fun getContributorsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Contributor> {
        val contributors =
            researchFieldRepository.getContributorIdsExcludingSubFields(id, pageable).map(ContributorId::value)
        //return PageImpl(userRepository.findByIdIn(contributors.content.toTypedArray()).map(UserEntity::toContributor))
        return PageImpl(orkgUserRepository.findUserListInOldIDOrKeycloakID(contributors.content.toTypedArray()).map(ORKGUserEntity::toContributor))
    }

    override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getPapersExcludingSubFields(id, pageable)

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getComparisonsExcludingSubFields(id, pageable)

    override fun getResearchProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        researchFieldRepository.getResearchProblemsExcludingSubFields(id, pageable)

    override fun withBenchmarks(): List<ResearchField> =
        researchFieldRepository.findResearchFieldsWithBenchmarks()
            .map { ResearchField(it.id!!.value, it.label) }
}
