package com.hornet.movies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hornet.movies.ui.MoviesViewModel
import com.hornet.movies.ui.components.GenreFilter
import com.hornet.movies.ui.components.MovieListItem
import com.hornet.movies.ui.components.PosterModal
import com.hornet.movies.ui.theme.HornetMoviesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HornetMoviesTheme {
                MoviesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= uiState.movies.size - 3) {
                    viewModel.loadMovies()
                }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Hornet Movies") }
            )
        },
        bottomBar = {
            GenreFilter(
                genreCounts = uiState.genreCounts,
                selectedGenreId = uiState.selectedGenreId,
                onGenreSelected = { genreId -> viewModel.selectGenre(genreId) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredMoviesByGenre = if (uiState.selectedGenreId != null) {
                    uiState.movies.filter { movieWithDetails ->
                        movieWithDetails.movie.genre_ids.contains(uiState.selectedGenreId)
                    }
                } else {
                    uiState.movies
                }

                items(filteredMoviesByGenre) { movieWithDetails ->
                    val isHighlighted = uiState.selectedGenreId != null &&
                            movieWithDetails.movie.genre_ids.contains(uiState.selectedGenreId)
                    MovieListItem(
                        movieWithDetails = movieWithDetails,
                        isHighlighted = isHighlighted,
                        onPosterClick = {
                            viewModel.showPosterModal(movieWithDetails.movie.id)
                        },
                        onMovieClick = {
                            viewModel.toggleMovieDetails(movieWithDetails.movie.id)
                        }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (uiState.selectedPosterId != null) {
        val selectedMovie = uiState.movies.find { it.movie.id == uiState.selectedPosterId }
        if (selectedMovie != null) {
            PosterModal(
                posterUrl = selectedMovie.movie.poster,
                movieTitle = selectedMovie.movie.title,
                onDismiss = { viewModel.hidePosterModal() }
            )
        }
    }
}