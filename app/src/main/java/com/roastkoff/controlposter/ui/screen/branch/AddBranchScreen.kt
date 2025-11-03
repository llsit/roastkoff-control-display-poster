package com.roastkoff.controlposter.ui.screen.branch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBranchScreen(
    tenantId: String,
    onSaved: () -> Unit
) {
    val viewModel: AddBranchViewModel = hiltViewModel()
    val addBranchUi by viewModel.addBranchUi.collectAsState()

    LaunchedEffect(addBranchUi.done) { if (addBranchUi.done) onSaved() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Branch / Group") }) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = addBranchUi.name,
                onValueChange = viewModel::setName,
                label = { Text("Branch name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = addBranchUi.address,
                onValueChange = viewModel::setAddress,
                label = { Text("Address (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (addBranchUi.error != null) Text(
                addBranchUi.error!!,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                onClick = { viewModel.save(tenantId) },
                enabled = !addBranchUi.loading && addBranchUi.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (addBranchUi.loading) CircularProgressIndicator(Modifier.size(18.dp))
                else Text("Save")
            }
        }
    }
}