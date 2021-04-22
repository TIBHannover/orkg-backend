package org.orkg.core.adapter.output

import com.github.michaelbull.result.Result
import org.orkg.core.domain.Statement
import org.orkg.core.domain.Triple

// TODO: extract repo interface (ro, wo)

interface StatementRepository {
  fun byId(id: Statement.Id): Result<Statement, Throwable>
  fun byTriple(triple: Triple): Result<Statement, Throwable>
}
