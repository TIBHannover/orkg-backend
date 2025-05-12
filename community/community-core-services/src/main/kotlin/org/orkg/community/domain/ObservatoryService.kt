package org.orkg.community.domain

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.UpdateObservatoryUseCase
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.Resources
import org.orkg.graph.output.ResourceRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
@TransactionalOnJPA
class ObservatoryService(
    private val postgresObservatoryRepository: ObservatoryRepository,
    private val postgresOrganizationRepository: OrganizationRepository,
    private val resourceRepository: ResourceRepository,
) : ObservatoryUseCases {
    override fun create(command: CreateObservatoryUseCase.CreateCommand): ObservatoryId {
        postgresObservatoryRepository.findByName(command.name).ifPresent {
            throw ObservatoryAlreadyExists.withName(command.name)
        }
        postgresObservatoryRepository.findByDisplayId(command.displayId).ifPresent {
            throw ObservatoryAlreadyExists.withDisplayId(command.displayId)
        }
        command.organizations.forEach { organizationId ->
            postgresOrganizationRepository.findById(organizationId)
                .orElseThrow { OrganizationNotFound(organizationId) }
        }
        resourceRepository.findById(command.researchField)
            .filter { resource -> Classes.researchField in resource.classes }
            .orElseThrow { ResearchFieldNotFound(command.researchField) }
        command.sustainableDevelopmentGoals.forEach { sdgId ->
            if (sdgId !in Resources.sustainableDevelopmentGoals) {
                throw SustainableDevelopmentGoalNotFound(sdgId)
            }
        }
        val id = command.id
            ?.also { id -> postgresObservatoryRepository.findById(id).ifPresent { throw ObservatoryAlreadyExists.withId(id) } }
            ?: ObservatoryId(UUID.randomUUID())
        val observatory = Observatory(
            id = id,
            name = command.name,
            description = command.description,
            researchField = command.researchField,
            organizationIds = command.organizations,
            displayId = command.displayId,
            sustainableDevelopmentGoals = command.sustainableDevelopmentGoals
        )
        postgresObservatoryRepository.save(observatory)
        return id
    }

    override fun update(command: UpdateObservatoryUseCase.UpdateCommand) {
        if (command.hasNoContents()) return
        val observatory = postgresObservatoryRepository.findById(command.id)
            .orElseThrow { ObservatoryNotFound(command.id) }
        if (command.organizations != null && command.organizations != observatory.organizationIds) {
            command.organizations!!.forEach { organizationId ->
                postgresOrganizationRepository
                    .findById(organizationId)
                    .orElseThrow { OrganizationNotFound(organizationId) }
            }
        }
        if (command.researchField != null && command.researchField != observatory.researchField) {
            resourceRepository.findById(command.researchField!!)
                .filter { resource -> Classes.researchField in resource.classes }
                .orElseThrow { ResearchFieldNotFound(command.researchField!!) }
        }
        if (command.sustainableDevelopmentGoals != null && command.sustainableDevelopmentGoals != observatory.sustainableDevelopmentGoals) {
            command.sustainableDevelopmentGoals!!.forEach { sdgId ->
                if (sdgId !in Resources.sustainableDevelopmentGoals) {
                    throw SustainableDevelopmentGoalNotFound(sdgId)
                }
            }
        }
        val updated = observatory.copy(
            name = command.name ?: observatory.name,
            organizationIds = command.organizations ?: observatory.organizationIds,
            description = command.description ?: observatory.description,
            researchField = command.researchField ?: observatory.researchField,
            sustainableDevelopmentGoals = command.sustainableDevelopmentGoals ?: observatory.sustainableDevelopmentGoals
        )
        if (updated != observatory) {
            postgresObservatoryRepository.save(updated)
        }
    }

    override fun findAll(pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAll(pageable)

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAllByOrganizationId(id, pageable)

    override fun findAllResearchFields(pageable: Pageable): Page<ThingId> =
        postgresObservatoryRepository.findAllResearchFields(pageable)

    override fun findByName(name: String): Optional<Observatory> =
        postgresObservatoryRepository.findByName(name)

    override fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAllByNameContains(name, pageable)

    override fun findById(id: ObservatoryId): Optional<Observatory> =
        postgresObservatoryRepository.findById(id)

    override fun findByDisplayId(id: String): Optional<Observatory> =
        postgresObservatoryRepository.findByDisplayId(id)

    override fun findAllByResearchFieldId(researchFieldId: ThingId, pageable: Pageable): Page<Observatory> {
        resourceRepository.findById(researchFieldId)
            .filter { resource -> Classes.researchField in resource.classes }
            .orElseThrow { ResearchFieldNotFound(researchFieldId) }
        return postgresObservatoryRepository.findAllByResearchField(researchFieldId, pageable)
    }

    override fun deleteAll() = postgresObservatoryRepository.deleteAll()
}
