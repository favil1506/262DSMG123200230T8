package com.example.unidad6ruta3.ui

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.unidad6ruta3.R
import com.example.unidad6ruta3.data.Airport
import com.example.unidad6ruta3.data.Favorite
import java.text.NumberFormat

@Composable
fun FlightSearchScreen(
    viewModel: FlightSearchViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val flights by viewModel.flights.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favoriteKeys by viewModel.favoriteKeys.collectAsState()

    val focusManager = LocalFocusManager.current
    val showSuggestions = selectedAirport == null && suggestions.isNotEmpty()

    Column(modifier = modifier.fillMaxSize()) {
        SearchTextField(
            query = searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onSubmit = { focusManager.clearFocus() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val departure = selectedAirport
            if (departure == null) {
                FavoritesList(
                    favorites = favorites,
                    onDeleteFavorite = viewModel::deleteFavorite,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FlightResultsList(
                    departure = departure,
                    flights = flights,
                    favoriteKeys = favoriteKeys,
                    onToggleFavorite = viewModel::toggleFavorite,
                    modifier = Modifier.fillMaxSize()
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showSuggestions,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                SuggestionsList(
                    suggestions = suggestions,
                    onSelect = { airport ->
                        viewModel.selectAirport(airport)
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(R.string.search)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() })
    )
}

@Composable
private fun SuggestionsList(
    suggestions: List<Airport>,
    onSelect: (Airport) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
            itemsIndexed(
                items = suggestions,
                key = { _, airport -> airport.id }
            ) { index, airport ->
                SuggestionRow(airport = airport, onClick = { onSelect(airport) })
                if (index != suggestions.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    airport: Airport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = airport.iataCode,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = airport.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatNumber(airport.passengers),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FlightResultsList(
    departure: Airport,
    flights: List<Airport>,
    favoriteKeys: Set<String>,
    onToggleFavorite: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item(key = "header") {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.flights_from, departure.iataCode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = departure.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        items(items = flights, key = { it.id }) { destination ->
            FlightRow(
                departure = departure,
                destination = destination,
                isFavorite = favoriteKeys.contains(
                    "${departure.iataCode}-${destination.iataCode}"
                ),
                onToggleFavorite = {
                    onToggleFavorite(departure.iataCode, destination.iataCode)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun FlightRow(
    departure: Airport,
    destination: Airport,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${departure.iataCode}  \u2192  ${destination.iataCode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatNumber(destination.passengers),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${departure.name}  \u2192  ${destination.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.remove_favorite else R.string.add_favorite
                ),
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Favorite>,
    onDeleteFavorite: (Favorite) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        EmptyMessage(
            message = stringResource(R.string.no_favorites),
            modifier = modifier
        )
        return
    }
    LazyColumn(modifier = modifier) {
        item(key = "header") {
            Text(
                text = stringResource(R.string.favorite_routes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            )
        }
        items(items = favorites, key = { it.id }) { favorite ->
            FavoriteRow(favorite = favorite, onDelete = { onDeleteFavorite(favorite) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun FavoriteRow(
    favorite: Favorite,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${favorite.departureCode}  \u2192  ${favorite.destinationCode}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.remove_favorite),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)
