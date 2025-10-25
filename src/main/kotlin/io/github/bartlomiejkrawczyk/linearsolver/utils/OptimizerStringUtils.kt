package io.github.bartlomiejkrawczyk.linearsolver.utils

public object OptimizerStringUtils {

    @JvmStatic
    public fun formatDouble(v: Double): String =
        if (v % 1.0 == 0.0)
            v.toLong().toString()
        else
            v.toString()
}
