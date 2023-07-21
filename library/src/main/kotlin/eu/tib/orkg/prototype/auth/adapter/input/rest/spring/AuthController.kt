package eu.tib.orkg.prototype.auth.adapter.input.rest.spring

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.auth.domain.PasswordsDoNotMatch
import eu.tib.orkg.prototype.auth.domain.UserAlreadyRegistered
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth", produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController(
    private val userService: AuthUseCase
) {
    @PostMapping("/register", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(OK)
    fun registerUser(@RequestBody @Valid request: RegisterUserRequest): ResponseEntity<*> {
        // TODO: Move logic to service
        val email = request.email.lowercase()
        val user = userService.findByEmail(email)
        if (user.isPresent)
            throw UserAlreadyRegistered(request.email)

        if (request.password != request.matchingPassword)
            throw PasswordsDoNotMatch()

        userService.registerUser(email, request.password, request.displayName)

        return successResponse()
    }

    private fun successResponse() = ok(RegisteredUserResponse("success"))

    data class RegisterUserRequest(
        @field:Email
        @field:NotBlank
        val email: String,

        @field:Size(min = 6, message = "Please choose a more secure password. It should be longer than 6 characters.")
        @field:NotBlank
        val password: String,

        @field:NotBlank
        @JsonProperty("matching_password")
        val matchingPassword: String,

        @field:Size(min = 1, max = 100)
        @JsonProperty("display_name")
        val displayName: String?
    )

    data class RegisteredUserResponse(
        val status: String
    )
}
