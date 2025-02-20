package org.orkg.mediastorage.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.orkg.mediastorage.domain.ImageId
import org.orkg.mediastorage.output.ImageRepository
import java.util.UUID

interface ImageRepositoryContracts {
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
