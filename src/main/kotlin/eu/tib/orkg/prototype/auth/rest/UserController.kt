package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
        if (principal.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val user = userService.findById(UUID.fromString(principal.name))
        if (user.isPresent)
            return ResponseEntity.ok(UserDetails(user.get()))
        return ResponseEntity(NOT_FOUND)
    }

    @PutMapping("/")
    fun updateUserDetails(@RequestBody updatedDetails: UserDetailsUpdateRequest, principal: Principal): ResponseEntity<UserDetails> {
        if (principal.name == null)
            return ResponseEntity((UNAUTHORIZED))
        val foundUser = userService.findById(UUID.fromString(principal.name))
        if (foundUser.isPresent) {
            val currentUser = foundUser.get()
            val id = currentUser.id!!
            if (!updatedDetails.name.isNullOrBlank()) {
                userService.updateName(id, updatedDetails.name)
            }
            if (passwordsAreSetAndMatchIn(updatedDetails)) {
                userService.updatePassword(id, updatedDetails.newPassword!!)
            } else {
                return ResponseEntity(BAD_REQUEST)
            }
            return ok(UserDetails(currentUser))
        }
        return ResponseEntity(NOT_FOUND)
    }

    private fun passwordsAreSetAndMatchIn(updatedDetails: UserDetailsUpdateRequest) =
        !updatedDetails.newPassword.isNullOrBlank() && (updatedDetails.newPassword == updatedDetails.newMatchingPassword)

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

    data class UserDetailsUpdateRequest(
        @JsonProperty("password")
        val newPassword: String?,
        @JsonProperty("matching_password")
        val newMatchingPassword: String?,
        @JsonProperty("display_name")
        val name: String?
    )
}
