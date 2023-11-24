package org.orkg.graph.adapter.input.rest

import java.util.*
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * Base class for all controllers.
 *
 * Provides helper methods, e.g. for accessing the ID of the currently logged-in user.
 */
@Deprecated("To be removed")
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
}
