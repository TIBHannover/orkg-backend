package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userService: UserService
) {
    @GetMapping("/")
    fun lookupUserDetails(principal: Principal): ResponseEntity<UserDetails> {
        if (principal.name == null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        val user = userService.findById(UUID.fromString(principal.name))
        if (user.isPresent)
            return ResponseEntity.ok(UserDetails(user.get()))
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

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
