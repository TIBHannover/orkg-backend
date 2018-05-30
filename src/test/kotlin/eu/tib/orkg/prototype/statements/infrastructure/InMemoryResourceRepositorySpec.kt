package eu.tib.orkg.prototype.statements.infrastructure

import eu.tib.orkg.prototype.statements.domain.model.ResourceRepositorySpec
import org.junit.jupiter.api.DisplayName

@DisplayName("In-Memory Resource Repository")
class InMemoryResourceRepositorySpec : ResourceRepositorySpec() {
    override fun createRepository() = InMemoryResourceRepository()
}
