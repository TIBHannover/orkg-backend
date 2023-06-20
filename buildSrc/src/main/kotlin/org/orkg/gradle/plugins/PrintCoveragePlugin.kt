package org.orkg.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

abstract class PrintCoveragePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin("jacoco-report-aggregation")) {
            println("JaCoCo Report Aggregation Plugin not found. This plugin will not work without it.")
            return
        }
        target.tasks.register("printCoverage", PrintCoverageTask::class.java) {
            val aggregateTask = target.tasks.named("testCodeCoverageReport", JacocoReport::class.java)
            mustRunAfter(aggregateTask)

            val xmlReport = aggregateTask.get().outputs.files.filter { it.name.endsWith(".xml") }.singleFile
            aggregatedCoverageReport.set(xmlReport)
        }
    }
}
