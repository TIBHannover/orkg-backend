package org.orkg.dataimport.domain.testing.asciidoc

import org.orkg.dataimport.domain.jobs.JobStatus

val allowedJobStatusValues =
    JobStatus.Status.entries.sorted().joinToString(separator = ", ", prefix = "`", postfix = "`")
