package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId

interface CreatePredicateUseCase {
    // legacy methods:
    fun create(label: String): Predicate
    fun create(userId: ContributorId, label: String): Predicate
    fun create(request: CreatePredicateRequest): Predicate
    fun create(userId: ContributorId, request: CreatePredicateRequest): Predicate
    fun createIfNotExists(id: PredicateId, label: String)
}

interface UpdatePredicateUseCase {
    // legacy methods:
    fun update(predicate: Predicate): Predicate
}

interface DeletePredicateUseCase {
    // legacy methods:
    fun removeAll()
}
