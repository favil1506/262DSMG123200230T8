package com.example.unidad6ruta3.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {

    @Query(
        "SELECT * FROM airport " +
            "WHERE name LIKE '%' || :query || '%' " +
            "OR iata_code LIKE '%' || :query || '%' " +
            "ORDER BY passengers DESC"
    )
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE UPPER(iata_code) = UPPER(:iataCode)")
    fun getAirportByCode(iataCode: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code != :departureCode ORDER BY passengers DESC")
    fun getDestinations(departureCode: String): Flow<List<Airport>>

    @Query("SELECT * FROM favorite ORDER BY id DESC")
    fun getFavorites(): Flow<List<Favorite>>

    @Query(
        "SELECT * FROM favorite " +
            "WHERE departure_code = :departureCode AND destination_code = :destinationCode " +
            "LIMIT 1"
    )
    suspend fun getFavorite(departureCode: String, destinationCode: String): Favorite?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)
}
