package org.orkg.common

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

fun Serializable.serializeToByteArray(): ByteArray =
    ByteArrayOutputStream().use { baos ->
        ObjectOutputStream(baos).run {
            flush()
            writeObject(this@serializeToByteArray)
        }
        baos
    }.toByteArray()

inline fun <reified T> ByteArray.deserializeToObject(): T =
    ByteArrayInputStream(this).use { bais ->
        ObjectInputStream(bais).readObject() as T
    }
