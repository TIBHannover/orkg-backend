package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
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

        assertThat(actual).isNotNull
        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(actual.id).`as`("property 'id'").isEqualTo(expected.id)
            softly.assertThat(actual.label).`as`("property 'label'").isEqualTo(expected.label)
            softly.assertThat(actual.createdAt).`as`("property 'createdAt'").isEqualTo(expected.createdAt)
            softly.assertThat(actual.classes).`as`("property 'classes'")
                .containsExactlyInAnyOrder(*(expected.classes.toTypedArray()))
            softly.assertThat(actual.createdBy).`as`("property 'createdBy'").isEqualTo(expected.createdBy)
            softly.assertThat(actual.observatoryId).`as`("property 'observatoryId'").isEqualTo(expected.observatoryId)
            softly.assertThat(actual.extractionMethod).`as`("property 'extractionMethod'")
                .isEqualTo(expected.extractionMethod)
            softly.assertThat(actual.featured).`as`("property 'featured'").isEqualTo(expected.featured)
            softly.assertThat(actual.unlisted).`as`("property 'unlisted'").isEqualTo(expected.unlisted)
            softly.assertThat(actual.verified).`as`("property 'verified'").isEqualTo(expected.verified)
        }
    }

    @Test
    fun `given several resources, when all retrieved, gets the correct count when paged`() {
        val times = 23
        repeat(times) {
            val r = createResource().copy(id = ResourceId(UUID.randomUUID().toString()))
            repository.save(r)
        }
        assertThat(repository.findAll(PageRequest.of(0, 10)).totalElements).isEqualTo(times.toLong())
    }

    abstract fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
