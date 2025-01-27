package org.orkg.community.testing.asciidoc

import org.orkg.community.domain.OrganizationType

val allowedOrganizationTypeValues =
    OrganizationType.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")
