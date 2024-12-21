package org.burufi.monitoring.delivery.integration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test-h2")
class InMemoryH2IntegrationTest : DeliveryIntegrationTest()
