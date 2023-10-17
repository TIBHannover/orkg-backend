#!/usr/bin/env kscript

import java.io.File

val file = args.getOrElse(0) { "result.csv" }

val results = readCSV(file)

var totalTimeA: Long = 0
var totalTimeB: Long = 0
var totalMethodCount = 0

results.forEach { (repo, measurements) ->
    val methodToResult = computeMethodResults(measurements)
    var time: Long = 0
    var methodCount = 0

    println("$repo:")

    methodToResult.forEach { (method, result) ->
        print("  ")
        print("avg: ${result.average} ")
        print("median: ${result.median} ")
        println(method)
        time += result.average
        methodCount++
    }

    println("Repository average: ${time.toDouble() / methodCount}")
    println()

    totalTimeA += time
    totalMethodCount += methodCount
}

println("Total average: ${totalTimeA.toDouble() / totalMethodCount}")

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
