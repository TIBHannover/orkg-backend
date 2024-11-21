package org.orkg.common

import org.orkg.common.exceptions.Unauthorized
import org.springframework.security.core.Authentication

fun Authentication?.contributorId(): ContributorId =
    this?.name?.let(::ContributorId) ?: throw Unauthorized()
