package eu.tib.orkg.prototype.auth.adapter.input.rest.spring

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.auth.domain.CurrentPasswordInvalid
import eu.tib.orkg.prototype.auth.domain.PasswordsDoNotMatch
import eu.tib.orkg.prototype.auth.domain.Role
import eu.tib.orkg.prototype.auth.domain.User
import eu.tib.orkg.prototype.community.application.UserIsAlreadyMemberOfObservatory
import eu.tib.orkg.prototype.community.application.UserIsAlreadyMemberOfOrganization
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.toContributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.UserNotFound
import java.security.Principal
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: AuthUseCase,
) {
    @GetMapping("/")
    fun lookupUserDetails(principal: Principal?): ResponseEntity<UserDetails> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        return ok(UserDetails(user))
    }

    @PutMapping("/")
    fun updateUserDetails(@RequestBody @Valid updatedDetails: UserDetailsUpdateRequest, principal: Principal?): ResponseEntity<UserDetails> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        userService.updateName(user.id, updatedDetails.displayName)
        return ok(UserDetails(user))
    }

    @PutMapping("/password")
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

    @PutMapping("/role")
    fun updateUserRoleToOwner(principal: Principal?): ResponseEntity<Any> {
        if (principal?.name == null)
            return ResponseEntity(UNAUTHORIZED)
        val id = UUID.fromString(principal.name)
        val user = userService.findById(id).orElseThrow { UserNotFound(id) }
        userService.updateRole(id)
        return ok(UserDetails(user))
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/observatory")
    fun updateUserObservatory(@RequestBody @Valid userObservatory: UserObservatoryRequest): ResponseEntity<Any> {
        val user = userService.findByEmail(userObservatory.userEmail).orElseThrow { UserNotFound(userObservatory.userEmail) }
        if (user.observatoryId == userObservatory.observatoryId.value) {
            throw UserIsAlreadyMemberOfObservatory(userObservatory.observatoryId)
        }
        if (user.organizationId == userObservatory.organizationId.value) {
            throw UserIsAlreadyMemberOfOrganization(userObservatory.organizationId)
        }
        return ok(
            userService.addUserObservatory(
                userObservatory.observatoryId.value,
                userObservatory.organizationId.value,
                user
            ).toContributor()
        )
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("{id}/observatory")
    fun deleteUserObservatory(@PathVariable id: ContributorId) {
        userService.deleteUserObservatory(id.value)
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

    data class UserObservatoryRequest(
        @field:Email
        @field:NotBlank
        @JsonProperty("user_email")
        val userEmail: String,
        @JsonProperty("observatory_id")
        val observatoryId: ObservatoryId,
        @JsonProperty("organization_id")
        val organizationId: OrganizationId
    )
}
