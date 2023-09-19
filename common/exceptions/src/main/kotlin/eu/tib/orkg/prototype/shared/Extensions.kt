package eu.tib.orkg.prototype.shared

inline fun <K, V, R : Any> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?) : Map<K, R> =
    LinkedHashMap<K, R>(size).also { result ->
        entries.forEach { entry -> transform(entry)?.let { result[entry.key] = it } }
    }
