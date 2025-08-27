package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.output.ContributorRepository
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.input.UpdateCSVUseCase
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.item.ExecutionContext

fun UpdateCSVUseCase.UpdateCommand.hasNoContents(): Boolean =
    name == null &&
        data == null &&
        type == null &&
        format == null

fun CSV.apply(command: UpdateCSVUseCase.UpdateCommand): CSV =
    copy(
        name = command.name ?: name,
        data = command.data?.takeIf { it.isNotBlank() } ?: data,
        type = command.type ?: type,
        format = command.format ?: format
    )

internal inline fun <reified T : Any> JobParametersBuilder.add(name: String, value: T, identifying: Boolean = true) =
    addJobParameter(name, value, T::class.java, identifying)

internal inline operator fun <reified T> JobParameters.get(name: String): T =
    getParameter(name).also { require(T::class.java.isAssignableFrom(it.type)) }.value as T

internal inline fun <reified T> ExecutionContext.getAndCast(name: String): T? =
    get(name, T::class.java)

internal inline fun <reified T> ExecutionContext.getOrDefault(name: String, noinline defaultValue: () -> T): T =
    getAndCast<T>(name) ?: defaultValue()

internal fun ContributorRepository.isAdmin(contributorId: ContributorId) =
    findById(contributorId).orElseThrow { ContributorNotFound(contributorId) }.isAdmin
