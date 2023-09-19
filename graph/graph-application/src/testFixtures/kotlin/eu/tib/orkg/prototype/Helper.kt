package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.OrganizationEntity
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contenttypes.domain.model.Visualization
import eu.tib.orkg.prototype.contenttypes.services.PublishingService
import eu.tib.orkg.prototype.contenttypes.spi.DoiService
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.UpdateOrganizationUseCases
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.io.BufferedReader
import java.io.StringWriter
import java.io.Writer
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import javax.activation.MimeType
import javax.activation.MimetypesFileTypeMap
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

/**
 * Creates a resource that uses as many defaults as possible.
 */
fun createResource(
    id: ThingId = ThingId("R1"),
    label: String = "Default Label",
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    classes: Set<ThingId> = emptySet(),
    createdBy: ContributorId = ContributorId.createUnknownContributor(),
    observatoryId: ObservatoryId = ObservatoryId.createUnknownObservatory(),
    extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    organizationId: OrganizationId = OrganizationId.createUnknownOrganization(),
    visibility: Visibility = Visibility.DEFAULT,
    verified: Boolean? = null,
    unlistedBy: ContributorId? = null
) = Resource(
    id = id,
    label = label,
    createdAt = createdAt,
    classes = classes,
    createdBy = createdBy,
    observatoryId = observatoryId,
    extractionMethod = extractionMethod,
    organizationId = organizationId,
    visibility = visibility,
    verified = verified,
    unlistedBy = unlistedBy
)

fun createClass(id: String = "OK"): Class = Class(
    id = ThingId(id),
    label = "some label",
    createdAt = OffsetDateTime.now(),
    uri = URI.create("https://example.org/OK"),
    createdBy = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834")
)

fun createClassWithoutURI(): Class = createClass().copy(uri = null)

fun createPredicate(id: String = "P1") = Predicate(
    id = ThingId(id),
    label = "some predicate label",
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("a56cfd65-8d29-4eae-a252-1b806fe88d3c"),
)

fun createLiteral(value: String = "some literal value", id: String = "L1") = Literal(
    id = ThingId(id),
    label = value,
    datatype = "xsd:string",
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("679ad2bd-ceb3-4f26-80ec-b6eab7a5e8c1"),
)

fun createStatement(subject: Thing, `object`: Thing) = GeneralStatement(
    id = StatementId(1),
    subject = subject,
    predicate = createPredicate(),
    `object` = `object`,
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
)

fun createStatement(subject: Thing, predicate: Predicate, `object`: Thing) = GeneralStatement(
    id = StatementId(1),
    subject = subject,
    predicate = predicate,
    `object` = `object`,
    createdAt = OffsetDateTime.now(),
    createdBy = ContributorId("34da5516-7901-4b0d-94c5-b062082e11a7")
)

fun createOrganization(
    id: OrganizationId = OrganizationId(UUID.fromString("d02073bc-30fd-481e-9167-f3fc3595d590")),
    name: String = "some organization name",
    createdBy: ContributorId = ContributorId("ee06bdf3-d6f3-41d1-8af2-64c583d9057e"),
    homepage: String = "https://example.org",
    observatories: Set<ObservatoryId> = emptySet(),
    displayId: String = "some display id",
    type: OrganizationType = OrganizationType.GENERAL,
    logoId: ImageId? = null
) = Organization(id, name, createdBy, homepage, observatories, displayId, type, logoId)

/**
 * This method should only be used for mocking purposes, as does not return a valid database entity.
 */
fun Organization.toOrganizationEntity(): OrganizationEntity =
    OrganizationEntity().also {
        it.id = id!!.value
        it.name = name
        it.createdBy = createdBy?.value
        it.url = homepage
        it.displayId = displayId
        it.type = type
        it.logoId = logoId?.value
    }

fun createObservatory(
    organizationIds: Set<OrganizationId> = emptySet(),
    id: ObservatoryId = ObservatoryId(UUID.fromString("95565e51-2b80-4c28-918c-6fbc5e2a9b33")),
    name: String = "Test Observatory",
    description: String = "Example Description",
    researchField: ThingId = ThingId("R1234"),
    members: Set<ContributorId> = emptySet(),
    displayId: String = "test_observatory"
) = Observatory(id, name,  description, researchField, members, organizationIds, displayId)

val testImage: URI = URI.create("classpath:/images/test_image.png")
val encodedTestImage: URI = URI.create("classpath:/images/test_image_encoded.txt")

fun loadImage(
    uri: URI,
    id: ImageId = ImageId(UUID.randomUUID()),
    createdBy: ContributorId = ContributorId(UUID.fromString("d02073bc-30fd-481e-9167-f3fc3595d590")),
    createdAt: OffsetDateTime = OffsetDateTime.now()
): Image {
    val image = loadRawImage(uri)
    return Image(id, image.data, image.mimeType, createdBy, createdAt)
}

fun loadRawImage(uri: URI): UpdateOrganizationUseCases.RawImage {
    uri.inputStream.use {
        val mimeType = MimetypesFileTypeMap().run { getContentType(uri.path) }.let(::MimeType)
        val data = ImageData(it.readBytes())
        return UpdateOrganizationUseCases.RawImage(data, mimeType)
    }
}

fun loadEncodedImage(uri: URI): String =
    uri.inputStream.bufferedReader().use(BufferedReader::readText)

private val URI.inputStream
    get() = ClassPathResource(path).inputStream

fun ((Writer) -> Unit).asString(): String {
    val writer = StringWriter()
    this(writer)
    return writer.toString()
}

fun createDummyPaper() = Paper(
    id = ThingId("R8186"),
    title = "Dummy Paper Title",
    researchFields = listOf(
        ObjectIdAndLabel(
            id = ThingId("R456"),
            label = "Research Field 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R789"),
            label = "Research Field 2"
        )
    ),
    identifiers = mapOf(
        "doi" to "10.1000/182"
    ),
    publicationInfo = PublicationInfo(
        publishedMonth = 4,
        publishedYear = 2023,
        publishedIn = "Fancy Conference",
        url = URI.create("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to "0000-0002-1825-0097"
            ),
            homepage = URI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
        )
    ),
    contributions = listOf(
        ObjectIdAndLabel(
            id = ThingId("R258"),
            label = "Contribution 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R396"),
            label = "Contribution 2"
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT,
    verified = false
)

fun createDummyContribution() = Contribution(
    id = ThingId("R15634"),
    label = "Contribution",
    classes = setOf(ThingId("C123")),
    properties = mapOf(
        Predicates.hasEvaluation to listOf(ThingId("R123"))
    ),
    visibility = Visibility.DEFAULT
)

fun createDummyComparison() = Comparison(
    id = ThingId("R8186"),
    title = "Dummy Comparison Title",
    description = "Some description about the contents",
    researchFields = listOf(
        ObjectIdAndLabel(
            id = ThingId("R456"),
            label = "Research Field 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R789"),
            label = "Research Field 2"
        )
    ),
    identifiers = mapOf(
        "doi" to "10.1000/182"
    ),
    publicationInfo = PublicationInfo(
        publishedMonth = 4,
        publishedYear = 2023,
        publishedIn = "ORKG",
        url = URI.create("https://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to "0000-0002-1825-0097"
            ),
            homepage = URI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
        )
    ),
    contributions = listOf(
        ObjectIdAndLabel(
            id = ThingId("R258"),
            label = "Contribution 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R396"),
            label = "Contribution 2"
        )
    ),
    visualizations = listOf(
        ObjectIdAndLabel(
            id = ThingId("R159"),
            label = "Visualization 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R357"),
            label = "Visualization 2"
        )
    ),
    relatedFigures = listOf(
        ObjectIdAndLabel(
            id = ThingId("R951"),
            label = "Related Figure 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R753"),
            label = "Related Figure 2"
        )
    ),
    relatedResources = listOf(
        ObjectIdAndLabel(
            id = ThingId("R741"),
            label = "Related Resource 1"
        ),
        ObjectIdAndLabel(
            id = ThingId("R852"),
            label = "Related Resource 2"
        )
    ),
    references = listOf(
        "https://www.reference.com/1",
        "https://www.reference.com/2"
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    previousVersion = ThingId("R963"),
    isAnonymized = false,
    visibility = Visibility.DEFAULT
)

fun createDummyComparisonRelatedResource() = ComparisonRelatedResource(
    id = ThingId("R1563"),
    label = "Comparison Related Resource",
    image = "https://example.org/image.png",
    url = "https://orkg.org",
    description = "Description of a Comparison Related Resource"
)

fun createDummyComparisonRelatedFigure() = ComparisonRelatedFigure(
    id = ThingId("R5476"),
    label = "Comparison Related Figure",
    image = "https://example.org/image.png",
    description = "Description of a Comparison Related Figure"
)

fun createDummyVisualization() = Visualization(
    id = ThingId("R8186"),
    title = "Dummy Visualization Title",
    description = "Some description about the contents",
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to "0000-0002-1825-0097"
            ),
            homepage = URI.create("https://example.org")
        ),
        Author(
            id = null,
            name = "Author 2",
            identifiers = emptyMap(),
            homepage = null
        )
    ),
    observatories = listOf(
        ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece"),
        ObservatoryId("73b2e081-9b50-4d55-b464-22d94e8a25f6")
    ),
    organizations = listOf(
        OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f"),
        OrganizationId("1f63b1da-3c70-4492-82e0-770ca94287ea")
    ),
    extractionMethod = ExtractionMethod.UNKNOWN,
    createdAt = OffsetDateTime.parse("2023-04-12T16:05:05.959539600+02:00"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    visibility = Visibility.DEFAULT
)

fun dummyPublishCommand(): PublishingService.PublishCommand =
    PublishingService.PublishCommand(
        id = ThingId("R123"),
        title = "Paper title",
        subject = "Paper subject",
        description = "Description of the paper",
        url = URI.create("https://example.org"),
        creators = listOf(
            Author(
                id = ThingId("147"),
                name = "Josiah Stinkney Carberry",
                identifiers = mapOf(
                    "orcid" to "0000-0002-1825-0097"
                ),
                homepage = URI.create("https://example.org")
            ),
            Author(
                id = null,
                name = "Author 2",
                identifiers = emptyMap(),
                homepage = null
            )
        ),
        resourceType = Classes.paper,
        relatedIdentifiers = listOf("10.1000/183")
    )

fun dummyRegisterDoiCommand(): DoiService.RegisterCommand =
    DoiService.RegisterCommand(
        suffix = "182",
        title = "Paper title",
        subject = "Paper subject",
        description = "Description of the paper",
        url = URI.create("https://example.org"),
        creators = listOf(
            Author(
                id = ThingId("147"),
                name = "Josiah Stinkney Carberry",
                identifiers = mapOf(
                    "orcid" to "0000-0002-1825-0097"
                ),
                homepage = URI.create("https://example.org")
            ),
            Author(
                id = null,
                name = "Author 2",
                identifiers = emptyMap(),
                homepage = null
            )
        ),
        resourceType = Classes.paper.value,
        resourceTypeGeneral = "Dataset",
        relatedIdentifiers = listOf("10.48366/r609337")
    )

fun dummyCreatePaperCommand() = CreatePaperUseCase.CreateCommand(
    contributorId = ContributorId(UUID.randomUUID()),
    title = "test",
    researchFields = listOf(ThingId("R12")),
    identifiers = mapOf("doi" to "dummy.doi.numbers"),
    publicationInfo = PublicationInfo(
        publishedYear = 2015,
        publishedMonth = 5,
        publishedIn = "conference",
        url = URI.create("http://example.org")
    ),
    authors = listOf(
        Author(
            id = ThingId("R123"),
            name = "Author with id"
        ),
        Author(
            name = "Author with orcid",
            identifiers = mapOf("orcid" to "0000-1111-2222-3333")
        ),
        Author(
            id = ThingId("R456"),
            name = "Author with id and orcid",
            identifiers = mapOf("orcid" to "1111-2222-3333-4444")
        ),
        Author(
            name = "Author with homepage",
            homepage = URI.create("http://example.org/author")
        ),
        Author(
            name = "Author that just has a name"
        )
    ),
    observatories = listOf(ObservatoryId(UUID.randomUUID())),
    organizations = listOf(OrganizationId(UUID.randomUUID())),
    contents = CreatePaperUseCase.CreateCommand.PaperContents(
        resources = mapOf(
            "#temp1" to CreatePaperUseCase.CreateCommand.ResourceDefinition(
                label = "MOTO",
                classes = setOf(ThingId("R2000"))
            )
        ),
        literals = mapOf(
            "#temp2" to CreatePaperUseCase.CreateCommand.LiteralDefinition(
                label = "0.1",
                dataType = Literals.XSD.DECIMAL.prefixedUri
            )
        ),
        predicates = mapOf(
            "#temp3" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
                label = "hasResult",
                description = "has result"
            ),
            "#temp4" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
                label = "hasLiteral"
            )
        ),
        contributions = listOf(
            CreatePaperUseCase.CreateCommand.Contribution(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1")
                    )
                )
            ),
            CreatePaperUseCase.CreateCommand.Contribution(
                label = "Contribution 2",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
                    ),
                    Predicates.hasEvaluation.value to listOf(
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1"),
                        CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"),
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp2")
                                ),
                                "#temp4" to listOf(
                                    CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1")
                                )
                            )
                        )
                    )
                )
            )
        )
    ),
    extractionMethod = ExtractionMethod.MANUAL
)

fun dummyCreateContributionCommand() = CreateContributionUseCase.CreateCommand(
    contributorId = ContributorId(UUID.randomUUID()),
    paperId = ThingId("R123"),
    resources = mapOf(
        "#temp1" to CreatePaperUseCase.CreateCommand.ResourceDefinition(
            label = "MOTO",
            classes = setOf(ThingId("R2000"))
        )
    ),
    literals = mapOf(
        "#temp2" to CreatePaperUseCase.CreateCommand.LiteralDefinition(
            label = "0.1",
            dataType = Literals.XSD.DECIMAL.prefixedUri
        )
    ),
    predicates = mapOf(
        "#temp3" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasResult",
            description = "has result"
        ),
        "#temp4" to CreatePaperUseCase.CreateCommand.PredicateDefinition(
            label = "hasLiteral"
        )
    ),
    contribution = CreatePaperUseCase.CreateCommand.Contribution(
        label = "Contribution 1",
        classes = setOf(ThingId("C123")),
        statements = mapOf(
            Predicates.hasResearchProblem.value to listOf(
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003")
            ),
            Predicates.hasEvaluation.value to listOf(
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1"),
                CreatePaperUseCase.CreateCommand.StatementObjectDefinition(
                    id = "R3004",
                    statements = mapOf(
                        "#temp3" to listOf(
                            CreatePaperUseCase.CreateCommand.StatementObjectDefinition("R3003"),
                            CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp2")
                        ),
                        "#temp4" to listOf(
                            CreatePaperUseCase.CreateCommand.StatementObjectDefinition("#temp1")
                        )
                    )
                )
            )
        )
    )
)

fun <T> pageOf(vararg values: T, pageable: Pageable = Pageable.unpaged()): Page<T> =
    pageOf(listOf(*values), pageable)

fun <T> pageOf(values: List<T>, pageable: Pageable = Pageable.unpaged()): Page<T> =
    PageImpl(values, pageable, values.size.toLong())
