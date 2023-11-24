package org.orkg.auth.domain

import java.util.*
import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

abstract class UserRegistrationException(
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

class PasswordsDoNotMatch : UserRegistrationException("The provided passwords do not match.")

class UserAlreadyRegistered(email: String) :
    UserRegistrationException("A user with email $email is already registered.")

class CurrentPasswordInvalid : UserRegistrationException("The provided current password is not correct.")

class UserNotFound : SimpleMessageException {
    constructor(userId: UUID) : super(HttpStatus.BAD_REQUEST, """User "$userId" not found.""")
    constructor(email: String) : super(HttpStatus.BAD_REQUEST, """User with email "$email" not found.""")
}
