package com.fine.samples.rustforkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.regex.Pattern

private val pattern = Pattern.compile("^[0-9]*$")

internal fun String.ensureInput(): Boolean = isNotEmpty() && pattern.matcher(this).matches()

internal fun execute(
    a: String, b: String,
    scope: CoroutineScope, snackbarHostState: SnackbarHostState,
    action: (ULong, ULong) -> Any
) {
    val msg = try {
        if (!a.ensureInput() || !b.ensureInput()) "Input is not a number"
        else "Result = " + action(a.toULong(), b.toULong()).toString()
    } catch (e: Throwable) {
        e.message ?: "Unknown error"
    }
    scope.launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = "OK",
            duration = SnackbarDuration.Short
        )
    }
}

enum class Actions(val actionName: String) {
    ADD("add"), SUB("sub"), DIV("sub"), EQUAL("equal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun RootContent() {
    MaterialTheme {
        var a by remember { mutableStateOf("") }
        var b by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Rust For KMP in ${Greeting().greet()}") }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = a,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = { a = it })
                OutlinedTextField(
                    value = b,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = { b = it }
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(Actions.entries.toTypedArray()) { action ->
                        Button(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = {
                                execute(a, b, scope, snackbarHostState) { a, b ->
                                    when (action) {
                                        Actions.ADD -> rustAdd(a, b)
                                        Actions.SUB -> rustSub(a, b)
                                        Actions.DIV -> rustDiv(a, b)
                                        Actions.EQUAL -> rustEqual(a, b)
                                    }
                                }
                            }
                        ) {
                            Text(action.actionName)
                        }
                    }
                }
            }
        }
    }
}