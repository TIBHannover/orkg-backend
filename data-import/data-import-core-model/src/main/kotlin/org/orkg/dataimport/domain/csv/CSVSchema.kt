package org.orkg.dataimport.domain.csv

import org.orkg.common.ThingId
import org.orkg.dataimport.domain.Namespace
import org.orkg.dataimport.domain.Property
import org.orkg.graph.domain.Classes

data class CSVSchema(
    val headers: Map<String, Namespace>,
    val values: Map<String, Namespace>,
    val typeMappings: Map<String, ThingId>,
) {
    constructor(spec: SchemaScope.() -> Unit) : this(mutableMapOf(), mutableMapOf(), mutableMapOf()) {
        spec(SchemaScope(headers as MutableMap, values as MutableMap, typeMappings as MutableMap))
    }

    @DslMarker
    @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
    annotation class SchemaDSL

    @SchemaDSL
    data class SchemaScope(
        private val headers: MutableMap<String, Namespace>,
        private val values: MutableMap<String, Namespace>,
        private val typeMappings: MutableMap<String, ThingId>,
    ) {
        fun header(spec: HeaderScope.() -> Unit) {
            spec(HeaderScope(headers))
        }

        fun value(spec: ValueScope.() -> Unit) {
            spec(ValueScope(values))
        }

        fun types(spec: TypeScope.() -> Unit) {
            spec(TypeScope(typeMappings))
        }
    }

    @SchemaDSL
    data class HeaderScope(private val headers: MutableMap<String, Namespace>) {
        fun namespace(
            name: String,
            spec: HeaderNamespaceScope.() -> Unit = {},
        ) {
            val scope = HeaderNamespaceScope().apply(spec)
            val namespace = Namespace(
                name = name,
                closed = scope.closed,
                properties = scope.properties,
                columnValueType = scope.columnValueType,
                columnValueConstraint = scope.columnValueConstraint,
                headerValueValidator = scope.headerValueValidator
            )
            headers[namespace.name] = namespace
        }
    }

    @SchemaDSL
    data class ValueScope(private val values: MutableMap<String, Namespace>) {
        fun namespace(
            name: String,
            spec: NamespaceScope.() -> Unit = {},
        ) {
            val scope = NamespaceScope().apply(spec)
            val namespace = Namespace(name, scope.closed, scope.properties, scope.columnValueType, scope.columnValueConstraint)
            values[namespace.name] = namespace
        }
    }

    @SchemaDSL
    data class TypeScope(private val typeMappings: MutableMap<String, ThingId>) {
        fun type(type: String, classId: ThingId) {
            typeMappings.set(type, classId)
        }
    }

    @SchemaDSL
    sealed class AbstractNamespaceScope(
        internal val properties: MutableMap<String, Property> = mutableMapOf(),
        internal var closed: Boolean = false,
        internal var columnValueType: ThingId? = null,
        internal var columnValueConstraint: ((String) -> Unit)? = null,
    ) {
        fun open() {
            this.closed = false
        }

        fun closed() {
            this.closed = true
        }

        fun columnValueType(type: ThingId) {
            columnValueType = type
        }

        fun columnValueConstraint(validator: (String) -> Unit) {
            columnValueConstraint = validator
        }

        fun value(name: String, spec: NamespaceValueScope.() -> Unit = {}) {
            val scope = NamespaceValueScope().apply(spec)
            properties[name] = Property(name, scope.columnValueType, scope.columnValueContraint)
        }
    }

    class NamespaceScope : AbstractNamespaceScope()

    data class HeaderNamespaceScope(
        internal var headerValueValidator: ((String) -> Unit)? = null,
    ) : AbstractNamespaceScope() {
        fun headerValueConstraint(validator: (String) -> Unit) {
            headerValueValidator = validator
        }
    }

    @SchemaDSL
    data class NamespaceValueScope(
        internal var columnValueType: ThingId = Classes.string,
        internal var columnValueContraint: ((String) -> Unit)? = null,
    ) {
        fun columnValueType(type: ThingId) {
            columnValueType = type
        }

        fun columnValueConstraint(validator: (String) -> Unit) {
            columnValueContraint = validator
        }
    }
}
