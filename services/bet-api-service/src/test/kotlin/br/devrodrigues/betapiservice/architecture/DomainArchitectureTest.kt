package br.devrodrigues.betapiservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class DomainArchitectureTest {

    private val importedClasses = ClassFileImporter()
        .importPackages("br.devrodrigues.betapiservice")

    @Test
    fun `domain should depend only on allowed packages`() {
        val allowedPackages = arrayOf(
            "..betapiservice.domain..",
            "java..",
            "kotlin..",
            "org.slf4j..",
            "org.jetbrains.."
        )

        val rule = classes()
            .that().resideInAnyPackage("..betapiservice.domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(*allowedPackages)

        rule.check(importedClasses)
    }
}
