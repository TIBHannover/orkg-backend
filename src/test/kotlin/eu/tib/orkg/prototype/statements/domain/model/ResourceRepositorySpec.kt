package eu.tib.orkg.prototype.statements.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

abstract class ResourceRepositorySpec {
    @Test
    @DisplayName("Adding and retrieving a resource should return an equal resource")
    fun addingAndFindingShouldRetrieveTheSameResource() {
        val id = ResourceId("1")
        val expectedResource = Resource(id, "some value")

        repository.add(expectedResource)

        val retrievedResource = repository.findById(id).get()

        // We do not expect the object to be the same, just equal.
        assertThat(retrievedResource).isEqualTo(expectedResource)
    }

    abstract fun createRepository(): ResourceRepository

    private val repository: ResourceRepository = this.createRepository()
}
