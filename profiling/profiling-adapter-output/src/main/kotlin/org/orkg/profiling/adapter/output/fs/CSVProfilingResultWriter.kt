package org.orkg.profiling.adapter.output.fs

import org.orkg.profiling.domain.FunctionResult
import org.orkg.profiling.output.ProfilingResultWriter
import org.orkg.profiling.output.ProfilingResultWriterFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

@Component
@Profile("profileRepositories")
class CSVProfilingResultWriterFactory : ProfilingResultWriterFactory {
    override fun invoke(file: File): ProfilingResultWriter =
        CSVProfilingResultWriter(file)
}

class CSVProfilingResultWriter(csv: File) : ProfilingResultWriter {
    private val writer: OutputStreamWriter = OutputStreamWriter(FileOutputStream(csv))

    init {
        if (!csv.exists()) {
            csv.createNewFile()
        }
    }

    override fun invoke(result: FunctionResult) {
        result.measurements.forEach { measurement ->
            val parameters = measurement.parameters.entries.joinToString(
                prefix = "\"",
                separator = "&",
                postfix = "\""
            ) {
                (it.key + "=" + it.value + "@" + it.value.javaClass.simpleName).replace("\"", "\"\"")
            }
            measurement.millis.forEach { time ->
                writer.write(result.repositoryName)
                writer.write(",")
                writer.write(result.functionName)
                writer.write(",")
                writer.write(time.toString())
                writer.write(",")
                writer.write(parameters)
                writer.write("\n")
            }
        }
    }

    override fun close() {
        writer.close()
    }
}
