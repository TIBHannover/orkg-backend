package org.orkg.dataimport.domain.testing.asciidoc

import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.jobs.JobStatus

val allowedCSVStateValues =
    CSV.State.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")

val allowedJobStatusValues =
    JobStatus.Status.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")
