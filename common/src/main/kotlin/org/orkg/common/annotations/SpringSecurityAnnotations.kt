package org.orkg.common.annotations

import kotlin.annotation.AnnotationRetention.RUNTIME
import org.springframework.security.access.prepost.PreAuthorize

@Retention(RUNTIME)
@PreAuthorize("isAuthenticated()")
annotation class RequireLogin

@Retention(RUNTIME)
@PreAuthorize("hasAuthority('ROLE_CURATOR')")
annotation class RequireCuratorRole

@Retention(RUNTIME)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
annotation class RequireAdminRole
