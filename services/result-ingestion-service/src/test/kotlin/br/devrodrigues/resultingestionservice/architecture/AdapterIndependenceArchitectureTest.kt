package br.devrodrigues.resultingestionservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class AdapterIndependenceArchitectureTest {

    private val basePackage = "br.devrodrigues.resultingestionservice"

    private val importedClasses = ClassFileImporter()
        .importPackages(basePackage)

    @Test
    fun `domain and application should not depend on adapters`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..resultingestionservice.domain..", "..resultingestionservice.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..resultingestionservice.adapter..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
