package org.orkg.community.adapter.output.jpa.internal

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresContributorRepository : JpaRepository<ContributorEntity, UUID>
