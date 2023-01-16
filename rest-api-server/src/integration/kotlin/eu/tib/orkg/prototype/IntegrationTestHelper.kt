package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.CreateClassUseCase
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationService
import eu.tib.orkg.prototype.statements.domain.model.OrganizationType
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.net.URI
import java.util.*

// Classes

fun CreateClassUseCase.createClasses(vararg classes: Pair<String, String>) =
    classes.forEach {
        createClass(id = it.first, label = it.second)
    }

fun CreateClassUseCase.createClasses(vararg classes: String) =
    classes.forEach {
        createClass(id = it, label = it)
    }

fun CreateClassUseCase.createClass(
    label: String,
    id: String? = null,
    contributorId: ContributorId? = null,
    uri: URI? = null
): ClassId =
    this.create(CreateClassUseCase.CreateCommand(label, id, contributorId, uri))

// Predicates

fun CreatePredicateUseCase.createPredicates(vararg predicates: Pair<String, String>) =
    predicates.forEach {
        createPredicate(label = it.second, id = it.first)
    }

fun CreatePredicateUseCase.createPredicates(vararg predicates: String) =
    predicates.forEach {
        createPredicate(label = it, id = it)
    }

fun CreatePredicateUseCase.createPredicate(
    label: String,
    id: String? = null
): PredicateId =
    this.create(CreatePredicateUseCase.CreateCommand(label, id, ContributorId.createUnknownContributor()))

// Resources

fun CreateResourceUseCase.createResource(
    classes: Set<String> = setOf(),
    id: String? = null,
    label: String? = null,
    extractionMethod: ExtractionMethod? = null
): ResourceId {
    val request = CreateResourceRequest(
        id = Optional.ofNullable(id).map(::ResourceId).orElse(null),
        label = label ?: "label",
        classes = classes.map(::ClassId).toSet(),
        extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN
    )
    return this.create(request).id
}

fun CreateResourceUseCase.createResource(
    classes: Set<String> = setOf(),
    id: String? = null,
    label: String? = null,
    extractionMethod: ExtractionMethod? = null,
    userId: ContributorId = ContributorId.createUnknownContributor(),
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization()
): ResourceId {
    val request = CreateResourceRequest(
        id = Optional.ofNullable(id).map(::ResourceId).orElse(null),
        label = label ?: "label",
        classes = classes.map(::ClassId).toSet(),
        extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN
    )
    return this.create(userId, request, observatoryId, request.extractionMethod, organizationId).id
}

// Users

fun UserService.createUser(
    anEmail: String = "user@example.org",
    aPassword: String = "123456",
    aDisplayName: String = "Example User"
) = this.registerUser(anEmail, aPassword, aDisplayName)

// Organizations

fun OrganizationService.createOrganization(
    createdBy: ContributorId,
    organizationName: String = "Test Organization",
    url: String = "https://www.example.org",
    displayId: String = organizationName.toDisplayId(),
    type: OrganizationType = OrganizationType.GENERAL
) = this.create(organizationName, createdBy, url, displayId, type).id!!

// Observatories

fun ObservatoryUseCases.createObservatory(
    organizationId: OrganizationId,
    researchField: ResourceId,
    name: String = "Test Observatory",
    description: String = "Example description",
    displayId: String = name.toDisplayId()
) = this.create(name, description, organizationId, researchField, displayId).id!!

private fun String.toDisplayId() = this.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
