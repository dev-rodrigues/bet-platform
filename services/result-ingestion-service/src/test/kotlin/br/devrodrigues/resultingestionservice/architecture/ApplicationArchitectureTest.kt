package br.devrodrigues.resultingestionservice.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class ApplicationArchitectureTest {

    private val basePackage = "br.devrodrigues.resultingestionservice"

    private val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(basePackage)

    @Test
    fun `application should depend only on domain and technical libraries`() {
        val allowedDependencies = object : DescribedPredicate<JavaClass>("application domain or external libraries") {
            override fun test(javaClass: JavaClass): Boolean {
                val packageName = javaClass.packageName
                val isProjectPackage = packageName == basePackage || packageName.startsWith("$basePackage.")
                val isApplication =
                    packageName == "$basePackage.application" || packageName.startsWith("$basePackage.application.")
                val isDomain = packageName == "$basePackage.domain" || packageName.startsWith("$basePackage.domain.")
                val isAdapter = packageName == "$basePackage.adapter" || packageName.startsWith("$basePackage.adapter.")
                val isInfrastructure =
                    packageName == "$basePackage.infrastructure" || packageName.startsWith("$basePackage.infrastructure.")
                return !isAdapter && !isInfrastructure && (!isProjectPackage || isApplication || isDomain)
            }
        }

        val rule = classes()
            .that().resideInAnyPackage("..resultingestionservice.application..")
            .should().onlyDependOnClassesThat(allowedDependencies)
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
