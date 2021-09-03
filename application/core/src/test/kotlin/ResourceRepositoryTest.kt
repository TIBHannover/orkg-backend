package eu.tib.orkg.prototype.statements.ports

import org.junit.jupiter.api.DisplayName

@DisplayName("An in-memory ResourceRepository")
class ResourceRepositoryTest : ResourceRepositoryContract {
    private val inMemoryResourceRepository = InMemoryResourceRepository()

    override val repository: ResourceRepository
        get() = inMemoryResourceRepository
}
