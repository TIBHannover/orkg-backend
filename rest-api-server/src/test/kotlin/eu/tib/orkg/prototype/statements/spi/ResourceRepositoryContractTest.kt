package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest

interface ResourceRepositoryContractTest {
    val repository: ResourceRepository

    @Test
    fun `when saved and loaded, then restores all properties`() {
        val expected = Resource(
            id = ResourceId("GecWdydH1s"),
            label = "0k6Y85xY9R",
            createdAt = OffsetDateTime.of(2022, 7, 19, 13, 14, 5, 12345, ZoneOffset.ofHours(2)),
            classes = setOf(ClassId("1F8eUlcCug"), ClassId("GecWdydH1s"), ClassId("2JYHgz8lvB")),
            createdBy = ContributorId("24c40ebb-a3d4-4cda-bf8c-41e2237b4ab0"),
            observatoryId = ObservatoryId("e68cdf97-ff61-434a-af9d-4120bcf7eb38"),
            extractionMethod = ExtractionMethod.AUTOMATIC,
            featured = null,
            unlisted = true,
            verified = false,
        )
        repository.save(expected)

        val actual = repository.findByResourceId(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.label shouldBe expected.label
            it.createdAt shouldBe expected.createdAt
            it.createdBy shouldBe expected.createdBy
            it.classes shouldContainExactlyInAnyOrder expected.classes
            it.observatoryId shouldBe expected.observatoryId
            it.extractionMethod shouldBe expected.extractionMethod
            it.featured shouldBe expected.featured
            it.unlisted shouldBe expected.unlisted
            it.verified shouldBe expected.verified
        }
    }

    @Test
    fun `given several resources, when all retrieved, gets the correct count when paged`() {
        val times = 23
        repeat(times) {
            val r = createResource().copy(id = ResourceId(UUID.randomUUID().toString()))
            repository.save(r)
        }
        repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe times
    }

    @Test
    @Suppress("UNUSED_VARIABLE") // Names are provided to better understand the test setup
    fun `when searching for featured resources, then unlisted are ignored, because they are mutually exclusive`() {
        val featured = newResourceWith(1, setOf("Foo")).copy(featured = true, unlisted = false).also {
            repository.save(it)
        }
        val notFeatured = newResourceWith(2, setOf("Foo")).copy(featured = false, unlisted = false).also {
            repository.save(it)
        }
        val unlisted = newResourceWith(10, setOf("Foo")).copy(featured = false, unlisted = true).also {
            repository.save(it)
        }
        val listed = newResourceWith(11, setOf("Foo")).copy(featured = false, unlisted = false).also {
            repository.save(it)
        }
        val inconsistent = newResourceWith(9999, setOf("Foo")).copy(featured = true, unlisted = true).also {
            repository.save(it)
        }
        val wrongClass = newResourceWith(8888, setOf("WrongClass")).copy(featured = true, unlisted = false).also {
            repository.save(it)
        }

        val result = repository.findAllFeaturedResourcesByClassIds(setOf(ClassId("Foo")), PageRequest.of(0, 100)).content

        result.asClue {
            it shouldHaveSize 1
            it.first().id shouldBe featured.id
        }
    }

    @Test
    @Suppress("UNUSED_VARIABLE") // Names are provided to better understand the test setup
    fun `when searching for unlisted resources, then featured are ignored, because they are mutually exclusive`() {
        val featured = newResourceWith(1, setOf("Foo")).copy(featured = true, unlisted = false).also {
            repository.save(it)
        }
        val notFeatured = newResourceWith(2, setOf("Foo")).copy(featured = false, unlisted = false).also {
            repository.save(it)
        }
        val unlisted = newResourceWith(10, setOf("Foo")).copy(featured = false, unlisted = true).also {
            repository.save(it)
        }
        val listed = newResourceWith(11, setOf("Foo")).copy(featured = false, unlisted = false).also {
            repository.save(it)
        }
        val inconsistent = newResourceWith(9999, setOf("Foo")).copy(featured = true, unlisted = true).also {
            repository.save(it)
        }
        val wrongClass = newResourceWith(8888, setOf("WrongClass")).copy(featured = false, unlisted = true).also {
            repository.save(it)
        }

        val result = repository.findAllUnlistedResourcesByClassIds(setOf(ClassId("Foo")), PageRequest.of(0, 100)).content

        result.asClue {
            it shouldHaveSize 1
            it.first().id shouldBe unlisted.id
        }
    }

    private fun newResourceWith(id: Long, classes: Set<String>) = createResource().copy(
        id = ResourceId(id), classes = classes.map(::ClassId).toSet()
    )

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
