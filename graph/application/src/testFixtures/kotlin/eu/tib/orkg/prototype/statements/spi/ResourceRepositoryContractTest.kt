package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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
    fun `successfully restores all properties after saving`() {
        val expected = Resource(
            id = ThingId("GecWdydH1s"),
            label = "0k6Y85xY9R",
            createdAt = OffsetDateTime.of(2022, 7, 19, 13, 14, 5, 12345, ZoneOffset.ofHours(2)),
            classes = setOf(ThingId("1F8eUlcCug"), ThingId("GecWdydH1s"), ThingId("2JYHgz8lvB")),
            createdBy = ContributorId("24c40ebb-a3d4-4cda-bf8c-41e2237b4ab0"),
            observatoryId = ObservatoryId("e68cdf97-ff61-434a-af9d-4120bcf7eb38"),
            extractionMethod = ExtractionMethod.AUTOMATIC,
            visibility = Visibility.UNLISTED,
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
            it.visibility shouldBe expected.visibility
            it.verified shouldBe expected.verified
        }
    }

    @Test
    fun `given several resources, when all retrieved, gets the correct count when paged`() {
        val times = 23
        repeat(times) {
            val r = createResource().copy(id = ThingId(UUID.randomUUID().toString()))
            repository.save(r)
        }
        repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe times
    }

    @Test
    fun `given a resource with a class, when it is searched by it's observatory ID and class, it should be found`() {
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val classes = setOf(ThingId("ToBeFound"), ThingId("Other"))
        val resource = createResource().copy(
            id = ThingId("R1234"),
            observatoryId = observatoryId,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource)

        val result = repository.findAllByClassInAndVisibilityAndObservatoryId(
            setOf(ThingId("ToBeFound")),
            visibility = Visibility.DEFAULT,
            observatoryId,
            PageRequest.of(0, 10)
        )

        result.content.size shouldBe 1
        result.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given a resource with a class, when searched by it's observatory ID and class and featured is true, it should not be found`() {
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val classes = setOf(ThingId("ToBeFound"), ThingId("Other"))
        val resource = createResource().copy(
            id = ThingId("R1234"),
            observatoryId = observatoryId,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource)

        val result = repository.findAllByClassInAndVisibilityAndObservatoryId(
            setOf(ThingId("ToBeFound")),
            visibility = Visibility.FEATURED,
            observatoryId,
            PageRequest.of(0, 10)
        )

        result.content.size shouldBe 0
    }

    @Test
    fun `given a resource with a class, when searched by it's class and observatory ID, it should be found`() {
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val classes = setOf(ThingId("ToBeFound"), ThingId("Other"))
        val resource = createResource().copy(
            id = ThingId("R1234"),
            observatoryId = observatoryId,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource)

        val result = repository.findByClassAndObservatoryId(ThingId("ToBeFound"), observatoryId)
        result.count() shouldBe 1
        result.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given a resource with a class, when searched by it's class and observatory ID, it should not be found`() {
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val classes = setOf(ThingId("NotToBeFound"), ThingId("Other"))
        val resource = createResource().copy(
            id = ThingId("R1234"),
            observatoryId = observatoryId,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource)

        val result = repository.findByClassAndObservatoryId(ThingId("ToBeFound"), observatoryId)
        result.count() shouldBe 0
    }

    @Test
    fun `given a resource with a class, when searched by including class set, it should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)

        val result = repository.findAllIncludingAndExcludingClasses(
            setOf(ThingId("ToBeFound")),
            setOf(),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 1
        result.content.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given multiple resources with a class, when searched by including class set, they should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)

        val result = repository.findAllIncludingAndExcludingClasses(
            setOf(ThingId("ToBeFound")),
            setOf(),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 2
        result.content.map(Resource::id) shouldContainAll setOf(ThingId("R1234"), ThingId("R2345"))
    }

    @Test
    fun `given a resource with a class, when searched by excluding class set, it should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)

        val result = repository.findAllIncludingAndExcludingClasses(
            setOf(),
            setOf(ThingId("NotToBeFound")),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 1
        result.content.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given multiple resources with a class, when searched by excluding class set, they should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)

        val result = repository.findAllIncludingAndExcludingClasses(
            setOf(),
            setOf(ThingId("NotToBeFound")),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 2
        result.content.map(Resource::id) shouldContainAll setOf(ThingId("R1234"), ThingId("R2345"))
    }

    @Test
    fun `given a resource with a class, when searched by its label (exact), including and excluding class, it should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            label = "12345",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            label = "12345",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)
        val resource3 = createResource().copy(
            id = ThingId("R3456"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            label = "abcdef",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource3)

        val result = repository.findAllIncludingAndExcludingClassesByLabel(
            setOf(ThingId("ToBeFound")),
            setOf(ThingId("NotToBeFound")),
            SearchString.of("12345", exactMatch = true),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 1
        result.content.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given a resource with a class, when searched by its label (fuzzy), including and excluding class, it should be found`() {
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            classes = setOf(ThingId("ToBeFound"), ThingId("Other")),
            label = "12345",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            label = "12345",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)
        val resource3 = createResource().copy(
            id = ThingId("R3456"),
            classes = setOf(ThingId("NotToBeFound"), ThingId("Other")),
            label = "abcdef",
            visibility = Visibility.DEFAULT
        )
        repository.save(resource3)

        val result = repository.findAllIncludingAndExcludingClassesByLabel(
            setOf(ThingId("ToBeFound")),
            setOf(ThingId("NotToBeFound")),
            SearchString.of("1234", exactMatch = false),
            PageRequest.of(0, 10)
        )
        result.totalElements shouldBe 1
        result.content.first().id shouldBe ThingId("R1234")
    }

    @Test
    fun `given multiple resources, find all contributor ids`() {
        val classes = setOf(ThingId("ToBeFound"), ThingId("Other"))
        val contributorId1 = ContributorId("dc8b2055-c14a-4e9f-9fcd-e0b79cf1f834")
        val contributorId2 = ContributorId("4e08d9e4-e16c-42f1-9e9b-294579bdff1d")
        val resource1 = createResource().copy(
            id = ThingId("R1234"),
            createdBy = contributorId1,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource1)
        val resource2 = createResource().copy(
            id = ThingId("R2345"),
            createdBy = contributorId1,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource2)
        val resource3 = createResource().copy(
            id = ThingId("R3456"),
            createdBy = contributorId2,
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource3)
        val resource4 = createResource().copy(
            id = ThingId("R4567"),
            createdBy = ContributorId.createUnknownContributor(),
            classes = classes,
            visibility = Visibility.DEFAULT
        )
        repository.save(resource4)

        val result = repository.findAllContributorIds(PageRequest.of(0, Int.MAX_VALUE))
        result.totalElements shouldBe 2
        result.content shouldContainAll setOf(contributorId1, contributorId2)
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
