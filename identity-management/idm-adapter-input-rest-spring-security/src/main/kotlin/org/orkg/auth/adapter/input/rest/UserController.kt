package org.orkg.auth.adapter.input.rest

import com.fasterxml.jackson.annotation.JsonProperty
import java.security.Principal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.orkg.auth.domain.CurrentPasswordInvalid
import org.orkg.auth.domain.PasswordsDoNotMatch
import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.auth.domain.UserNotFound
import org.orkg.auth.input.AuthUseCase
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
    private val userService: AuthUseCase,
) {
    @GetMapping
    fun lookupUserDetails(principal: Principal?): ResponseEntity<UserDetails> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        return ok(UserDetails(user))
    }

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUserDetails(@RequestBody @Valid updatedDetails: UserDetailsUpdateRequest, principal: Principal?): ResponseEntity<UserDetails> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        userService.updateName(user.id, updatedDetails.displayName)
        return ok(UserDetails(user))
    }

    @PutMapping("/password", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePassword(@RequestBody @Valid updatedPassword: PasswordDTO, principal: Principal?): ResponseEntity<Any> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        if (!updatedPassword.hasMatchingPasswords())
            throw PasswordsDoNotMatch()

        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        if (userService.checkPassword(user.id, updatedPassword.currentPassword)) {
            userService.updatePassword(user.id, updatedPassword.newPassword)
        } else {
            throw CurrentPasswordInvalid()
        }
        return ok(UpdatedUserResponse("success"))
    }

    /**
     * Decorator for user data.
     * This class prevents user data from leaking by only exposing data that is relevant to the client.
     */
    data class UserDetails(private val user: User) {
        @JsonProperty("id")
        val id: UUID = user.id

        @JsonProperty("email")
        val email = user.email

        @JsonProperty("display_name")
        val displayName = user.displayName

        @JsonProperty("created_at")
        val created = user.createdAt

        @JsonProperty("organization_id")
        val organizationId = user.organizationId

        @JsonProperty("observatory_id")
        val observatoryId = user.observatoryId

        @get:JsonProperty("is_curation_allowed")
        val isCurationAllowed: Boolean
            get() = "ROLE_ADMIN" in user.roles.map(Role::name)
    }

    data class UpdatedUserResponse(
        val status: String
    )

    /**
     * Data Transfer Object (DTO) for updating the user details.
     */
    data class UserDetailsUpdateRequest(
        @field:Size(min = 1, max = 100)
        @JsonProperty("display_name")
        val displayName: String
    )

    /**
     * Data Transfer Object (DTO) for updating the password. All fields need to be provided.
     */
    data class PasswordDTO(
        @field:NotBlank
        @JsonProperty("current_password")
        val currentPassword: String,

        @field:Size(min = 6, message = "Please choose a more secure password. It should be longer than 6 characters.")
        @field:NotBlank
        @JsonProperty("new_password")
        val newPassword: String,

        @field:NotBlank
        @JsonProperty("new_matching_password")
        val newMatchingPassword: String
    ) {
        fun hasMatchingPasswords() = newPassword == newMatchingPassword
    }
}
