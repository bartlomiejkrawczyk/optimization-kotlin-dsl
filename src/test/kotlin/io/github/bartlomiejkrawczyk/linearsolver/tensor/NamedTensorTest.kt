package io.github.bartlomiejkrawczyk.linearsolver.tensor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NamedTensorTest {

    private fun buildTensor(): NamedTensor<String, Int> =
        NamedTensor(
            keys = listOf(listOf("i1", "i2"), listOf("j1", "j2")),
            values = mapOf(
                "i1" to mapOf("j1" to 1),
                "i2" to mapOf("j1" to 3, "j2" to 4)
            ),
            wildcard = "*"
        )

    @Test
    fun `subTensor with wildcard should preserve full structure`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("*")

        val expected = mapOf(
            "i1" to mapOf("j1" to 1),
            "i2" to mapOf("j1" to 3, "j2" to 4)
        )

        assertEquals(expected, sub.values)
    }

    @Test
    fun `subTensor with wildcard for all dims should equal original`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("*", "*")

        val expected = mapOf(
            "i1" to mapOf("j1" to 1),
            "i2" to mapOf("j1" to 3, "j2" to 4)
        )

        assertEquals(expected, sub.values)
    }

    @Test
    fun `subTensor selecting first index should collapse first dimension`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("i1")

        val expected = mapOf("j1" to 1)

        assertEquals(expected, sub.values)
    }

    @Test
    fun `subTensor selecting second index should collapse first dimension`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("i2")

        val expected = mapOf("j1" to 3, "j2" to 4)

        assertEquals(expected, sub.values)
    }

    @Test
    fun `subTensor wildcard first dimension and j1 second should reduce properly`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("*", "j1")

        val expected = mapOf("i1" to 1, "i2" to 3)

        assertEquals(expected, sub.values)
    }

    @Test
    fun `subTensor wildcard first dimension and j2 second should reduce properly`() {
        val tensor = buildTensor()
        val sub = tensor.subTensor("*", "j2")

        val expected = mapOf("i2" to 4)

        assertEquals(expected, sub.values)
    }

    @Test
    fun `get operator should return scalar value correctly`() {
        val tensor = buildTensor()
        val value = tensor["i2", "j1"]
        assertEquals(3, value)
    }
}
