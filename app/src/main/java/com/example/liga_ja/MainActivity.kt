package com.example.liga_ja

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.liga_ja.ui.theme.Liga_JATheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permissão de chamada negada.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)
        requestCallPermissionIfNeeded()

        setContent {
            Liga_JATheme {
                var currentScreen by remember { mutableStateOf("calculator") }

                Crossfade(targetState = currentScreen, label = "navigation") { screen ->
                    when (screen) {
                        "calculator" -> {
                            CalculatorScreen(
                                preferencesManager = preferencesManager,
                                onEmergencyCall = { number -> makeEmergencyCall(number) },
                                onNavigateToContacts = { currentScreen = "contacts" }
                            )
                        }
                        "contacts" -> {
                            ContactListScreen(
                                preferencesManager = preferencesManager,
                                onBack = { currentScreen = "calculator" }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestCallPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun makeEmergencyCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Permissão de chamada não concedida.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    preferencesManager: PreferencesManager,
    onEmergencyCall: (String) -> Unit,
    onNavigateToContacts: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var showHiddenSettings by remember { mutableStateOf(false) }

    var emergencyNumber by remember { mutableStateOf(preferencesManager.getEmergencyNumber()) }
    var callCode by remember { mutableStateOf(preferencesManager.getCallCode()) }
    var settingsCode by remember { mutableStateOf(preferencesManager.getSettingsCode()) }
    val crudCode by remember { mutableStateOf(preferencesManager.getCrudCode()) }

    val buttons = listOf(
        listOf("C", "( )", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("(-)", "0", ",", "=")
    )

    if (showHiddenSettings) {
        HiddenSettingsDialog(
            emergencyNumber = emergencyNumber,
            callCode = callCode,
            settingsCode = settingsCode,
            onEmergencyNumberChange = { emergencyNumber = it },
            onCallCodeChange = { callCode = it },
            onSettingsCodeChange = { settingsCode = it },
            onDismiss = { showHiddenSettings = false },
            onSave = {
                preferencesManager.saveEmergencyNumber(emergencyNumber)
                preferencesManager.saveCallCode(callCode)
                preferencesManager.saveSettingsCode(settingsCode)
                showHiddenSettings = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = display,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { label ->
                    CalculatorButton(
                        label = label,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onClick = {

                            when (label) {

                                "C" -> {
                                    expression = ""
                                    display = "0"
                                }

                                "=" -> {
                                    val contacts = preferencesManager.getContacts()
                                    val matchedContact = contacts.find { it.calculationCode == expression }

                                    when {
                                        expression == callCode -> {
                                            display = "Ligando..."
                                            expression = ""
                                            onEmergencyCall(emergencyNumber)
                                        }

                                        expression == settingsCode -> {
                                            display = "Ajustes"
                                            expression = ""
                                            showHiddenSettings = true
                                        }

                                        expression == crudCode -> {
                                            display = "Contatos"
                                            expression = ""
                                            onNavigateToContacts()
                                        }

                                        matchedContact != null -> {
                                            display = "Ligando p/ ${matchedContact.name}..."
                                            expression = ""
                                            onEmergencyCall(matchedContact.phoneNumber)
                                        }

                                        else -> {
                                            val result = calculateExpression(expression)
                                            display = result
                                            expression = if (result == "Erro") "" else result
                                        }
                                    }
                                }

                                "(-)" -> {
                                    expression += "(-"
                                    display = formatDisplay(expression)
                                }

                                "," -> {
                                    if (!expression.contains(",")) {
                                        expression += ","
                                        display = formatDisplay(expression)
                                    }
                                }

                                "%" -> {
                                    expression += "%"
                                    display = formatDisplay(expression)
                                }

                                "×" -> {
                                    expression += "*"
                                    display = formatDisplay(expression)
                                }

                                "÷" -> {
                                    expression += "/"
                                    display = formatDisplay(expression)
                                }

                                "( )" -> {
                                    val openCount = expression.count { it == '(' }
                                    val closeCount = expression.count { it == ')' }

                                    val lastChar = expression.lastOrNull()

                                    val shouldOpen = expression.isEmpty() ||
                                            lastChar == '(' ||
                                            lastChar in listOf('+', '-', '*', '/')

                                    val newChar = if (shouldOpen || openCount == closeCount) {
                                        "("
                                    } else {
                                        ")"
                                    }

                                    expression += newChar
                                    display = expression
                                }

                                else -> {
                                    if (display == "0" || display == "Erro") {
                                        expression = label
                                    } else {
                                        expression += label
                                    }
                                    display = formatDisplay(expression)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HiddenSettingsDialog(
    emergencyNumber: String,
    callCode: String,
    settingsCode: String,
    onEmergencyNumberChange: (String) -> Unit,
    onCallCodeChange: (String) -> Unit,
    onSettingsCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes ocultos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = emergencyNumber,
                    onValueChange = onEmergencyNumberChange,
                    label = { Text("Número de emergência") }
                )
                OutlinedTextField(
                    value = callCode,
                    onValueChange = onCallCodeChange,
                    label = { Text("Código de chamada") }
                )
                OutlinedTextField(
                    value = settingsCode,
                    onValueChange = onSettingsCodeChange,
                    label = { Text("Código dos ajustes") }
                )
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = label in listOf("+", "-", "×", "÷", "=")
    val buttonColor = if (isOperator) Color(0xFF2563EB) else Color(0xFF1E293B)

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        )
    ) {
        Text(text = label, style = MaterialTheme.typography.headlineMedium)
    }
}

fun formatDisplay(expr: String): String {
    return expr
        .replace("*", "×")
        .replace("/", "÷")
}

fun calculateExpression(expression: String): String {
    return try {
        var normalized = expression
            .replace(",", ".")
            .replace("%", "/100")

        val open = normalized.count { it == '(' }
        val close = normalized.count { it == ')' }
        repeat(open - close) {
            normalized += ")"
        }

        val result = eval(normalized)
        formatResult(result)
    } catch (_: Exception) {
        "Erro"
    }
}

fun formatResult(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}

fun eval(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Erro")
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            val startPos = pos
            if (eat('('.code)) {
                val x = parseExpression()
                eat(')'.code)
                return x
            }

            while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
            return expr.substring(startPos, pos).toDouble()
        }
    }.parse()
}
