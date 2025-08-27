package org.orkg.dataimport.domain.csv.papers

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.TypedValue
import java.io.Serial
import java.io.Serializable

data class ContributionStatement(
    val predicate: Either<ThingId, String>,
    val `object`: TypedValue,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -2813193385293434568L
    }
}
