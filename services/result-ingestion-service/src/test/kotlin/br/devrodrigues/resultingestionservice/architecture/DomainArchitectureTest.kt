package br.devrodrigues.resultingestionservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class DomainArchitectureTest {

    private val importedClasses = ClassFileImporter()
        .importPackages("br.devrodrigues.resultingestionservice")

    @Test
    fun `domain should depend only on allowed packages`() {
        val allowedPackages = arrayOf(
            "..resultingestionservice.domain..",
            "java..",
            "kotlin..",
            "org.slf4j..",
            "org.jetbrains.."
        )

        val rule = classes()
            .that().resideInAnyPackage("..resultingestionservice.domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(*allowedPackages)
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
