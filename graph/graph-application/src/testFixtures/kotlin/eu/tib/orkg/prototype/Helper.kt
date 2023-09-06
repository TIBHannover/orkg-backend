package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.OrganizationType
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.ObjectIdAndLabel
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
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

/**
 * Creates a resource that uses as many defaults as possible.
 */
fun createResource(id: ThingId = ThingId("R1")) = Resource(id, "Default Label", OffsetDateTime.now())

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

fun createOrganization() = Organization(
    id = OrganizationId(UUID.fromString("d02073bc-30fd-481e-9167-f3fc3595d590")),
    name = "some organization name",
    createdBy = ContributorId("ee06bdf3-d6f3-41d1-8af2-64c583d9057e"),
    homepage = "https://example.org",
    displayId = "some display id",
    type = OrganizationType.GENERAL,
    logoId = null
)

fun createObservatory(organizationIds: Set<OrganizationId>) = Observatory(
    id = ObservatoryId(UUID.fromString("95565e51-2b80-4c28-918c-6fbc5e2a9b33")),
    name = "Test Observatory",
    description = "Example Description",
    researchField = ThingId("R1234"),
    organizationIds = organizationIds,
    displayId = "test_observatory"
)

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
        url = "https://example.org"
    ),
    authors = listOf(
        Author(
            id = ThingId("147"),
            name = "Josiah Stinkney Carberry",
            identifiers = mapOf(
                "orcid" to "0000-0002-1825-0097"
            ),
            homepage = "https://example.org"
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
