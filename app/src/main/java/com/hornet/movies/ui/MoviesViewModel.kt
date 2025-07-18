package com.hornet.movies.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hornet.movies.data.MoviesService
import com.hornet.movies.data.model.meta.Genre
import com.hornet.movies.data.model.movie.Movie
import com.hornet.movies.data.model.movie.MovieDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GenreCount(
    val genre: Genre,
    val count: Int
)

data class MovieWithDetails(
    val movie: Movie,
    val isExpanded: Boolean = false,
    val details: MovieDetails? = null,
    val isLoadingDetails: Boolean = false
)

data class MoviesUiState(
    val movies: List<MovieWithDetails> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val genreCounts: List<GenreCount> = emptyList(),
    val selectedGenreId: Int? = null,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val selectedPosterId: Int? = null,
    val currentPage: Int = 1
)

class MoviesViewModel : ViewModel() {

    private val moviesService = MoviesService.getInstance()

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadGenres()
        loadMovies()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val genresResponse = moviesService.getGenres()
                _uiState.value = _uiState.value.copy(genres = genresResponse.genres)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun loadMovies() {
        if (_uiState.value.isLoading || _uiState.value.hasMore.not()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = moviesService.getTopMovies(_uiState.value.currentPage)
                val newMovies = response.results
                val filteredMovies = newMovies.filter { it.vote_average >= 7.0 }
                val hasMore = newMovies.isNotEmpty() && filteredMovies.size == newMovies.size
                val moviesWithDetails = filteredMovies.map { movie ->
                    MovieWithDetails(movie = movie)
                }

                val finalResult = _uiState.value.movies + moviesWithDetails
                _uiState.value = _uiState.value.copy(
                    movies = finalResult,
                    isLoading = false,
                    hasMore = hasMore,
                    currentPage = _uiState.value.currentPage + 1
                )

                updateGenreCounts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }

        }
    }

    fun showPosterModal(movieId: Int) {
        _uiState.value = _uiState.value.copy(selectedPosterId = movieId)
    }

    fun hidePosterModal() {
        _uiState.value = _uiState.value.copy(selectedPosterId = null)
    }

    fun toggleMovieDetails(movieId: Int) {
        val updatedMovies = _uiState.value.movies.map { movieWithDetails ->
            if (movieWithDetails.movie.id == movieId) {
                val newExpandedState = movieWithDetails.isExpanded.not()
                if (newExpandedState && movieWithDetails.details == null && movieWithDetails.isLoadingDetails.not()) {
                    loadMovieDetails(movieId)
                }
                movieWithDetails.copy(isExpanded = newExpandedState)
            } else {
                movieWithDetails
            }
        }

        _uiState.value = _uiState.value.copy(movies = updatedMovies)
    }

    private fun updateGenreCounts() {
        val genres = _uiState.value.genres
        val movies = _uiState.value.movies

        val genreCounts = genres.mapNotNull { genre ->
            val count = movies.count { movieWithDetails ->
                movieWithDetails.movie.genre_ids.contains(genre.id)
            }
            if (count > 0) GenreCount(genre, count) else null
        }

        _uiState.value = _uiState.value.copy(genreCounts = genreCounts)
    }

    private fun loadMovieDetails(movieId: Int) {
        val updatedMovies = _uiState.value.movies.map { movieWithDetails ->
            if (movieWithDetails.movie.id == movieId) {
                movieWithDetails.copy(isLoadingDetails = true)
            } else {
                movieWithDetails
            }
        }
        _uiState.value = _uiState.value.copy(movies = updatedMovies)

        viewModelScope.launch {
            try {
                val details = moviesService.getMovieDetails(movieId)

                val finalUpdatedMovies = _uiState.value.movies.map { movieWithDetails ->
                    if (movieWithDetails.movie.id == movieId) {
                        movieWithDetails.copy(
                            details = details,
                            isLoadingDetails = false
                        )
                    } else {
                        movieWithDetails
                    }
                }

                _uiState.value = _uiState.value.copy(movies = finalUpdatedMovies)
            } catch (e: Exception) {
                Log.e("Error", "-------------------------\n${e.message}\n-------------------------\n")
                val errorUpdatedMovies = _uiState.value.movies.map { movieWithDetails ->
                    if (movieWithDetails.movie.id == movieId) {
                        movieWithDetails.copy(isLoadingDetails = false)
                    } else {
                        movieWithDetails
                    }
                }

                _uiState.value = _uiState.value.copy(movies = errorUpdatedMovies)
            }
        }
    }

    fun selectGenre(genreId: Int?) {
        _uiState.value = _uiState.value.copy(selectedGenreId = genreId)
    }
}