package org.orkg.testing.spring.restdocs

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.testing.spring.restdocs.PropertyPathResolver.PropertyPath
import java.util.stream.Stream

internal class PropertyPathResolverUnitTest {
    @ParameterizedTest
    @MethodSource("arguments")
    fun `Given a the path of a property and an enclosing class, when resolving, it is resolved correctly`(pair: Pair<String, PropertyPath>) {
        val (path, propertyPath) = pair
        PropertyPathResolver.resolve(path, Example::class) shouldBe propertyPath
    }

    companion object {
        @JvmStatic
        fun arguments(): Stream<Arguments> = Stream.of(
            "simple" to PropertyPath(
                fieldName = "simple",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = Example::class.java
            ),
            "list" to PropertyPath(
                fieldName = "list",
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "list[]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "list[].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "array" to PropertyPath(
                fieldName = "array",
                fieldType = Array<SomeDataClass>::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Array<SomeDataClass>::class.java,
                enclosingClass = Example::class.java
            ),
            "array[]" to PropertyPath(
                fieldName = null,
                fieldType = Array<SomeDataClass>::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Array<SomeDataClass>::class.java,
                enclosingClass = Example::class.java
            ),
            "array[].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "arrayOfList[]" to PropertyPath(
                fieldName = null,
                fieldType = Array::class.java,
                typeArgument = List::class.java,
                enclosingField = arrayOf<List<*>>()::class.java, // FIXME: It is not possible to declare an array of lists in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfList[][]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = arrayOf<List<*>>()::class.java, // FIXME: It is not possible to declare an array of lists in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfList[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "listArray[]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = Array<SomeDataClass>::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "listArray[][]" to PropertyPath(
                fieldName = null,
                fieldType = Array<SomeDataClass>::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "listArray[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "nestedList[]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = List::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedList[][]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = List::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedList[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "map" to PropertyPath(
                fieldName = "map",
                fieldType = Map::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "map.*" to PropertyPath(
                fieldName = null,
                fieldType = Map::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "map.*.first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "nestedMap" to PropertyPath(
                fieldName = "nestedMap",
                fieldType = Map::class.java,
                typeArgument = List::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedMap.*" to PropertyPath(
                fieldName = null,
                fieldType = Map::class.java,
                typeArgument = List::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedMap.*[]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = List::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedMap.*[][]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedMap.*[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "nestedArrayMap" to PropertyPath(
                fieldName = "nestedArrayMap",
                fieldType = Map::class.java,
                typeArgument = Array::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedArrayMap.*" to PropertyPath(
                fieldName = null,
                fieldType = Map::class.java,
                typeArgument = Array::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedArrayMap.*[]" to PropertyPath(
                fieldName = null,
                fieldType = Array::class.java,
                typeArgument = List::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedArrayMap.*[][]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = Map::class.java,
                enclosingClass = Example::class.java
            ),
            "nestedArrayMap.*[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
            "arrayOfMaps[]" to PropertyPath(
                fieldName = null,
                fieldType = Array::class.java,
                typeArgument = Map::class.java,
                enclosingField = arrayOf<Map<*, *>>()::class.java, // FIXME: It is not possible to declare an array of maps in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfMaps[].*" to PropertyPath(
                fieldName = null,
                fieldType = Map::class.java,
                typeArgument = Array::class.java,
                enclosingField = arrayOf<Map<*, *>>()::class.java, // FIXME: It is not possible to declare an array of maps in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfMaps[].*[]" to PropertyPath(
                fieldName = null,
                fieldType = Array::class.java,
                typeArgument = List::class.java,
                enclosingField = arrayOf<Map<*, *>>()::class.java, // FIXME: It is not possible to declare an array of maps in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfMaps[].*[][]" to PropertyPath(
                fieldName = null,
                fieldType = List::class.java,
                typeArgument = SomeDataClass::class.java,
                enclosingField = arrayOf<Map<*, *>>()::class.java, // FIXME: It is not possible to declare an array of maps in Kotlin without using an object instance.
                enclosingClass = Example::class.java
            ),
            "arrayOfMaps[].*[][].first" to PropertyPath(
                fieldName = "first",
                fieldType = String::class.java,
                typeArgument = null,
                enclosingField = String::class.java,
                enclosingClass = SomeDataClass::class.java
            ),
        ).map(Arguments::of)
    }

    data class Example(
        val simple: String,
        val list: List<SomeDataClass>,
        val array: Array<SomeDataClass>,
        val nestedList: List<List<SomeDataClass>>,
        val arrayOfList: Array<List<SomeDataClass>>,
        val listArray: List<Array<SomeDataClass>>,
        val map: Map<String, SomeDataClass>,
        val nestedMap: Map<String, List<List<SomeDataClass>>>,
        val nestedArrayMap: Map<String, Array<List<SomeDataClass>>>,
        val arrayOfMaps: Array<Map<String, Array<List<SomeDataClass>>>>,
    )

    data class SomeDataClass(
        val first: String,
        val second: String,
    )
}
