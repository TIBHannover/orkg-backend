package org.orkg

import java.net.URI
import java.util.*
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.OrganizationType
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.mediastorage.domain.ImageId

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
        id = id?.let(::ThingId),
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
) = this.create(
    CreateObservatoryUseCase.CreateCommand(
        id = id,
        name = name,
        description = description,
        organizationId = organizationId,
        researchField = researchField,
        displayId = displayId
    )
)

fun ListUseCases.createList(
    label: String,
    elements: List<ThingId>,
    id: ThingId? = null,
    contributorId: ContributorId? = null,
) = create(CreateListUseCase.CreateCommand(label, elements, id, contributorId))

private fun String.toDisplayId() = this.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
