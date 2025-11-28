package br.devrodrigues.betapiservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController

class ControllerLocationArchitectureTest {

    private val importedClasses = ClassFileImporter()
        .importPackages("br.devrodrigues.betapiservice")

    @Test
    fun `controllers should live only in adapter inbound web package`() {
        val rule = classes()
            .that().areAnnotatedWith(RestController::class.java)
            .or().areAnnotatedWith(Controller::class.java)
            .or().haveSimpleNameEndingWith("Controller")
            .should().resideInAnyPackage("..betapiservice.adapter.inbound.web..")

        rule.check(importedClasses)
    }
}
