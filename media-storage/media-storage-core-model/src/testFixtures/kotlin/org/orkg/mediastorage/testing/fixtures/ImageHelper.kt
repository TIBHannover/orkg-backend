package org.orkg.mediastorage.testing.fixtures

import java.io.BufferedReader
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import javax.activation.MimeType
import javax.activation.MimetypesFileTypeMap
import org.orkg.common.ContributorId
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.mediastorage.domain.Image
import org.orkg.mediastorage.domain.ImageData
import org.orkg.mediastorage.domain.ImageId
import org.orkg.testing.fixedClock
import org.springframework.core.io.ClassPathResource

val testImage: URI = URI.create("classpath:/images/test_image.png")
val encodedTestImage: URI = URI.create("classpath:/images/test_image_encoded.txt")

fun loadImage(
    uri: URI,
    id: ImageId = ImageId(UUID.fromString("bcf41f4b-aa9c-4e98-8fb0-dfe89b4fad27")),
    createdBy: ContributorId = ContributorId(UUID.fromString("d02073bc-30fd-481e-9167-f3fc3595d590")),
    createdAt: OffsetDateTime? = null,
    clock: Clock = fixedClock,
): Image {
    val image = loadRawImage(uri)
    return Image(id, image.data, image.mimeType, createdBy, createdAt ?: OffsetDateTime.now(clock))
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
