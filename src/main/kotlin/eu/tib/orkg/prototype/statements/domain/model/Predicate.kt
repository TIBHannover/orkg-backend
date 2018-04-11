package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Entity

data class Predicate(override val id: PredicateId) :
    Entity<PredicateId>(id)
