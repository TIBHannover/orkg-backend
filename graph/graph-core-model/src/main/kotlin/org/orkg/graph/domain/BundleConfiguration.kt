package org.orkg.graph.domain

import org.orkg.common.ThingId

/**
 * A Bundle configuration class containing the min and max levels to be fetched
 * Also the list of classes to be white-listed or black-listed during the fetch
 * @param minLevel the minimum level to be fetched (if not provided it is set to 0)
 * @param maxLevel the maximum level of statements to be fetched (if not provided, all child statements will be fetched)
 * @param blacklist the list of classes to be black-listed (i.e. not fetched), these classes are checked on the subjects and objects of a statement
 * @param whitelist the list of classes to be white-listed (i.e. the only ones to be fetched), these classes are checked on the subjects and objects of a statement
 */
data class BundleConfiguration(
    val minLevel: Int?,
    val maxLevel: Int?,
    val blacklist: kotlin.collections.List<ThingId>,
    val whitelist: kotlin.collections.List<ThingId>,
) {
    companion object Factory {
        fun firstLevelConf(): BundleConfiguration =
            BundleConfiguration(minLevel = null, maxLevel = 1, blacklist = emptyList(), whitelist = emptyList())
    }
}
