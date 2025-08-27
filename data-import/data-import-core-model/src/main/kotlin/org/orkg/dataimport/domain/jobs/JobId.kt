package org.orkg.dataimport.domain.jobs

data class JobId(val value: Long) {
    constructor(value: String) : this(value.toLongOrNull() ?: throw IllegalArgumentException("Job id must only contain numbers."))

    override fun toString(): String = value.toString()
}
