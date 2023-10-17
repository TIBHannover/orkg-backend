package eu.tib.orkg.prototype.profiling.spi

import java.io.File

interface ProfilingResultWriterFactory {
    operator fun invoke(file: File): ProfilingResultWriter
}
