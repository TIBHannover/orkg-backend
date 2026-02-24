package org.orkg.contenttypes.adapter.output.jpa.internal

import org.springframework.data.jpa.repository.JpaRepository

interface PostgresComparisonTableRepository : JpaRepository<ComparisonTableEntity, String>
