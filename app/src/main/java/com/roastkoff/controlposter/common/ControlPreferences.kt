package com.roastkoff.controlposter.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "control_prefs")

class ControlPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")
    }

    val tenantId: Flow<String?> = context.dataStore.data.map { it[KEY_TENANT_ID] }

    suspend fun saveTenantId(tenantId: String?) {
        context.dataStore.edit {
            if (tenantId != null) it[KEY_TENANT_ID] = tenantId
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}