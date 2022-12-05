package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

interface ClassRepositoryContractTest {
    val repository: ClassRepository

    @Test
    fun `existAll should be true when all classes exist`() {
        val ids = (1L..3).map(::ClassId).onEach { repository.save(createClass().copy(id = it)) }

        assertThat(repository.existsAll(ids.toSet())).isTrue
    }

    @Test
    fun `existAll should be false when at least one class does not exist`() {
        val ids = (1L..3).map(::ClassId).onEach { repository.save(createClass().copy(id = it)) }
            .plus(listOf(ClassId(9)))

        assertThat(repository.existsAll(ids.toSet())).isFalse
    }

    @Test
    fun `existAll should be false when the set of class IDs is empty`() {
        assertThat(repository.existsAll(emptySet())).isFalse
    }
}
