package org.orkg.contenttypes.output

import org.orkg.common.Handle
import java.net.URI

interface HandleService {
    fun register(command: RegisterCommand): Handle

    fun delete(handle: Handle)

    data class RegisterCommand(
        val suffix: String,
        val url: URI,
    )
}
