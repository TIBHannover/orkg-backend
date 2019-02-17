package eu.tib.orkg.prototype.statements.domain.model

interface IdentityGenerator<out T> {
    fun nextIdentity(): T
}
