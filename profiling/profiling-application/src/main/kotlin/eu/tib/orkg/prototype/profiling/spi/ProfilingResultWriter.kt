package eu.tib.orkg.prototype.profiling.spi

import eu.tib.orkg.prototype.profiling.domain.FunctionResult

interface ProfilingResultWriter : AutoCloseable {
    operator fun invoke(result: FunctionResult)
}
