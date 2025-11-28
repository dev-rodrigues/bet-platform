package br.devrodrigues.betapiservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class AdapterIndependenceArchitectureTest {

    private val basePackage = "br.devrodrigues.betapiservice"

    private val importedClasses = ClassFileImporter()
        .importPackages(basePackage)

    @Test
    fun `domain and application should not depend on adapters`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..betapiservice.domain..", "..betapiservice.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..betapiservice.adapter..")

        rule.check(importedClasses)
    }
}
