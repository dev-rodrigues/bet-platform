package br.devrodrigues.betsettlementservice.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class DomainArchitectureTest {

    private val importedClasses = ClassFileImporter()
        .importPackages("br.devrodrigues.betsettlementservice")

    @Test
    fun `domain should depend only on allowed packages`() {
        val allowedPackages = arrayOf(
            "..betsettlementservice.domain..",
            "java..",
            "kotlin..",
            "org.slf4j..",
            "org.jetbrains.."
        )

        val rule = classes()
            .that().resideInAnyPackage("..betsettlementservice.domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(*allowedPackages)
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
