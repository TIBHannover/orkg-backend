package org.orkg.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class PrintCoverageTask extends DefaultTask {
    @Input
    abstract final Property<File> aggregatedCoverageReport = project.objects.property(File)

    PrintCoverageTask() {
        group = "coverage"
    }

    @TaskAction
    def printCoverage() {
        def xml = new XmlSlurper()
        xml.setFeature('http://apache.org/xml/features/disallow-doctype-decl', false)
        xml.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false)
        def report = xml.parse(aggregatedCoverageReport.get())
        double missed = report.counter.find { it.'@type' == "INSTRUCTION" }.@missed.toDouble()
        double covered = report.counter.find { it.'@type' == "INSTRUCTION" }.@covered.toDouble()
        def coverage = (100 / (missed + covered) * covered).round(2)
        println 'Coverage: ' + coverage + '%'
    }
}
