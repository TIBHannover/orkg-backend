package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

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

    @GetMapping("/user")
    fun userDetails(principal: Principal): ResponseEntity<UserDetails> {
        if (principal.name == null) {
            return ResponseEntity(UNAUTHORIZED)
        }
        val user = userService.findById(UUID.fromString(principal.name))
        if (user.isPresent)
            return ok(UserDetails(user.get()))
        return ResponseEntity(NOT_FOUND)
    }

    data class RegisterUserRequest(
        val email: String,
        val password: String,
        @JsonProperty("matching_password")
        val matchingPassword: String
    )

    data class RegisteredUserResponse(
        val status: String
    )

    /**
     * Decorator for user data.
     * This class prevents user data from leaking by only exposing data that is relevant to the client.
     */
    data class UserDetails(private val user: UserEntity) {
        @JsonProperty("email")
        val email = user.email

        @JsonProperty("display_name")
        val displayName = user.displayName

        @JsonProperty("created_at")
        val created = user.created
    }
}
