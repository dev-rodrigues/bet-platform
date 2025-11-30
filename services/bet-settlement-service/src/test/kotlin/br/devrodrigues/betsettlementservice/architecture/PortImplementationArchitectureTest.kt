package br.devrodrigues.betsettlementservice.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class PortImplementationArchitectureTest {

    private val basePackage = "br.devrodrigues.betsettlementservice"
    private val adapterPackages = arrayOf(
        "..betsettlementservice.adapter.inbound..",
        "..betsettlementservice.adapter.outbound..",
        "..betsettlementservice.adapter.in..",
        "..betsettlementservice.adapter.out.."
    )

    private val importedClasses = ClassFileImporter()
        .importPackages(basePackage)

    @Test
    fun `interfaces ending with Port should live in port packages`() {
        val rule = classes()
            .that().areInterfaces()
            .and().haveSimpleNameEndingWith("Port")
            .should().resideInAnyPackage(
                "..betsettlementservice.domain.port..",
                "..betsettlementservice.application.port.."
            )
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `implementations of ports must live in adapters`() {
        val rule = classes()
            .that().areNotInterfaces()
            .and().implement(simpleNameEndingWith("Port"))
            .should().resideInAnyPackage(*adapterPackages)
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `implementations of port interfaces should end with Adapter`() {
        val rule = classes()
            .that().implement(resideInAnyPackage("..betsettlementservice..port.."))
            .and().resideInAnyPackage(*adapterPackages)
            .should().haveSimpleNameEndingWith("Adapter")
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }

    @Test
    fun `adapter style implementations stay in adapter packages`() {
        val adapterStyleClasses =
            object : DescribedPredicate<JavaClass>("classes ending with Adapter, Repository or Client") {
                override fun test(javaClass: JavaClass): Boolean =
                    !javaClass.isInterface && (
                            javaClass.simpleName.endsWith("Adapter") ||
                                    javaClass.simpleName.endsWith("Repository") ||
                                    javaClass.simpleName.endsWith("Client")
                            )
            }

        val rule = classes()
            .that(adapterStyleClasses)
            .should().resideInAnyPackage(*adapterPackages)
            .allowEmptyShould(true)

        rule.check(importedClasses)
    }
}
