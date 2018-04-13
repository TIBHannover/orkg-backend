package eu.tib.orkg.prototype.authentication.domain.model

import eu.tib.orkg.prototype.core.Identity
import java.util.*

data class UserId(override val value: UUID = UUID.randomUUID()) :
    Identity<UUID>(value)
