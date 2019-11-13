package eu.tib.orkg.prototype.auth.rest

import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(BAD_REQUEST)
abstract class UserRegistrationException(message: String) : RuntimeException(message)

class PasswordsDoNotMatch : UserRegistrationException("The provided passwords do not match")

class UserAlreadyRegistered(email: String) :
    UserRegistrationException("A user with email $email is already registered")
