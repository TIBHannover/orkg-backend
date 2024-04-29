package org.orkg.profiling.output

import java.io.File

interface ProfilingResultWriterFactory {
    operator fun invoke(file: File): ProfilingResultWriter
}
