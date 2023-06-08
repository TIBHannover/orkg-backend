package eu.tib.orkg.prototype.auth.domain

import eu.tib.orkg.prototype.shared.SimpleMessageException
import java.util.*
import org.springframework.http.HttpStatus

abstract class UserRegistrationException(
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)

class PasswordsDoNotMatch : UserRegistrationException("The provided passwords do not match.")

class UserAlreadyRegistered(email: String) :
    UserRegistrationException("A user with email $email is already registered.")

class CurrentPasswordInvalid : UserRegistrationException("The provided current password is not correct.")

class UserNotFound(userId: UUID) :
    SimpleMessageException(HttpStatus.BAD_REQUEST, """No user with ID $userId""")
