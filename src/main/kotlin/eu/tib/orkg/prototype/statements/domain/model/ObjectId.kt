package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.core.Identity

data class ObjectId(override val value: String) :
    Identity<String>(value)
