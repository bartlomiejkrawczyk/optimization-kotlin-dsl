# Optimization Kotlin DSL

![](https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl/actions/workflows/build.yml/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.bartlomiejkrawczyk/optimization-kotlin-dsl)](https://search.maven.org/artifact/io.github.bartlomiejkrawczyk/optimization-kotlin-dsl)

> A concise Kotlin DSL wrapper for Google's [OR-Tools](https://developers.google.com/optimization) Linear and Mixed Integer Programming Solver.

## Overview

**Optimization Kotlin DSL** is a expressive Kotlin Domain-Specific Language (DSL) for defining and
solving optimization problems using [Google OR-Tools](https://developers.google.com/optimization).  
It simplifies model creation by providing idiomatic Kotlin syntax while supporting:

- Linear Programming (LP)
- Integer Programming (IP)
- Mixed Integer Programming (MIP)

Inspired by [`io.justdevit:simplex-kotlin-dsl`](https://github.com/temofey1989/simplex-kotlin-dsl), this library extends
its concept to cover a wider range of solver types.

## Example Usage

```kotlin
val (status, config) = optimize {
    solver(SolverType.SCIP_MIXED_INTEGER_PROGRAMMING)

    val x = intVar("x")
    val y = numVar("y")
    val z = boolVar("z")

    // OBJECTIVE
    x * 2 + y * 3 + 4 * z to Goal.MAX

    // CONSTRAINTS
    x + y le 3
    y - 1 le 2

    5 * y eq (x + 3) * 2

    val variables = listOf(x, y, z)

    for (variable in variables) {
        variable le 1.5
    }

    variables.sum() le y
    
    solve()
}

println("OBJECTIVE")
println("Optimal objective value = ${config.objective.value()}")

println("VARIABLES")
config.variables.forEach { variable ->
    println("${variable.name()} = ${variable.solutionValue()}")
}
````

**Output:**

```kotlin
OBJECTIVE
Optimal objective value = 4.3999999999999995
VARIABLES
x = -1.0
y = 0.7999999999999999
z = 1.0
```

### Flow Network

```kotlin
val (status, config) = optimize {
    solver(SolverType.GLOP_LINEAR_PROGRAMMING)

    val totalCost = numVar("totalCost", lowerBound = 0.0)
    val flows = tensorNumVar(
        tensorKeys = listOf(nodes, nodes),
        namePrefix = "flow",
        lowerBound = 0,
    )

    min {
        totalCost
    }

    "total cost constraint" {
        totalCost eq nodes.flatMap { f ->
            nodes.map { t ->
                costTensor[f, t] * flows[f, t]
            }
        }.sum()
    }

    "initial flow constraint" {
        nodes.map { z -> flows["s", z] }.sum() eq fGiven
    }

    for (v in nodesWithout) {
        "Kirchhoff's law constraint - $v" {
            nodes.map { z -> flows[v, z] }.sum() eq
                    nodes.map { u -> flows[u, v] }.sum()
        }
    }

    for (f in nodes) {
        for (t in nodes) {
            "max flow constraint - $f $t" {
                flows[f, t] le capacityMinCostTensor[f, t]
            }
        }
    }
    
    solve()
}
```

## Installation

Add the dependency to your **Maven** or **Gradle** project.

### Maven

```xml

<dependency>
    <groupId>io.github.bartlomiejkrawczyk</groupId>
    <artifactId>optimization-kotlin-dsl</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.bartlomiejkrawczyk:optimization-kotlin-dsl:${version}")
}
```

## Build Configuration

This project is built with **Gradle** and configured for publication to **GitHub Packages**.
To build and test locally:

```bash
./gradlew clean build
```

To publish to GitHub Packages repository:

```bash
./gradlew publish
```

Make sure to set the following environment variables or Gradle properties:

```bash
GITHUB_ACTOR=your_github_username
GITHUB_TOKEN=your_personal_access_token
```

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

## Contributing

Contributions are welcome!

If you'd like to contribute:

1. Fork the repository
2. Create a new branch (`feature/my-feature` or `fix/my-bug`)
3. Commit your changes
4. Open a Pull Request

Issues, suggestions, and improvements are highly appreciated.

## Links

- [Google OR-Tools Documentation](https://developers.google.com/optimization)
- [Google OR-Tools Repository](https://github.com/google/or-tools)
- [Kotlin DSL Reference](https://kotlinlang.org/docs/type-safe-builders.html)
- [Project Repository](https://github.com/bartlomiejkrawczyk/optimization-kotlin-dsl)
