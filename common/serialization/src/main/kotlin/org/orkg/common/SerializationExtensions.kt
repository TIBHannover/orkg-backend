package org.orkg.common

import tools.jackson.core.JsonParser
import tools.jackson.core.TreeNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass

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

fun <T : Any> JsonParser.treeToValue(node: TreeNode, type: KClass<T>) =
    objectReadContext().treeAsTokens(node).readValueAs(type.java)
