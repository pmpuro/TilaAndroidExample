package com.example.tilaandroidexample

import androidx.compose.ui.semantics.SemanticsProperties.TestTag
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.doesNotContain
import strikt.assertions.isNotEmpty
import kotlin.time.Duration.Companion.seconds

class AppTest {

    @Test
    fun `should show the greeting`(): Unit = with(composeTestRule) {
        onNodeWithText("Hello Android!")
            .assertExists()
    }

    @Test
    fun `should show the counter value 100 after start`(): Unit = with(composeTestRule) {
        onNodeWithText("counter = 100")
            .assertExists()
    }

    @Test
    fun `should increase the counter`(): Unit = with(composeTestRule) {
        onNodeWithText("Press here")
            .performClick()
        onNodeWithText("counter = 101")
            .assertExists()
    }

    @Test
    fun `should increase the counter multiple times`(): Unit = with(composeTestRule) {
        onNodeWithText("Press here")
            .let { interaction ->
                repeat(10) {
                    interaction.performClick()
                }
            }
        onNodeWithText("counter = 110")
            .assertExists()
    }

    @Test
    fun `should display unknown time initially`(): Unit = with(composeTestRule) {
        onNodeWithText("Time now is not available yet")
            .assertExists()
    }

    @Test
    fun `should display changing time`(): Unit = with(composeTestRule) {
        runBlocking {
            val result = mutableListOf<String>()

            delay(1.seconds)

            repeat(3) {
                delay(6.seconds)
                onNodeWithText("Time now is", substring = true)
                    .fetchSemanticsNode()
                    .config
                    .getOrNull(Text)
                    ?.map { it.toString() }
                    ?.first()
                    ?.also {
                        result.add(it)
                    }
            }

            expectThat(result).isNotEmpty()
            expectThat(result).doesNotContain("")
            expectThat(result.distinct()).isNotEmpty()
        }
    }

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setup(): Unit {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close() {
        scenario?.close()
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val appContext get() = InstrumentationRegistry.getInstrumentation().targetContext
}
