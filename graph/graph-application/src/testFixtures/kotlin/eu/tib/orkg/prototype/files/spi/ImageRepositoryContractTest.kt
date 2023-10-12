package eu.tib.orkg.prototype.files.spi

import eu.tib.orkg.prototype.files.testing.fixtures.testImage
import eu.tib.orkg.prototype.files.domain.model.ImageId
import eu.tib.orkg.prototype.files.testing.fixtures.loadImage
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.Test

interface ImageRepositoryContractTest {
    val repository: ImageRepository

    @Test
    fun `successfully save and load an image`() {
        val image = loadImage(testImage)
        repository.save(image)

        val result = repository.findById(image.id)
        result.isPresent shouldBe true
        result.get().asClue {
            it.id shouldBe image.id
            it.mimeType.toString() shouldBe image.mimeType.toString()
            it.data shouldBe image.data
            it.createdBy shouldBe image.createdBy
            it.createdAt shouldBe image.createdAt
        }
    }

    @Test
    fun `when searching for an image, and the image is not in the repository, an empty result is returned from the repository`() {
        val result = repository.findById(ImageId(UUID.randomUUID()))
        result.isPresent shouldBe false
    }

    @Test
    fun `given a new new id is requested, it should be different`() {
        val id1 = repository.nextIdentity()
        val id2 = repository.nextIdentity()
        id1 shouldNotBe id2
    }
}
