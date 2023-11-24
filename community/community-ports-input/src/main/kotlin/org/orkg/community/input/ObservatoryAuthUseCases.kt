package org.orkg.community.input

import java.util.*
import org.orkg.auth.domain.User

interface ObservatoryAuthUseCases {
    // TODO: More obscure use cases, that we should change or decouple:
    fun addUserObservatory(observatoryId: UUID, organizationId: UUID, contributor: User): User
    fun deleteUserObservatory(contributor: UUID)
}
