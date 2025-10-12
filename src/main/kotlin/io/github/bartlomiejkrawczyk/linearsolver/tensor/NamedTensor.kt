package io.github.bartlomiejkrawczyk.linearsolver.tensor

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
