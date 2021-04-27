package org.orkg.core

import com.github.michaelbull.result.Result
import org.orkg.core.domain.Statement
import org.orkg.core.domain.Triple
import java.util.UUID

interface CreateStatementUseCase {
    fun create(command: CreateStatementCommand): Result<Statement, Throwable>

    data class CreateStatementCommand(val triple: Triple, val creator: UUID)
}

interface RetrieveStatementUseCase {
    fun byId(): Result<Statement, Throwable>
}

interface CreateResourceUseCase
interface CreateClassUseCase
