package org.orkg.common.annotations

import kotlin.annotation.AnnotationRetention.*
import org.springframework.security.access.prepost.PreAuthorize

@Retention(RUNTIME)
@PreAuthorize("isAuthenticated()")
annotation class RequireLogin

// TODO: Change when role was introduced
@Retention(RUNTIME)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
annotation class PreAuthorizeCurator

@Retention(RUNTIME)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
annotation class PreAuthorizeAdmin

@Retention(RUNTIME)
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
annotation class PreAuthorizeUser
