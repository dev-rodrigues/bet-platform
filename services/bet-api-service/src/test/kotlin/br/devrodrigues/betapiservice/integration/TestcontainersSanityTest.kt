package br.devrodrigues.betapiservice.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class TestcontainersSanityTest {

    companion object {
        @Container
        @JvmStatic
        val container: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
    }

    @Test
    fun `should start a simple container`() {
        Assertions.assertTrue(container.isRunning)
    }
}