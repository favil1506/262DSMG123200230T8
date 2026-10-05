package com.example.unidad6ruta3.ui

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unidad6ruta3.FlightSearchApplication
import com.example.unidad6ruta3.data.Airport
import com.example.unidad6ruta3.data.Favorite
import com.example.unidad6ruta3.searchDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FlightSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val flightDao = (application as FlightSearchApplication).database.flightDao()
    private val dataStore = application.searchDataStore

    val searchQuery: StateFlow<String> = dataStore.data
        .catch { throwable ->
            if (throwable is Exception) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences -> preferences[SEARCH_QUERY] ?: "" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val selectedAirport: StateFlow<Airport?> = searchQuery
        .map { it.trim() }
        .flatMapLatest { query -> airportForQuery(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val suggestions: StateFlow<List<Airport>> = searchQuery
        .map { it.trim() }
        .flatMapLatest { query ->
            if (query.isEmpty()) flowOf(emptyList()) else flightDao.searchAirports(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val flights: StateFlow<List<Airport>> = selectedAirport
        .flatMapLatest { airport ->
            if (airport == null) flowOf(emptyList())
            else flightDao.getDestinations(airport.iataCode)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<Favorite>> = flightDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteKeys: StateFlow<Set<String>> = favorites
        .map { list -> list.map { "${it.departureCode}-${it.destinationCode}" }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            dataStore.edit { preferences -> preferences[SEARCH_QUERY] = query }
        }
    }

    fun selectAirport(airport: Airport) {
        updateSearchQuery(airport.iataCode)
    }

    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            val existing = flightDao.getFavorite(departureCode, destinationCode)
            if (existing == null) {
                flightDao.insertFavorite(
                    Favorite(
                        departureCode = departureCode,
                        destinationCode = destinationCode
                    )
                )
            } else {
                flightDao.deleteFavorite(departureCode, destinationCode)
            }
        }
    }

    fun deleteFavorite(favorite: Favorite) {
        viewModelScope.launch {
            flightDao.deleteFavorite(favorite.departureCode, favorite.destinationCode)
        }
    }

    private fun airportForQuery(query: String): Flow<Airport?> {
        if (query.length != 3) return flowOf(null)
        return flightDao.getAirportByCode(query).map { it.firstOrNull() }
    }

    companion object {
        private val SEARCH_QUERY = stringPreferencesKey("search_query")
    }
}
