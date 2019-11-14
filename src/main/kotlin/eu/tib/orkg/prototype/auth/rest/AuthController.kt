package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.service.UserService
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val userService: UserService
) {
    @PostMapping("/register")
    @ResponseStatus(OK)
    fun registerUser(@RequestBody @Valid request: RegisterUserRequest): ResponseEntity<*> {
        val user = userService.findByEmail(request.email)
        if (user.isPresent)
            throw UserAlreadyRegistered(request.email)

        if (request.password != request.matchingPassword)
            throw PasswordsDoNotMatch()

        userService.registerUser(request.email, request.password, request.displayName)

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
