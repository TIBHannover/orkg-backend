package eu.tib.orkg.prototype.auth.api

import eu.tib.orkg.prototype.auth.domain.User
import java.util.*

interface AuthUseCase : RegisterUserUseCase, ManageUserSettingUseCases, FindUserUseCases {
    // TODO: More obscure use cases, that we should change or decouple:
    fun addUserObservatory(observatoryId: UUID, organizationId: UUID, contributor: User): User
    fun updateOrganizationAndObservatory(userId: UUID, organizationId: UUID?, observatoryId: UUID?)
    fun deleteUserObservatory(contributor: UUID)
}

interface RegisterUserUseCase {
    fun registerUser(anEmail: String, aPassword: String, aDisplayName: String?, userId: UUID? = null): UUID
}

interface ManageUserSettingUseCases {
    fun checkPassword(userId: UUID, aPassword: String): Boolean
    fun updatePassword(userId: UUID, aPassword: String)
    fun updateName(userId: UUID, newName: String)
    fun updateRole(userId: UUID)
}

interface FindUserUseCases {
    fun findByEmail(email: String): Optional<User>
    fun findById(id: UUID): Optional<User>
}
