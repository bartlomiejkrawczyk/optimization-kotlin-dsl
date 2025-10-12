package io.github.bartlomiejkrawczyk.linearsolver.tensor

/**
 * Represents a multi-dimensional collection of variables or values.
 *
 * Provides type-safe access via multiple keys. Supports default value provider
 * when a key combination does not exist.
 *
 * Example:
 * ```kotlin
 * val tensor = NamedTensor(
 *     keys = listOf(listOf("i1", "i2"), listOf("j1", "j2")),
 *     values = mapOf(
 *         "i1" to mapOf("j1" to x1, "j2" to x2),
 *         "i2" to mapOf("j1" to x3, "j2" to x4)
 *     )
 * )
 *
 * val x = tensor["i1", "j2"] // -> x2
 * ```
 *
 * @param keys List of allowed keys per dimension.
 * @param values Nested map storing actual variable instances.
 * @param defaultValueProvider Called when a key combination is not found.
 */
public open class NamedTensor<K, V>(
    public val keys: List<List<K>> = listOf(listOf()),
    public val values: Map<K, Any> = mapOf(),
    public val defaultValueProvider: (keys: List<K>) -> V = { throw NotImplementedError() },
) {

    @Suppress("UNCHECKED_CAST")
    public operator fun get(vararg elementKeys: K): V {
        var elements: Map<K, Any> = values
        var finalValue: V? = null

        if (keys.size != elementKeys.size) {
            throw IllegalArgumentException("The number of keys must be the same as the number of dimensions.")
        }

        for ((key, possibleKeys) in elementKeys.zip(keys)) {
            if (!possibleKeys.contains(key)) {
                throw IllegalArgumentException("Key $key is not allowed in $possibleKeys")
            }
            val value = elements[key]
            if (value is Map<*, *>) {
                elements = value as Map<K, Any>
            } else if (value == null) {
                break
            } else {
                finalValue = value as? V
            }
        }

        if (finalValue == null) {
            return defaultValueProvider(elementKeys.toList())
        }

        return finalValue
    }
}
