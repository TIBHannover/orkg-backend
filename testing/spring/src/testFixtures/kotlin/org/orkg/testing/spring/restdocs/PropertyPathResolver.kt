package org.orkg.testing.spring.restdocs

import org.orkg.common.toCamelCase
import java.lang.reflect.Field
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.text.get

class PropertyPathResolver {
    companion object {
        fun resolve(path: String, enclosingClass: KClass<*>): PropertyPath? {
            val enclosingClasses = mutableListOf(enclosingClass.java)
            val pathNames = path.split(Regex("""\.|\["""))
            val fields = mutableListOf<Field>()
            val typeArguments = mutableListOf<Type>()
            pathNames.forEach { path ->
                if (path == "*") {
                    val parameterizedType = (typeArguments.lastOrNull() ?: fields.last().genericType) as ParameterizedType
                    typeArguments += parameterizedType.actualTypeArguments[1]
                } else if (path == "]") {
                    val type = (typeArguments.lastOrNull() ?: fields.last().genericType)
                    typeArguments += when (type) {
                        is ParameterizedType -> type.actualTypeArguments.first() // type declared via generics
                        is GenericArrayType -> type.genericComponentType
                        else -> (type as Class<*>).componentType
                    }
                } else {
                    val field = findDeclaredFieldByName(path, typeArguments.lastOrNull() as? Class<*> ?: enclosingClasses.last())
                        ?: return null
                    if (typeArguments.isNotEmpty()) {
                        enclosingClasses += typeArguments.last() as Class<*>
                    }
                    typeArguments.clear()
                    enclosingClasses += field.type
                    fields += field
                }
            }
            val fieldType = enclosingClasses.last()
            return PropertyPath(
                type = fieldType,
                typeArgument = typeArguments.lastOrNull() as? Class<*>?,
                enclosingClass = enclosingClasses[enclosingClasses.size - 2],
            )
        }

        private fun findDeclaredFieldByName(name: String, clazz: Class<*>): Field? {
            val camelCasePath = name.toCamelCase()
            val field = clazz.declaredFields.find { it.name == camelCasePath }
            return field // ?: throw IllegalArgumentException("""Field "$name" does not exist in class ${clazz.name}.""")
        }
    }

    data class PropertyPath(
        val type: Class<*>,
        val typeArgument: Class<*>?,
        val enclosingClass: Class<*>,
    )
}
