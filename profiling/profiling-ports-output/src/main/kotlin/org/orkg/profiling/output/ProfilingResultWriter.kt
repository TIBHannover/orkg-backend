package org.orkg.profiling.output

import org.orkg.profiling.domain.FunctionResult

interface ProfilingResultWriter : AutoCloseable {
    operator fun invoke(result: FunctionResult)
}
