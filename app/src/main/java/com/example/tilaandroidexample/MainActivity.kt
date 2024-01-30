package com.example.tilaandroidexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tilaandroidexample.MainActivity.*
import com.example.tilaandroidexample.ui.theme.TilaAndroidExampleTheme
import io.tila.api.DataId
import io.tila.api.DerivativeSubscription
import io.tila.api.EventHandlerSubscription
import io.tila.api.EventId
import io.tila.api.Machine
import io.tila.api.accessData
import io.tila.api.accessDataOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    companion object Ids {
        object Data {
            val time = DataId("current time")
            val counter = DataId("counter")
        }

        object State {
            val timeString = DataId("time string")
            val counterValue = DataId("counter value")
        }

        object Argument {
            val current = DataId("current")
        }

        object Events {
            val ticker = EventId("ticker")
            val click = EventId("click")
        }
    }

    private val machine = Machine(data = mapOf(Data.counter to 100))
        .apply {
            registerEventHandlers()
            registerDerivatives()
            derive()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTicker(lifecycle.coroutineScope)
        setContent {
            TilaAndroidExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        val time by remember { machine.injectState<String>(State.timeString) }
                        val counter by remember { machine.injectState<Int>(State.counterValue) }
                        Greeting("Android")
                        Text(text = time)
                        Text(text = "counter = $counter")
                        Button(onClick = machine.createEvent(Events.click)) {
                            Text(text = "Press here")
                        }
                    }
                }
            }
        }
    }

    private fun setupTicker(coroutineScope: LifecycleCoroutineScope) {
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    delay(6.seconds)
                    val time = System.currentTimeMillis().toString()
                    machine.createEvent(Events.ticker, mapOf(Argument.current to time))()
                }
            }
        }
    }
}

private fun EventHandlerSubscription.registerEventHandlers() {
    registerEventHandler(Ids.Events.ticker) { _, args ->
        val now = args.accessDataOrNull<String>(Ids.Argument.current) ?: ""
        mapOf(Ids.Data.time to now)
    }

    registerEventHandler(Ids.Events.click) { appData, _ ->
        val counter = appData.accessData<Int>(Ids.Data.counter)
        val newValue = counter + 1
        mapOf(Ids.Data.counter to newValue)
    }
}

private fun DerivativeSubscription.registerDerivatives() {
    registerDerivative { appData ->
        val now = appData.accessDataOrNull<String>(Ids.Data.time) ?: "not available yet"
        mapOf(Ids.State.timeString to "Time now is $now")
    }

    registerDerivative { appData ->
        val value = appData.accessData<Int>(Ids.Data.counter)
        mapOf(Ids.State.counterValue to value)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TilaAndroidExampleTheme {
        Greeting("Android")
    }
}