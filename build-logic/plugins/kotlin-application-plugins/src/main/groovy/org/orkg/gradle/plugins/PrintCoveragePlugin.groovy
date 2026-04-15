package org.orkg.gradle.plugins

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

@CompileStatic
abstract class PrintCoveragePlugin implements Plugin<Project> {
    void apply(Project target) {
        if (!target.plugins.hasPlugin("jacoco-report-aggregation")) {
            println("JaCoCo Report Aggregation Plugin not found. This plugin will not work without it.")
            return
        }
        target.tasks.register("printCoverage", PrintCoverageTask) {
            def aggregateTask = target.tasks.named("testCodeCoverageReport", JacocoReport)
            it.mustRunAfter(aggregateTask)

            def xmlReport = aggregateTask.get().outputs.files.filter { File file -> file.name.endsWith(".xml") }.singleFile
            it.aggregatedCoverageReport.set(xmlReport)
        }
    }
}
