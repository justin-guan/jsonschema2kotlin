package com.jsonschema2kotlin.parser

import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class MergeStrategyTest {
    @Test
    fun `should merge by using the given existing value`() {
        val property = "property"
        val definition = mockk<() -> String>()

        val result = MergeStrategy.Merge.execute(property, definition)

        assertThat(result, Is(property))
        verify(exactly = 0) { definition.invoke() }
    }

    @Test
    fun `should merge by using the fallback value`() {
        val property = "property"

        val result = MergeStrategy.Merge.execute(null) { property }

        assertThat(result, Is(property))
        assertThat(result, not(nullValue()))
    }

    @Test
    fun `should unmerge by using the existing null value when it does not exist`() {
        val definition = mockk<() -> String>()

        val result = MergeStrategy.Unmerge.execute(null, definition)

        assertThat(result, Is(nullValue()))
        verify(exactly = 0) {
            definition.invoke()
        }
    }

    @Test
    fun `should unmerge by using the existing value if it is not equal to the definition`() {
        val property = "property"
        val definition = "definition"

        val result = MergeStrategy.Unmerge.execute(property) { definition }

        assertThat(result, Is(property))
        assertThat(result, not(definition))
    }

    @Test
    fun `should unmerge by using null value if the property and definition are equal`() {
        val property = "property"

        val result = MergeStrategy.Unmerge.execute(property) { property }

        assertThat(result, Is(nullValue()))
    }
}
