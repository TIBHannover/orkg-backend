package org.orkg.community.output

import org.orkg.common.ContributorId

interface AdminRepository {
    fun hasAdminPriviledges(id: ContributorId): Boolean
}
