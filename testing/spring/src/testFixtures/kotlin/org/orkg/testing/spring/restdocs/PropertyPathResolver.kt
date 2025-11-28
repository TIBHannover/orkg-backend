package org.orkg.testing.spring.restdocs

import org.orkg.common.toCamelCase
import java.lang.reflect.Field
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

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
                    typeArguments += type.firstGenericType()
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
            val lastField = fields.last()
            val fieldName = lastField.name.takeIf { typeArguments.isEmpty() }
            val fieldType = (if (typeArguments.size > 1) typeArguments[typeArguments.lastIndex - 1] else lastField.genericType).toClass()!!
            if (typeArguments.isEmpty()) {
                if (fieldType.isArray || Iterable::class.java.isAssignableFrom(fieldType)) {
                    typeArguments += lastField.genericType.firstGenericType()
                } else if (Map::class.java.isAssignableFrom(fieldType)) {
                    typeArguments += (lastField.genericType as ParameterizedType).actualTypeArguments[1]
                }
            }
            return PropertyPath(
                fieldName = fieldName,
                fieldType = fieldType,
                typeArgument = typeArguments.lastOrNull()?.toClass(),
                enclosingField = enclosingClasses.last(),
                enclosingClass = enclosingClasses[enclosingClasses.size - 2],
            )
        }

        private fun Type.toClass(): Class<out Any>? =
            when (this) {
                is ParameterizedType -> rawType.toClass()
                is GenericArrayType -> Array::class.java // return generic array because we cannot convert GenericArrayType to Class<*>
                else -> this as? Class<*>
            }

        private fun Type.firstGenericType(): Type =
            when (this) {
                is ParameterizedType -> actualTypeArguments.first() // type declared via generics
                is GenericArrayType -> genericComponentType
                else -> (this as Class<*>).componentType
            }

        private fun findDeclaredFieldByName(name: String, clazz: Class<*>): Field? {
            val camelCasePath = name.toCamelCase()
            val field = clazz.declaredFields.find { it.name == camelCasePath }
            return field // ?: throw IllegalArgumentException("""Field "$name" does not exist in class ${clazz.name}.""")
        }
    }

    data class PropertyPath(
        val fieldName: String?,
        val fieldType: Class<*>,
        val typeArgument: Class<*>?,
        val enclosingField: Class<*>,
        val enclosingClass: Class<*>,
    )
}
