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

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val userService: UserService
) {
    @PostMapping("/register")
    @ResponseStatus(OK)
    fun registerUser(@RequestBody request: RegisterUserRequest): ResponseEntity<*> {
        val user = userService.findByEmail(request.email)
        if (user.isPresent) {
            return ok(RegisteredUserResponse("success"))
        }

        if (request.password != request.matchingPassword)
            throw RuntimeException("Passwords do not match")

        userService.registerUser(request.email, request.password)

        return ok(RegisteredUserResponse("success"))
    }

    data class RegisterUserRequest(
        val email: String,
        val password: String,
        @JsonProperty("matching_password")
        val matchingPassword: String,
        val name: String?
    )

    data class RegisteredUserResponse(
        val status: String
    )
}
