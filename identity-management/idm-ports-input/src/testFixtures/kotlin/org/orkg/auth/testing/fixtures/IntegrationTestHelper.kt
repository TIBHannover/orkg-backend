package org.orkg.auth.testing.fixtures

import org.orkg.auth.input.AuthUseCase

// Users

fun AuthUseCase.createUser(
    anEmail: String = "user@example.org",
    aPassword: String = "123456",
    aDisplayName: String = "Example User"
) = this.registerUser(anEmail, aPassword, aDisplayName)
