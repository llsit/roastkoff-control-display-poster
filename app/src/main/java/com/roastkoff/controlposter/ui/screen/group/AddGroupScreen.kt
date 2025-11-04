package com.roastkoff.controlposter.ui.screen.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupScreen(
    tenantId: String,
    onSaved: () -> Unit,
    viewModel: AddGroupViewModel = hiltViewModel(),
    onClickBack: () -> Unit,
) {
    val ui by viewModel.addGroupUi.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("เพิ่มกลุ่มใหม่") },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "",
                        modifier = Modifier.clickable(onClick = onClickBack)
                    )
                })
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ui.name,
                onValueChange = viewModel::setName,
                label = { Text("ชื่อกลุ่ม") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.save(tenantId) },
                enabled = ui.name.isNotBlank() && !ui.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) CircularProgressIndicator(Modifier.size(18.dp))
                else Text("Add")
            }

            if (ui.error != null)
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}