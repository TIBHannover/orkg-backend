package eu.tib.orkg.prototype.community.api

import eu.tib.orkg.prototype.auth.domain.User
import java.util.*

interface ObservatoryAuthUseCases {
    // TODO: More obscure use cases, that we should change or decouple:
    fun addUserObservatory(observatoryId: UUID, organizationId: UUID, contributor: User): User
    fun deleteUserObservatory(contributor: UUID)
}
