package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.RoleEntity
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.application.UserNotFound
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import java.security.Principal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val observatoryService: ObservatoryService
) {
    @GetMapping("/")
    fun lookupUserDetails(principal: Principal): ResponseEntity<UserDetails> {
        if (principal.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val user = userService.findById(UUID.fromString(principal.name))
        if (user.isPresent)
            return ok(UserDetails(user.get()))
        return ResponseEntity(NOT_FOUND)
    }

    /**
     * Retrieve the user's data.
     *
     * <strong>Note:</strong> This endpoint should only be used to obtain data for the logged-in user!
     * It should not be used for other user data! Use the contributor abstraction for that!
     */
    @GetMapping("/{id}")
    fun lookupUser(@PathVariable id: UUID): ResponseEntity<UserDetails> {
        val contributor = userService.findById(id).orElseThrow { UserNotFound("$id") }
        return ok(UserDetails(contributor))
    }

    @PutMapping("/")
    fun updateUserDetails(@RequestBody @Valid updatedDetails: UserDetailsUpdateRequest, principal: Principal): ResponseEntity<UserDetails> {
        if (principal.name == null)
            return ResponseEntity((UNAUTHORIZED))
        val foundUser = userService.findById(UUID.fromString(principal.name))
        if (foundUser.isPresent) {
            val currentUser = foundUser.get()
            val id = currentUser.id!!
            userService.updateName(id, updatedDetails.displayName)
            return ok(UserDetails(currentUser))
        }
        return ResponseEntity(NOT_FOUND)
    }

    @PutMapping("/password")
    fun updatePassword(@RequestBody @Valid updatedPassword: PasswordDTO, principal: Principal): ResponseEntity<Any> {
        if (principal.name == null)
            return ResponseEntity((UNAUTHORIZED))
        if (!updatedPassword.hasMatchingPasswords())
            throw PasswordsDoNotMatch()

        val foundUser = userService.findById(UUID.fromString(principal.name))
        if (foundUser.isPresent) {
            val currentUser = foundUser.get()
            if (userService.checkPassword(currentUser.id!!, updatedPassword.currentPassword)) {
                userService.updatePassword(currentUser.id!!, updatedPassword.newPassword)
            } else {
                throw CurrentPasswordInvalid()
            }
        }
        return ok(UpdatedUserResponse("success"))
    }

    @PutMapping("/role")
    fun updateUserRoleToOwner(principal: Principal): ResponseEntity<Any> {
        if (principal.name == null)
            return ResponseEntity((UNAUTHORIZED))
        val foundUser = userService.findById(UUID.fromString(principal.name))
        if (foundUser.isPresent) {
            val currentUser = foundUser.get()
            val id = currentUser.id!!
            userService.updateRole(id)
            return ok(UserDetails(currentUser))
        }
        return ResponseEntity(NOT_FOUND)
    }

    /**
     * Decorator for user data.
     * This class prevents user data from leaking by only exposing data that is relevant to the client.
     */
    data class UserDetails(private val user: UserEntity) {
        @JsonProperty("id")
        val id: UUID = user.id!!

        @JsonProperty("email")
        val email = user.email

        @JsonProperty("display_name")
        val displayName = user.displayName

        @JsonProperty("created_at")
        val created = user.created

        @JsonProperty("organization_id")
        val organizationId = user.organizationId

        @JsonProperty("observatory_id")
        val observatoryId = user.observatoryId

        @get:JsonProperty("is_curation_allowed")
        val isCurationAllowed: Boolean
        get() = "ROLE_ADMIN" in user.roles.map(RoleEntity::name)
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
