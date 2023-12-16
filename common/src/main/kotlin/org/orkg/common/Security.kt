package org.orkg.common

import java.util.*
import org.orkg.common.exceptions.Unauthorized
import org.springframework.security.core.userdetails.UserDetails

fun UserDetails?.contributorId(): ContributorId =
    this?.username?.let { ContributorId(UUID.fromString(it)) } ?: throw Unauthorized()
