package org.orkg.featureflags.output

/**
 * Interface to provide access to feature flags.
 *
 * When flags are tested, their default values should be the same as in the production configuration.
 *
 * Flags should not be removed to decrease the risk of accidental reuse.
 * Use the [Deprecated] annotation and set `level` to [DeprecationLevel.HIDDEN] instead.
 * To find (and remove) all usages before that, use [DeprecationLevel.ERROR] and follow the compiler messages.
 */
interface FeatureFlagService {
    /** Determine if formatted labels are enabled and appended to resource representation. */
    fun isFormattedLabelsEnabled(): Boolean

    /** Determine if cache warmup is enabled **/
    fun isCacheWarmupEnabled(): Boolean
}
