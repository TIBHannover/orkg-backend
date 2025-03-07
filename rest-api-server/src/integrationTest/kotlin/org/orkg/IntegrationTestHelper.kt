package org.orkg

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.OrganizationType
import org.orkg.community.domain.internal.MD5Hash
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.CreateContributorUseCase
import org.orkg.community.input.CreateObservatoryUseCase
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.mediastorage.domain.ImageId
import org.orkg.testing.MockUserId
import java.time.OffsetDateTime

// Classes

fun CreateClassUseCase.createClasses(vararg classes: ThingId) =
    classes.forEach { id -> createClass(id = id, label = id.value) }

fun CreateClassUseCase.createClass(
    label: String = "label",
    id: ThingId? = null,
    contributorId: ContributorId = ContributorId.UNKNOWN,
    uri: ParsedIRI? = null,
    modifiable: Boolean = true,
) = create(
    CreateClassUseCase.CreateCommand(
        id = id,
        contributorId = contributorId,
        label = label,
        uri = uri,
        modifiable = modifiable
    )
)

// Predicates

fun CreatePredicateUseCase.createPredicates(vararg predicates: ThingId) =
    predicates.forEach { id -> createPredicate(id = id, label = id.value) }

fun CreatePredicateUseCase.createPredicate(
    id: ThingId? = null,
    label: String = "label",
    contributorId: ContributorId = ContributorId.UNKNOWN,
    modifiable: Boolean = true,
): ThingId = create(
    CreatePredicateUseCase.CreateCommand(
        id = id,
        contributorId = contributorId,
        label = label,
        modifiable = modifiable
    )
)

// Resources

fun CreateResourceUseCase.createResource(
    classes: Set<ThingId> = setOf(),
    id: ThingId? = null,
    label: String = "label",
    extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    userId: ContributorId = ContributorId.UNKNOWN,
    observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.UNKNOWN,
    modifiable: Boolean = true,
): ThingId = create(
    CreateResourceUseCase.CreateCommand(
        id = id,
        contributorId = userId,
        label = label,
        classes = classes,
        extractionMethod = extractionMethod,
        observatoryId = observatoryId,
        organizationId = organizationId,
        modifiable = modifiable
    )
)

// Literals

fun CreateLiteralUseCase.createLiteral(
    id: ThingId? = null,
    label: String = "label",
    datatype: String = Literals.XSD.STRING.prefixedUri,
    contributorId: ContributorId = ContributorId.UNKNOWN,
    modifiable: Boolean = true,
): ThingId = create(
    CreateLiteralUseCase.CreateCommand(
        id = id,
        contributorId = contributorId,
        label = label,
        datatype = datatype,
        modifiable = modifiable,
    )
)

// Contributors

fun ContributorUseCases.createContributor(
    id: ContributorId = ContributorId(MockUserId.USER),
    name: String = "Example User",
    joinedAt: OffsetDateTime = OffsetDateTime.now(fixedClock),
    organizationId: OrganizationId = OrganizationId.UNKNOWN,
    observatoryId: ObservatoryId = ObservatoryId.UNKNOWN,
    emailMD5: MD5Hash = MD5Hash.fromEmail("user@example.org"),
    isCurator: Boolean = false,
    isAdmin: Boolean = false,
): ContributorId = create(
    CreateContributorUseCase.CreateCommand(
        id = id,
        name = name,
        joinedAt = joinedAt,
        organizationId = organizationId,
        observatoryId = observatoryId,
        emailMD5 = emailMD5,
        isCurator = isCurator,
        isAdmin = isAdmin
    )
)

// Organizations

fun OrganizationUseCases.createOrganization(
    createdBy: ContributorId,
    organizationName: String = "Test Organization",
    url: String = "https://www.example.org",
    displayId: String = organizationName.toDisplayId(),
    type: OrganizationType = OrganizationType.GENERAL,
    id: OrganizationId? = null,
    logoId: ImageId? = null,
) = create(
    id = id,
    organizationName = organizationName,
    createdBy = createdBy,
    url = url,
    displayId = displayId,
    type = type,
    logoId = logoId
)

// Observatories

fun ObservatoryUseCases.createObservatory(
    id: ObservatoryId? = null,
    name: String = "Test Observatory",
    description: String = "Example description",
    organizations: Set<OrganizationId> = emptySet(),
    researchField: ThingId = ThingId("R123"),
    displayId: String = name.toDisplayId(),
    sustainableDevelopmentGoals: Set<ThingId> = emptySet(),
) = create(
    CreateObservatoryUseCase.CreateCommand(
        id = id,
        name = name,
        description = description,
        organizations = organizations,
        researchField = researchField,
        displayId = displayId,
        sustainableDevelopmentGoals = sustainableDevelopmentGoals
    )
)

// Lists

fun ListUseCases.createList(
    label: String = "label",
    elements: List<ThingId> = emptyList(),
    id: ThingId? = null,
    contributorId: ContributorId = ContributorId.UNKNOWN,
) = create(
    CreateListUseCase.CreateCommand(
        contributorId = contributorId,
        label = label,
        elements = elements,
        id = id
    )
)

// Statements

fun StatementUseCases.createStatement(
    subject: ThingId,
    predicate: ThingId,
    `object`: ThingId,
    contributorId: ContributorId = ContributorId.UNKNOWN,
    modifiable: Boolean = true,
) = create(
    CreateCommand(
        contributorId = contributorId,
        subjectId = subject,
        predicateId = predicate,
        objectId = `object`,
        modifiable = modifiable
    )
)

private fun String.toDisplayId() = lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")
