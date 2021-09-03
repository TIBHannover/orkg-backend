package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

interface ResourceRepositoryContract {
    val repository: ResourceRepository

    @Test
    @DisplayName("creates a node when saving")
    fun createsNodeWhenSaving() {
        assertThat(repository.count()).isEqualTo(0L)
        repository.save(Resource(ResourceId("R1"), "some resource"))
        assertThat(repository.count()).isEqualTo(1L)
    }
}
