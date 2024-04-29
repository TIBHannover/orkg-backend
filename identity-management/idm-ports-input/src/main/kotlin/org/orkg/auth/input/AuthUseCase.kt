package org.orkg.auth.input

import java.util.*
import org.orkg.auth.domain.User

interface AuthUseCase : RegisterUserUseCase, ManageUserSettingUseCases, FindUserUseCases

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
