package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.api.CreateClassUseCase
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
): ThingId =
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
): ThingId =
    this.create(CreatePredicateUseCase.CreateCommand(label, id, ContributorId.createUnknownContributor()))

// Resources

fun CreateResourceUseCase.createResource(
    classes: Set<String> = setOf(),
    id: String? = null,
    label: String? = null,
    extractionMethod: ExtractionMethod? = null
): ThingId = this.create(
    CreateResourceUseCase.CreateCommand(
        id = Optional.ofNullable(id).map(::ThingId).orElse(null),
        label = label ?: "label",
        classes = classes.map(::ThingId).toSet(),
        extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN
    )
)

fun CreateResourceUseCase.createResource(
    classes: Set<String> = setOf(),
    id: String? = null,
    label: String? = null,
    extractionMethod: ExtractionMethod? = null,
    userId: ContributorId = ContributorId.createUnknownContributor(),
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization()
): ThingId = this.create(
    CreateResourceUseCase.CreateCommand(
        id = Optional.ofNullable(id).map(::ThingId).orElse(null),
        label = label ?: "label",
        classes = classes.map(::ThingId).toSet(),
        extractionMethod = extractionMethod ?: ExtractionMethod.UNKNOWN,
        contributorId = userId,
        observatoryId = observatoryId,
        organizationId = organizationId,
    )
)

// Users

fun AuthUseCase.createUser(
    anEmail: String = "user@example.org",
    aPassword: String = "123456",
    aDisplayName: String = "Example User"
) = this.registerUser(anEmail, aPassword, aDisplayName)

// Organizations

fun OrganizationUseCases.createOrganization(
    createdBy: ContributorId,
    organizationName: String = "Test Organization",
    url: String = "https://www.example.org",
    displayId: String = organizationName.toDisplayId(),
    type: OrganizationType = OrganizationType.GENERAL,
    id: OrganizationId? = null,
    logoId: ImageId? = null
) = this.create(id, organizationName, createdBy, url, displayId, type, logoId)

// Observatories

fun ObservatoryUseCases.createObservatory(
    organizationId: OrganizationId,
    researchField: ThingId,
    name: String = "Test Observatory",
    description: String = "Example description",
    displayId: String = name.toDisplayId(),
    id: ObservatoryId? = null
) = this.create(id, name, description, organizationId, researchField, displayId)

private fun String.toDisplayId() = this.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
