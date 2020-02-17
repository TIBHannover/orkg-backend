package eu.tib.orkg.prototype.statements.domain.model

interface StatsService {
    /**
     * Get stats
     *
     * @return the stats summary
     */
    fun getStats(): Stats

    fun getFieldsStats(): Map<String, Int>

    fun countPapersPerUser(): Map<String, Int>

    fun countPapersPerUserThisMonth(): Map<String, Int>

    fun countPapersPerUserThisWeek(): Map<String, Int>
}
