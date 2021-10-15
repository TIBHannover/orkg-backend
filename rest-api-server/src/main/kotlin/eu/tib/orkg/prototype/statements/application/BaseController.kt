package eu.tib.orkg.prototype.statements.application

import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal
import java.util.UUID
import java.util.logging.Logger


/**
 * Base class for all controllers.
 *
 * Provides helper methods, e.g. for accessing the ID of the currently logged-in user.
 */
abstract class BaseController {
    /**
     * Determine the ID of the currently logged in user from the security context.
     *
     * @return The [UUID] of the user of an all-zero UUID if the user is not logged in.
     */
    protected fun authenticatedUserId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is AnonymousAuthenticationToken) return UUID(0, 0)
        val principal = authentication.principal
        val userIdAsString: String = if (principal is UserDetails) principal.username else principal.toString()
        return UUID.fromString(userIdAsString)
    }

    protected fun keycloakAuthenticatedUserId(): UUID{
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is AnonymousAuthenticationToken) return UUID(0, 0)

        val principal = authentication.principal as KeycloakPrincipal<*>
        var uuid = UUID.randomUUID()

        if (principal is KeycloakPrincipal<*>) {
            uuid = UUID.fromString(principal.keycloakSecurityContext.token.subject!!)
            val logger = Logger.getLogger("UUID logger")
            logger.info("UUID:  $uuid")
        }

        return uuid
    }
}
