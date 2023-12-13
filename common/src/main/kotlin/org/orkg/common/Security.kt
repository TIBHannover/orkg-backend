package org.orkg.common

import java.util.*
import org.springframework.security.core.userdetails.UserDetails

fun UserDetails.contributorId(): ContributorId = UUID.fromString(this.username).let(::ContributorId)
