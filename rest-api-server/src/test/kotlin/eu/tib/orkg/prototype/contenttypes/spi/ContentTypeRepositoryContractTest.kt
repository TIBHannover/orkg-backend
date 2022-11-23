package eu.tib.orkg.prototype.contenttypes.spi

import eu.tib.orkg.prototype.contenttypes.domain.ContentType
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest

interface ContentTypeRepositoryContractTest<T: ContentType> {
    val repository: ConcreteContentTypeRepository<T>

    // TODO: check if ConcreteContentTypeRepository.toResource and ConcreteContentTypeRepository.toContentType are correct

    @Test
    fun `given several content type resources, when all retrieved, gets the correct count when paged`() {
        val times = 23
        repeat(times) {
            repository.save(create(
                id = ResourceId(UUID.randomUUID().toString())
            ))
        }
        val result = repository.findAll(PageRequest.of(0, 10))
        result.totalElements shouldBe times
    }

    @Test
    fun `do not find non-featured resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            featured = false
        )
        repository.save(toBeFound)

        val result = repository.findAllFeaturedIs(true, PageRequest.of(0, 10))
        result.totalElements shouldBe 0
    }

    @Test
    fun `do not find featured resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            featured = true
        )
        repository.save(toBeFound)

        val result = repository.findAllFeaturedIs(false, PageRequest.of(0, 10))
        result.totalElements shouldBe 0
    }

    @Test
    fun `find only featured resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            featured = true
        )
        repository.save(toBeFound)
        val notToBeFound = create(
            id = ResourceId("R2345"),
            featured = false
        )
        repository.save(notToBeFound)

        val result = repository.findAllFeaturedIs(true, PageRequest.of(0, 10))
        result.content.size shouldBe 1
        result.first().id shouldBe ResourceId("R1234")
    }

    @Test
    fun `find only non-featured resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            featured = false
        )
        repository.save(toBeFound)
        val notToBeFound = create(
            id = ResourceId("R2345"),
            featured = true
        )
        repository.save(notToBeFound)

        val result = repository.findAllFeaturedIs(false, PageRequest.of(0, 10))
        result.content.size shouldBe 1
        result.first().id shouldBe ResourceId("R1234")
    }

    @Test
    fun `do not find featured and unlisted resource`() {
        val notToBeFound = create(
            id = ResourceId("R1234"),
            featured = true,
            unlisted = true
        )
        repository.save(notToBeFound)

        val result = repository.findAllFeaturedIs(true, PageRequest.of(0, 10))
        result.totalElements shouldBe 0
    }

    @Test
    fun `do not find listed resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            unlisted = false
        )
        repository.save(toBeFound)

        val result = repository.findAllUnlistedIs(true, PageRequest.of(0, 10))
        result.totalElements shouldBe 0
    }

    @Test
    fun `do not find unlisted resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            unlisted = true
        )
        repository.save(toBeFound)

        val result = repository.findAllUnlistedIs(false, PageRequest.of(0, 10))
        result.totalElements shouldBe 0
    }

    @Test
    fun `find only unlisted resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            unlisted = true
        )
        repository.save(toBeFound)
        val notToBeFound = create(
            id = ResourceId("R2345"),
            unlisted = false
        )
        repository.save(notToBeFound)

        val result = repository.findAllUnlistedIs(true, PageRequest.of(0, 10))
        result.content.size shouldBe 1
        result.first().id shouldBe ResourceId("R1234")
    }

    @Test
    fun `find only listed resources`() {
        val toBeFound = create(
            id = ResourceId("R1234"),
            unlisted = false
        )
        repository.save(toBeFound)
        val notToBeFound = create(
            id = ResourceId("R2345"),
            unlisted = true
        )
        repository.save(notToBeFound)

        val result = repository.findAllUnlistedIs(false, PageRequest.of(0, 10))
        result.content.size shouldBe 1
        result.first().id shouldBe ResourceId("R1234")
    }

    fun cleanUpAfterEach()

    fun create(id: ResourceId, featured: Boolean = false, unlisted: Boolean = false): T

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
