#!/usr/bin/env kscript

import java.io.File

val fileA = args.getOrElse(0) { "before.csv" }
val fileB = args.getOrElse(1) { "after.csv" }

val mapA = readCSV(fileA)
val mapB = readCSV(fileB)

var totalTimeA: Long = 0
var totalTimeB: Long = 0
var totalMethodCount = 0

println("Comparing $fileA (A) to $fileB (B)")

(mapA.keys intersect mapB.keys).forEach { repo ->
    println("$repo:")
    val methodsA = computeMethodResults(mapA[repo]!!)
    val methodsB = computeMethodResults(mapB[repo]!!)
    var timeA: Long = 0
    var timeB: Long = 0
    var methodCount = 0

    (methodsA.keys intersect methodsB.keys).forEach { method ->
        val resultA = methodsA[method]!!
        val resultB = methodsB[method]!!
        print("  ")
        print("avg: ${resultA.average} ${resultB.average} ${resultB.average - resultA.average} ")
        print("median: ${resultA.median} ${resultB.median} ${resultB.median - resultA.median} ")
        println(method)
        timeA += resultA.average
        timeB += resultB.average
        methodCount++
    }

    println("Repository average: ${timeA.toDouble() / methodCount} ${timeB.toDouble() / methodCount} ${(timeB - timeA).toDouble() / methodCount}")
    println()

    totalTimeA += timeA
    totalTimeB += timeB
    totalMethodCount += methodCount
}

println("Total average: ${totalTimeA.toDouble() / totalMethodCount} ${totalTimeB.toDouble() / totalMethodCount} ${(totalTimeB - totalTimeA).toDouble() / totalMethodCount}")
println("Ratio A/B: ${totalTimeA.toDouble() / totalTimeB.toDouble()}")
println("Ratio B/A: ${totalTimeB.toDouble() / totalTimeA.toDouble()}")

fun computeMethodResults(measurements: List<Measurement>): Map<String, ProfilingResult> {
    val methodToResult: MutableMap<String, MutableList<Long>> = mutableMapOf()
    measurements.forEach {
        methodToResult.getOrPut(it.method) { mutableListOf() } += it.time
    }
    return methodToResult.mapValues { (_, times) ->
        ProfilingResult(times.sum() / times.size, times.median())
    }
}

fun readCSV(csv: String): MutableMap<String, MutableList<Measurement>> {
    val result: MutableMap<String, MutableList<Measurement>> = mutableMapOf()
    File(csv).useLines { lines ->
        lines.forEach { line ->
            val split = line.split(",")
            val measurement = Measurement(
                repository = split[0],
                method = split[1],
                time = split[2].toLong()
            )
            result.getOrPut(measurement.repository) { mutableListOf() } += measurement
        }
    }
    return result
}

data class Measurement(
    val repository: String,
    val method: String,
    val time: Long
)

data class ProfilingResult(
    val average: Long,
    val median: Long
)

fun List<Long>.median() =
    sorted().let {
        if (size % 2 == 0)
            (this[size / 2] + this[(size - 1) / 2]) / 2
        else
            this[size / 2]
    }
