package eu.tib.orkg.prototype.statements.domain.model

interface IndexService {

    fun createRequiredUniqueConstraints()

    fun createRequiredPropertyIndices()
}
