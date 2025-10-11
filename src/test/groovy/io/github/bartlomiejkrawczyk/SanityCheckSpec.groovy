package io.github.bartlomiejkrawczyk

import spock.lang.Specification

class SanityCheckSpec extends Specification {

    def "Should perform tests correctly"() {
        given:
        var first = 1
        var second = 1
        var expected = 2

        expect:
        first + second == expected
    }
}
