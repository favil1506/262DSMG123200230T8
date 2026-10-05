package com.example.unidad6ruta3

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.unidad6ruta3.data.FlightDatabase

val Context.searchDataStore: DataStore<Preferences> by preferencesDataStore(name = "flight_search_prefs")

class FlightSearchApplication : Application() {

    val database: FlightDatabase by lazy { FlightDatabase.getDatabase(this) }
}
