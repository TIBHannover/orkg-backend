import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

val dst2019Start = OffsetDateTime.parse("2019-03-31T02:00:00.000000+01:00")
val dst2019End = OffsetDateTime.parse("2019-10-27T02:00:00.000000+02:00")
val dst2020Start = OffsetDateTime.parse("2020-03-30T02:00:00.000000+01:00")
val dst2020End = OffsetDateTime.parse("2020-10-29T02:00:00.000000+02:00")
val dst2021Start = OffsetDateTime.parse("2021-03-30T02:00:00.000000+01:00")
val dst2021End = OffsetDateTime.parse("2021-10-28T02:00:00.000000+02:00")
val dst2022Start = OffsetDateTime.parse("2022-03-30T02:00:00.000000+01:00")
val dst2022End = OffsetDateTime.parse("2022-10-27T02:00:00.000000+02:00")
val dst2023Start = OffsetDateTime.parse("2023-03-30T02:00:00.000000+01:00")
val dst2023End = OffsetDateTime.parse("2023-10-26T02:00:00.000000+02:00")
val dst2024Start = OffsetDateTime.parse("2024-03-30T02:00:00.000000+01:00")
val dst2024End = OffsetDateTime.parse("2024-10-31T02:00:00.000000+02:00")
val dst2025Start = OffsetDateTime.parse("2025-03-30T02:00:00.000000+01:00")
val dst2025End = OffsetDateTime.parse("2025-10-26T02:00:00.000000+01:00")

val dstDates = listOf(
    dst2019Start,
    dst2019End,
    dst2020Start,
    dst2020End,
    dst2021Start,
    dst2021End,
    dst2022Start,
    dst2022End,
    dst2023Start,
    dst2023End,
    dst2024Start,
    dst2024End,
    dst2025Start,
    dst2025End,
)

val tableToColumn = mapOf(
    "contributors" to "joined_at",
    "observatory_filters" to "created_at",
    "images" to "created_at",
    "template_based_resource_snapshots" to "created_at"
)
val dstIntervals = dstDates.windowed(2, 2)
    .map { (from, to) -> from to to }
val nonDstIntervals = dstDates.drop(1).windowed(2, 2)
    .map { (from, to) -> from to to }
val offsetToInterval = mapOf(
    TimeUnit.HOURS.toSeconds(1) to nonDstIntervals,
    TimeUnit.HOURS.toSeconds(2) to dstIntervals
)

val query = buildString {
    offsetToInterval.forEach { (offset, intervals) ->
        tableToColumn.entries.forEach { (table, column) ->
            val condition = intervals.joinToString(separator = " OR\n") { (from, to) ->
                "($column > TIMESTAMP '${from.atZoneSameInstant(ZoneOffset.UTC)}' AND $column < TIMESTAMP '${to.atZoneSameInstant(ZoneOffset.UTC)}')"
            }
            appendLine(
                """
                UPDATE $table
                    SET ${column}_offset_total_seconds = $offset
                    WHERE $condition;
                """.trimIndent()
            )
        }
    }
}

println(query)
