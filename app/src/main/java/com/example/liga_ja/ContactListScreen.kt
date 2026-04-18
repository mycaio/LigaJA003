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
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = contact.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Número: ${contact.phoneNumber}")
                            Text(text = "Código: ${contact.calculationCode}", style = MaterialTheme.typography.bodySmall)
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

        // Diálogo para Adicionar
        if (showAddDialog) {
            ContactDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, number, code ->
                    val newContact = Contact(name = name, phoneNumber = number, calculationCode = code)
                    val newList = contacts + newContact
                    preferencesManager.saveContacts(newList)
                    contacts = newList
                    showAddDialog = false
                }
            )
        }

        // Diálogo para Editar
        if (contactToEdit != null) {
            ContactDialog(
                contact = contactToEdit,
                onDismiss = { contactToEdit = null },
                onSave = { name, number, code ->
                    val newList = contacts.map {
                        if (it.id == contactToEdit?.id) {
                            it.copy(name = name, phoneNumber = number, calculationCode = code)
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
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var number by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var code by remember { mutableStateOf(contact?.calculationCode ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Novo Contato" else "Editar Contato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Telefone") })
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código (ex: 123+)") })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, number, code) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
