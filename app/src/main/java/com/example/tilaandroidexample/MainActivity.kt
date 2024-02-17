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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tilaandroidexample.MainActivity.Ids
import com.example.tilaandroidexample.ui.theme.TilaAndroidExampleTheme
import com.github.pmpuro.tila.api.DataId
import com.github.pmpuro.tila.api.DataMap
import com.github.pmpuro.tila.api.DerivativeSubscription
import com.github.pmpuro.tila.api.EventHandlerSubscription
import com.github.pmpuro.tila.api.EventId
import com.github.pmpuro.tila.api.Machine
import com.github.pmpuro.tila.api.StateDataList
import com.github.pmpuro.tila.api.accessData
import com.github.pmpuro.tila.api.accessDataOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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

    private fun createInitialAppData() = mapOf(Data.counter to 100)
    private val machine = createMachine(data = createInitialAppData()) {
        registerEventHandlers()
        registerDerivatives()
    }

    private fun createMachine(
        data: DataMap = mapOf(),
        initialStateData: StateDataList = listOf(),
        coroutineScope: CoroutineScope = MainScope(),
        block: Machine.() -> Unit
    ) = Machine(data, initialStateData, coroutineScope).apply(block).also { it.derive() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupTicker(lifecycle.coroutineScope)

        setContent {
            TilaAndroidExampleTheme {
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

    private fun setupTicker(coroutineScope: LifecycleCoroutineScope) =
        coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    delay(6.seconds)
                    val time = System.currentTimeMillis().toString()
                    machine.sendEvent(Events.ticker, mapOf(Argument.current to time))
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
        appData.deriveAppDataToStateDirectly<Int>(Ids.Data.counter, Ids.State.counterValue)
    }
}

private inline fun <reified T : Any> DataMap.deriveAppDataToStateDirectly(
    source: DataId,
    destination: DataId
): DataMap {
    val value = accessData<T>(source)
    return mapOf(destination to value)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
