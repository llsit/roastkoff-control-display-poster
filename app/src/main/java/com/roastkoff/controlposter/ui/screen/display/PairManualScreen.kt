package com.roastkoff.controlposter.ui.screen.display

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairManualScreen(
    tenantId: String,
    onDone: () -> Unit,
    viewModel: PairManualViewModel = hiltViewModel(),
    onClickBack: () -> Unit,
    onAddGroup: () -> Unit
) {
    val ui by viewModel.pairManualUi.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadGroups(tenantId) }
    LaunchedEffect(ui.done) { if (ui.done) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("การเชื่อมต่อจอ") },
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
                value = ui.displayName,
                onValueChange = viewModel::setName,
                label = { Text("ชื่อจอ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = ui.location,
                onValueChange = viewModel::setLocation,
                label = { Text("สถานที่ (ไม่บังคับ)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = ui.code,
                onValueChange = viewModel::setCode,
                label = { Text("โค้ด (ไม่บังคับ)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = ui.groups.firstOrNull { it.first == ui.groupId }?.second
                        ?: "เลือกกลุ่ม (หรือเพิ่มกลุ่ม)",
                    onValueChange = {},
                    label = { Text("กลุ่ม") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("เพิ่มกลุ่ม") }, onClick = {
                        onAddGroup(); expanded = false
                    })
                    ui.groups.forEach { (id, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            viewModel.setGroup(id); expanded = false
                        })
                    }
                }
            }

            if (ui.error != null) Text(ui.error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = { viewModel.submit(tenantId) },
                enabled = !ui.loading && ui.displayName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) CircularProgressIndicator(Modifier.size(18.dp))
                else Text("Add")
            }
        }
    }
}