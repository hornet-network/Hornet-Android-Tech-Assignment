package com.hornet.movies.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hornet.movies.ui.GenreCount
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilter(
    genreCounts: List<GenreCount>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Genres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onGenreSelected(null) },
                    label = { Text("All") },
                    selected = selectedGenreId == null,
                    modifier = Modifier.wrapContentWidth()
                )

                genreCounts.forEach { genreCount ->
                    FilterChip(
                        onClick = {
                            onGenreSelected(
                                if (selectedGenreId == genreCount.genre.id) null
                                else genreCount.genre.id
                            )
                        },
                        label = {
                            Text("${genreCount.genre.name} (${genreCount.count})")
                        },
                        selected = selectedGenreId == genreCount.genre.id,
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
        }
    }
}