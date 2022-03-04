package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.DetailsPerResource
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val resourceRepository: ResourceRepository,
    private val resourceService: ResourceUseCases
) : ObservatoryService {
    override fun create(name: String, description: String, organization: Organization, researchField: String, displayId: String): Observatory {
        val oId = UUID.randomUUID()
        val org = postgresOrganizationRepository
            .findById(organization.id!!.value)
            .orElseThrow { OrganizationNotFound(organization.id) } // FIXME: should always have an ID
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
            this.researchField = researchField
            organizations = mutableSetOf(org)
            this.displayId = displayId
        }

        val response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return expand(response)
    }

    override fun listObservatories(): List<Observatory> =
        postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
            }

    override fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory> =
        postgresObservatoryRepository.findByorganizationsId(id.value)
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
        }

    override fun findByName(name: String): Optional<Observatory> {
        val response = postgresObservatoryRepository
            .findByName(name)
            .map(ObservatoryEntity::toObservatory)!!
        return if (response.isPresent && hasResearchField(response.get()))
            Optional.of(response.get().withResearchField(response.get().researchField?.id!!))
        else response
    }

    override fun findById(id: ObservatoryId): Optional<Observatory> {
        val response = postgresObservatoryRepository.findById(id.value).map(ObservatoryEntity::toObservatory).get()
        return if (hasResearchField(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
    }

    override fun findByDisplayId(id: String): Optional<Observatory> {
        val response = postgresObservatoryRepository.findByDisplayId(id).map(ObservatoryEntity::toObservatory)
        return if (response.isPresent && hasResearchField(response.get()))
            Optional.of(response.get().withResearchField(response.get().researchField?.id!!))
        else response
    }

    override fun findObservatoriesByResearchField(researchField: String): List<Observatory> {
        val response = postgresObservatoryRepository.findByResearchField(researchField).map(ObservatoryEntity::toObservatory)

        response.forEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
        }
        return response
    }

    override fun findMultipleClassesByObservatoryId(
        id: ObservatoryId,
        classes: List<String>,
        featured: Boolean?,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerResource> {
        val resultList = mutableListOf<DetailsPerResource>()

        when (featured) {
            null -> getListWithoutFeaturedFlag(classes, id, unlisted, pageable, resultList)
            else -> getListWithFlags(classes, id, featured, unlisted, pageable, resultList)
        }

        resultList.sortBy(DetailsPerResource::createdAt)

        return PageImpl(resultList, pageable, resultList.size.toLong())
    }

    override fun removeAll() = postgresObservatoryRepository.deleteAll()

    override fun changeName(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            name = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun changeDescription(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            description = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun changeResearchField(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            researchField = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    fun hasResearchField(response: Observatory): Boolean {
        return response.researchField?.id !== null
    }

    fun Observatory.withResearchField(resourceId: String) = this.apply {
        val resource = resourceService.findById(ResourceId(resourceId))
        researchField?.id = resource.get().id.toString()
        researchField?.label = resource.get().label
    }

    private fun expand(response: Observatory): Observatory =
        if (hasResearchField(response))
            response.withResearchField(response.researchField?.id!!)
        else response

    private fun getListWithoutFeaturedFlag(
        classesList: List<String>,
        id: ObservatoryId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(resourceRepository.findPapersByObservatoryId(id, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(resourceRepository.findComparisonsByObservatoryId(id, unlisted, pageable).content)
                else -> {
                    resultList.addAll(resourceRepository.findProblemsByObservatoryId(id, unlisted, pageable).content)
                }
            }
        }
    }

    private fun getListWithFlags(
        classesList: List<String>,
        id: ObservatoryId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerResource>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(resourceRepository.findPapersByObservatoryId(id, featured, unlisted, pageable).content)
                "COMPARISON" -> resultList.addAll(resourceRepository.findComparisonsByObservatoryId(id, featured, unlisted, pageable).content)
                else -> {
                    resultList.addAll(resourceRepository.findProblemsByObservatoryId(id, featured, unlisted, pageable).content)
                }
            }
        }
    }
}
