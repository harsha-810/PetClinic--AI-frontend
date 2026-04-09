package com.example.petclinicapp.network

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class AiResponseMappingTest {

    private val gson = Gson()

    @Test
    fun `test AiCheckResponse handles camelCase from backend`() {
        val json = """
            {
                "condition": "Fever",
                "recommendation": "Rest",
                "severity": "Medium",
                "priorityLevel": 1
            }
        """.trimIndent()

        val response = gson.fromJson(json, AiCheckResponse::class.java)

        assertNotNull(response)
        assertEquals("Fever", response.condition)
        assertEquals("Rest", response.recommendation)
        assertEquals("Medium", response.severity)
        assertEquals(1, response.priorityLevel)
    }

    @Test
    fun `test AiCheckResponse handles null values`() {
        val json = """
            {
                "condition": null,
                "recommendation": null,
                "severity": null,
                "priorityLevel": null
            }
        """.trimIndent()

        val response = gson.fromJson(json, AiCheckResponse::class.java)

        assertNotNull(response)
        assertNull(response.condition)
        assertNull(response.severity)
        assertNull(response.priorityLevel)
    }

    @Test
    fun `test AiCheckResponse handles empty JSON`() {
        val json = "{}"
        val response = gson.fromJson(json, AiCheckResponse::class.java)

        assertNotNull(response)
        assertNull(response.condition)
        // Default value in Kotlin data class might not be used by GSON if not using specialized adapter,
        // but our UI change handles nulls safely now.
    }
}
