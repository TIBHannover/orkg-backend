package org.orkg.dataimport.domain.internal

import org.apache.commons.csv.CSVRecord
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.BlankCSVHeaderValue
import org.orkg.dataimport.domain.DuplicateCSVHeaders
import org.orkg.dataimport.domain.EmptyCSVHeader
import org.orkg.dataimport.domain.InconsistentCSVColumnCount
import org.orkg.dataimport.domain.InvalidCSVValue
import org.orkg.dataimport.domain.Namespace
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.UnexpectedCSVValueType
import org.orkg.dataimport.domain.UnknownCSVNamespace
import org.orkg.dataimport.domain.UnknownCSVNamespaceValue
import org.orkg.dataimport.domain.UnknownCSVValueType
import org.orkg.dataimport.domain.csv.CSVHeader
import org.orkg.dataimport.domain.csv.CSVSchema
import org.orkg.graph.domain.Literals
import java.util.regex.Pattern

data class SchemaBasedCSVRecordParser(
    private val schema: CSVSchema,
) {
    fun parseRecord(record: CSVRecord, headers: List<CSVHeader>): List<TypedValue> =
        parseRecord(record.toList(), record.recordNumber, headers)

    fun parseRecord(values: List<String>, row: Long, headers: List<CSVHeader>): List<TypedValue> {
        if (values.size != headers.size) {
            throw InconsistentCSVColumnCount(values.size, headers.size, row)
        }
        val record = mutableListOf<TypedValue>()
        val exceptions = mutableListOf<Throwable>()
        values.forEachIndexed { index, value ->
            try {
                record += parseValue(value, row, index.toLong().inc(), headers[index])
            } catch (t: Throwable) {
                exceptions += t
            }
        }
        if (exceptions.isNotEmpty()) {
            throw RecordParsingException(exceptions)
        }
        return record
    }

    private fun parseValue(input: String, row: Long, column: Long, header: CSVHeader): TypedValue {
        val parsed = Value.parse(input, row, column, schema.typeMappings)
        val valueNamespace = parsed.namespace?.let { schema.values[it.trim()] }
        val headerNamespace = schema.headers[header.namespace]
        val value = parsed.format(valueNamespace)
        val type = validateType(header, valueNamespace, headerNamespace, value, parsed.type, row, column)
        validateValue(header, valueNamespace, headerNamespace, value, row, column)
        return TypedValue(valueNamespace?.name, value, type)
    }

    private fun validateValue(header: CSVHeader, valueNs: Namespace?, headerNs: Namespace?, value: String?, row: Long, column: Long) {
        if (value != null) {
            if (valueNs != null) {
                valueNs.validateClosedValue(value, row, column)
                valueNs.validateColumnValueConstraint(value, row, column)
            }
            if (headerNs != null) {
                headerNs.validateColumnValueConstraint(value, row, column)
                if (headerNs.closed) {
                    val headerProperty = headerNs.properties[header.name]
                    try {
                        headerProperty?.validator?.invoke(value)
                    } catch (t: Throwable) {
                        throw InvalidCSVValue(value, row, column, t)
                    }
                }
            }
        }
    }

    private fun validateType(header: CSVHeader, valueNs: Namespace?, headerNs: Namespace?, value: String?, type: ThingId?, row: Long, column: Long): ThingId {
        val declaredType = type ?: valueNs?.typeForValue(value) ?: header.columnType ?: Literals.XSD.fromValue(value.orEmpty()).`class`
        val expectedType = valueNs?.columnValueType ?: header.columnType.takeIf { headerNs?.closed == true }
        if (expectedType != null && declaredType != expectedType) {
            throw UnexpectedCSVValueType(declaredType, expectedType, row, column)
        }
        if (value != null) {
            Literals.XSD.fromClass(declaredType)?.also {
                if (!it.canParse(value)) {
                    throw InvalidCSVValue(value, row, column, declaredType)
                }
            }
        }
        return declaredType
    }

    fun parseHeader(record: CSVRecord): List<CSVHeader> =
        parseHeader(record.toList())

    fun parseHeader(values: List<String>): List<CSVHeader> {
        if (values.isEmpty()) {
            throw EmptyCSVHeader()
        }
        val headers = mutableListOf<CSVHeader>()
        val exceptions = mutableListOf<Throwable>()
        values.forEachIndexed { index, value ->
            try {
                headers += parseHeader(value, index.toLong().inc())
            } catch (t: Throwable) {
                exceptions += t
            }
        }
        if (exceptions.isNotEmpty()) {
            throw RecordParsingException(exceptions)
        }
        val duplicateSchemaHeaders = headers
            .filter { it.namespace?.let { schema.headers[it] }?.closed == true }
            .groupBy { it.namespace + ":" + it.name }
            .filter { it.value.size > 1 }
            .mapValues { (_, value) -> value.map { it.column } }
        if (duplicateSchemaHeaders.isNotEmpty()) {
            throw DuplicateCSVHeaders(duplicateSchemaHeaders)
        }
        return headers
    }

    private fun parseHeader(input: String, column: Long): CSVHeader {
        val parsed = Value.parse(input, 1, column, schema.typeMappings)
        if (parsed.value == null) {
            throw BlankCSVHeaderValue(column)
        }
        val namespace: Namespace? = parsed.namespace?.trim()?.let {
            schema.headers[it] ?: throw UnknownCSVNamespace(it, parsed.value, 1, column)
        }
        val value = parsed.format(namespace)!!
        validateHeader(namespace, value, parsed.type, 1, column)
        return CSVHeader(
            column = column,
            namespace = namespace?.name,
            name = value,
            columnType = namespace?.typeForValue(value) ?: parsed.type
        )
    }

    private fun validateHeader(namespace: Namespace?, value: String?, type: ThingId?, row: Long, column: Long) {
        if (value != null && namespace != null) {
            namespace.validateClosedValue(value, row, column)
            namespace.validateHeaderValueConstraint(value, row, column)
            if (namespace.closed) {
                val property = namespace.properties[value]!!
                if (type != null && property.type != type) {
                    throw UnexpectedCSVValueType(type, property.type, row, column)
                }
            }
        }
    }

    private fun Namespace.validateClosedValue(value: String, row: Long, column: Long) {
        if (closed && value !in properties) {
            throw UnknownCSVNamespaceValue(name, value, row, column)
        }
    }

    private fun Namespace.validateColumnValueConstraint(value: String, row: Long, column: Long) {
        try {
            columnValueConstraint?.invoke(value)
        } catch (t: Throwable) {
            throw InvalidCSVValue(value, row, column, t)
        }
    }

    private fun Namespace.validateHeaderValueConstraint(value: String, row: Long, column: Long) {
        try {
            headerValueValidator?.invoke(value)
        } catch (t: Throwable) {
            throw InvalidCSVValue(value, row, column, t)
        }
    }

    private data class Value(
        val namespace: String?,
        val value: String?,
        val type: ThingId?,
    ) {
        fun format(foundNamespace: Namespace?): String? {
            if (foundNamespace == null && namespace != null && value != null) {
                return "$namespace:$value".trim()
            }
            return value?.trim()
        }

        companion object {
            private val valueWithDataType: Pattern =
                Pattern.compile("""^(?:([\w-]+):)?([\s\S]*?)(?:<([^\n]+)>)?$""")

            fun parse(input: String, row: Long, column: Long, typeMappings: Map<String, ThingId>): Value {
                val matcher = valueWithDataType.matcher(input).also { it.find() }
                val namespace = matcher.group(1)
                val value = matcher.group(2).takeIf { it.isNotEmpty() }
                val type = matcher.group(3)?.let { typeMappings[it] ?: throw UnknownCSVValueType(it, row, column) }
                return Value(namespace, value, type)
            }
        }
    }
}
