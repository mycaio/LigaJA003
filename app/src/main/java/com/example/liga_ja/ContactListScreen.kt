package com.example.liga_ja

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    preferencesManager: PreferencesManager,
    onBack: () -> Unit
) {
    var contacts by remember { mutableStateOf(preferencesManager.getContacts()) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Contatos") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contacts) { contact ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(text = "Número: ${contact.phoneNumber}")
                            Text(
                                text = "Código: ${contact.calculationCode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Ação: ${if (contact.actionType == "WHATSAPP") "WhatsApp" else "Ligação"}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (contact.actionType == "WHATSAPP") {
                                Text(
                                    text = "Mensagem: ${contact.whatsappMessage}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { contactToEdit = contact }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                val newList = contacts.filter { it.id != contact.id }
                                preferencesManager.saveContacts(newList)
                                contacts = newList
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            ContactDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, number, code, actionType, whatsappMessage ->
                    val newContact = Contact(
                        name = name,
                        phoneNumber = number,
                        calculationCode = code,
                        actionType = actionType,
                        whatsappMessage = whatsappMessage
                    )
                    val newList = contacts + newContact
                    preferencesManager.saveContacts(newList)
                    contacts = newList
                    showAddDialog = false
                }
            )
        }

        if (contactToEdit != null) {
            ContactDialog(
                contact = contactToEdit,
                onDismiss = { contactToEdit = null },
                onSave = { name, number, code, actionType, whatsappMessage ->
                    val newList = contacts.map {
                        if (it.id == contactToEdit?.id) {
                            it.copy(
                                name = name,
                                phoneNumber = number,
                                calculationCode = code,
                                actionType = actionType,
                                whatsappMessage = whatsappMessage
                            )
                        } else it
                    }
                    preferencesManager.saveContacts(newList)
                    contacts = newList
                    contactToEdit = null
                }
            )
        }
    }
}

@Composable
fun ContactDialog(
    contact: Contact? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var number by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var code by remember { mutableStateOf(contact?.calculationCode ?: "") }
    var actionType by remember { mutableStateOf(contact?.actionType ?: "CALL") }
    var whatsappMessage by remember {
        mutableStateOf(
            contact?.whatsappMessage ?: "Preciso de ajuda. Entre em contato comigo imediatamente."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Novo Contato" else "Editar Contato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") }
                )

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Telefone") }
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código (ex: 123+)") }
                )

                Text("Tipo de ação")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = actionType == "CALL",
                        onClick = { actionType = "CALL" },
                        label = { Text("Ligação") }
                    )
                    FilterChip(
                        selected = actionType == "WHATSAPP",
                        onClick = { actionType = "WHATSAPP" },
                        label = { Text("WhatsApp") }
                    )
                }

                if (actionType == "WHATSAPP") {
                    OutlinedTextField(
                        value = whatsappMessage,
                        onValueChange = { whatsappMessage = it },
                        label = { Text("Mensagem do WhatsApp") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, number, code, actionType, whatsappMessage)
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}