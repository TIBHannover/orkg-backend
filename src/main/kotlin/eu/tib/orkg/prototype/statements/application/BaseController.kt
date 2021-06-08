package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.auth.service.OrkgUserRepository
import java.util.UUID
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal
import java.util.logging.Logger

/**
 * Base class for all controllers.
 *
 * Provides helper methods, e.g. for accessing the ID of the currently logged-in user.
 */
abstract class BaseController(
    private val orkgUserRepository: OrkgUserRepository) {
    /**
     * Determine the ID of the currently logged in user from the security context.
     *
     * @return The [UUID] of the user of an all-zero UUID if the user is not logged in.
     */
    protected fun authenticatedUserId(principal: Principal): UUID? {
        /*val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is AnonymousAuthenticationToken) return UUID(0, 0)
        val principal = authentication.principal
        val userIdAsString: String = if (principal is UserDetails) principal.username else principal.toString()
        return UUID.fromString(userIdAsString)*/
        val user = orkgUserRepository.findByDisplayName(principal.name)
        if(user.isPresent){
            return user.get().keycloakID
        }

        return UUID(0,0)
    }

    protected fun loggedInUserId(principal: Principal): UUID? {
        val user = orkgUserRepository.findByDisplayName(principal.name)
        if(user.isPresent){
            return user.get().keycloakID
        }

        return UUID(0,0)
    }


}
