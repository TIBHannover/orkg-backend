package eu.tib.orkg.prototype.files.testing.fixtures

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.files.domain.model.Image
import eu.tib.orkg.prototype.files.domain.model.ImageData
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.statements.api.UpdateOrganizationUseCases
import java.io.BufferedReader
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import javax.activation.MimeType
import javax.activation.MimetypesFileTypeMap
import org.springframework.core.io.ClassPathResource

val testImage: URI = URI.create("classpath:/images/test_image.png")
val encodedTestImage: URI = URI.create("classpath:/images/test_image_encoded.txt")

fun loadImage(
    uri: URI,
    id: ImageId = ImageId(UUID.fromString("bcf41f4b-aa9c-4e98-8fb0-dfe89b4fad27")),
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

private inline val URI.inputStream
    get() = ClassPathResource(path).inputStream
