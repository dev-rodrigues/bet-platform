package br.devrodrigues.betsettlementservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class AdapterIndependenceArchitectureTest {

    private val basePackage = "br.devrodrigues.betsettlementservice"

    private val importedClasses = ClassFileImporter()
        .importPackages(basePackage)

    @Test
    fun `domain and application should not depend on adapters`() {
        val rule = noClasses()
            .that().resideInAnyPackage("..betsettlementservice.domain..", "..betsettlementservice.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..betsettlementservice.adapter..")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
