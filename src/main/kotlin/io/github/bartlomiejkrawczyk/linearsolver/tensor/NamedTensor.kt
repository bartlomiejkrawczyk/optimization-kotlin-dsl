package io.github.bartlomiejkrawczyk.linearsolver.tensor

/**
 * Represents a multidimensional collection of variables or values.
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
    public val wildcard: K? = null,
) {

    /**
     * Retrieves the value associated with the given keys.
     *
     * Throws [IllegalArgumentException] if any key is invalid.
     * Calls [defaultValueProvider] if the key combination does not exist.
     */
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

    /**
     * Return a sub-tensor following MATLAB-like dimensional reduction rules:
     *  - A specific key (not equal to wildcard) collapses that dimension (dimension removed).
     *  - A wildcard (key == wildcard) keeps that dimension and all keys along it are included.
     *  - If fewer selectors are provided than dimensions, the remaining dimensions are treated as wildcard.
     *
     * The return is:
     *  - NamedTensor<K,V> if there are remaining (kept) dimensions,
     *  - an error if all dimensions are collapsed (use get operator instead).
     */
    public fun subTensor(vararg partialKeys: K): NamedTensor<K, V> {
        return subTensor(partialKeys.toList())
    }

    /**
     * Return a sub-tensor following MATLAB-like dimensional reduction rules:
     *  - A specific key (not equal to wildcard) collapses that dimension (dimension removed).
     *  - A wildcard (key == wildcard) keeps that dimension and all keys along it are included.
     *  - If fewer selectors are provided than dimensions, the remaining dimensions are treated as wildcard.
     *
     * The return is:
     *  - NamedTensor<K,V> if there are remaining (kept) dimensions,
     *  - an error if all dimensions are collapsed (use get operator instead).
     */
    @Suppress("UNCHECKED_CAST")
    public fun subTensor(partialKeys: List<K>): NamedTensor<K, V> {
        fun isWildcardAt(dim: Int): Boolean {
            if (dim >= partialKeys.size) return true
            val selector = partialKeys[dim]
            return selector == wildcard
        }

        val remainingKeysLists: List<List<K>> =
            keys.mapIndexedNotNull { idx, possible ->
                if (isWildcardAt(idx)) possible else null
            }

        val reducedRoot = mutableMapOf<K, Any>()

        fun insertNested(root: MutableMap<K, Any>, path: List<K>, value: Any?) {
            if (path.isEmpty()) {
                return
            }

            var cur: MutableMap<K, Any> = root
            for (i in 0 until path.size - 1) {
                val k = path[i]
                val next = cur[k]
                if (next == null || next !is MutableMap<*, *>) {
                    val newMap = mutableMapOf<K, Any>()
                    cur[k] = newMap
                    cur = newMap
                } else {
                    cur = next as MutableMap<K, Any>
                }
            }

            val lastKey = path.last()
            if (value is Map<*, *>) {
                // Deep-clean nested map
                val cleaned = (value as Map<K, Any?>)
                    .filterValues { it != null }
                    .mapValues { (_, v) -> v!! }
                if (cleaned.isNotEmpty()) cur[lastKey] = cleaned
            } else if (value != null) {
                cur[lastKey] = value
            }
        }

        fun traverse(node: Any?, dim: Int, remainingPath: MutableList<K>) {
            if (dim == keys.size) {
                insertNested(reducedRoot, remainingPath, node)
                return
            }

            val possibleKeys = keys[dim]

            if (isWildcardAt(dim)) {
                if (node !is Map<*, *>) return

                for ((keyAny, v) in node) {
                    val k = keyAny as K
                    if (!possibleKeys.contains(k)) continue
                    if (v == null) continue
                    remainingPath.add(k)
                    traverse(v, dim + 1, remainingPath)
                    remainingPath.removeAt(remainingPath.size - 1)
                }
            } else {
                val sel = partialKeys[dim]
                if (!possibleKeys.contains(sel)) {
                    throw IllegalArgumentException("Key $sel is not allowed in $possibleKeys")
                }
                if (node !is Map<*, *>) return
                val child = node[sel] ?: return
                traverse(child, dim + 1, remainingPath)
            }
        }

        traverse(values, 0, mutableListOf())

        if (remainingKeysLists.isEmpty()) {
            error("value return type not supported - use get operator instead")
        }

        @Suppress("UNCHECKED_CAST")
        fun makeImmutableMap(m: MutableMap<K, Any>): Map<K, Any> {
            val out = mutableMapOf<K, Any>()
            for ((k, v) in m) {
                when (v) {
                    is MutableMap<*, *> -> {
                        val cleaned = makeImmutableMap(v as MutableMap<K, Any>)
                        if (cleaned.isNotEmpty()) out[k] = cleaned
                    }
                    else -> if (v != null) out[k] = v
                }
            }
            return out
        }

        val newValues = makeImmutableMap(reducedRoot)

        return NamedTensor(
            keys = remainingKeysLists,
            values = newValues,
            defaultValueProvider = defaultValueProvider,
            wildcard = wildcard
        )
    }
}

public fun main() {
    val tensor = NamedTensor<String, Int>(
        keys = listOf(listOf("i1", "i2"), listOf("j1", "j2")),
        values = mapOf(
            "i1" to mapOf("j1" to 1),
            "i2" to mapOf("j1" to 3, "j2" to 4)
        ),
        wildcard = "*"
    )


    println(tensor.values)
    println(tensor.subTensor("*").values)
    println(tensor.subTensor("*", "*").values)
    println(tensor.subTensor("i1").values)
    println(tensor.subTensor("i2").values)
    println(tensor.subTensor("*", "j1").values)
    println(tensor.subTensor("*", "j2").values)
}