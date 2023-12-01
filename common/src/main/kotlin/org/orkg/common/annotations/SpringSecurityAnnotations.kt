package org.orkg.common.annotations

import org.springframework.security.access.prepost.PreAuthorize

// TODO: Change when role was introduced
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
annotation class PreAuthorizeCurator

@PreAuthorize("hasAuthority('ROLE_ADMIN')")
annotation class PreAuthorizeAdmin