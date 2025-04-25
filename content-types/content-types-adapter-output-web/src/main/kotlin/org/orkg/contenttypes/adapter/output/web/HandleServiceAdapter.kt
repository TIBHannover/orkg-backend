package org.orkg.contenttypes.adapter.output.web

import net.handle.api.HSAdapter
import net.handle.api.HSAdapterFactory
import net.handle.hdllib.HandleException
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.contenttypes.domain.identifiers.Handle
import org.orkg.contenttypes.output.HandleService
import org.orkg.contenttypes.output.HandleService.RegisterCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

internal const val TERM_IDENTIFIER = "https://doi.org/21.T11969/d56564a9795e9d9c1ab4"

@Component
class HandleServiceAdapter(
    @Value("\${orkg.handles.prefix}")
    private val prefix: String,
    @Value("\${orkg.handles.admin-handle}")
    private val adminHandle: String,
    @Value("\${orkg.handles.key-index}")
    private val keyIndex: Int,
    @Value("\${orkg.handles.password}")
    private val secretKey: String,
) : HandleService {
    private val adapter: Result<HSAdapter> by lazy {
        try {
            Result.success(HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()))
        } catch (e: HandleException) {
            Result.failure(e)
        }
    }

    override fun register(command: RegisterCommand): Handle {
        val adapter = adapter.getOrElse { exception ->
            throw ServiceUnavailable.create("Handle", exception)
        }
        val handle = Handle.of("$prefix/${command.suffix}")
        try {
            adapter.createHandle(
                handle.value,
                arrayOf(
                    adapter.createHandleValue(1, "TEXT", TERM_IDENTIFIER),
                    adapter.createHandleValue(2, "URL", command.url.toString()),
                    adapter.createHandleValue(3, "TEXT", "ORKG"),
                ),
            )
        } catch (e: HandleException) {
            throw ServiceUnavailable.create("Handle", e)
        }
        return handle
    }

    override fun delete(handle: Handle) {
        val adapter = adapter.getOrElse { exception ->
            throw ServiceUnavailable.create("Handle", exception)
        }
        try {
            adapter.deleteHandle(handle.value)
        } catch (e: HandleException) {
            if (e.code != HandleException.HANDLE_DOES_NOT_EXIST) {
                throw ServiceUnavailable.create("Handle", e)
            }
        }
    }
}
