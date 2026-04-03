package com.example.fc_006.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fc_006.model.Asteroid
import com.example.fc_006.network.NasaNeoApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.random.Random

sealed class AsteroidUiState {
    data object Idle : AsteroidUiState()
    data object Loading : AsteroidUiState()
    data class Success(val asteroid: Asteroid) : AsteroidUiState()
    data class Error(val message: String) : AsteroidUiState()
}

class AsteroidViewModel : ViewModel() {

    private val _uiState = MutableLiveData<AsteroidUiState>(AsteroidUiState.Idle)
    val uiState: LiveData<AsteroidUiState> = _uiState

    fun scanForAsteroids(apiKey: String) {
        viewModelScope.launch {
            _uiState.value = AsteroidUiState.Loading
            try {
                val response = NasaNeoApi.service.getAsteroids(
                    apiKey = apiKey,
                    page = Random.nextInt(0, 5)
                )

                val asteroid = response.nearEarthObjects
                    .shuffled()
                    .firstOrNull { it.closeApproachData.isNotEmpty() }
                    ?: response.nearEarthObjects.randomOrNull()

                if (asteroid != null) {
                    _uiState.value = AsteroidUiState.Success(asteroid)
                } else {
                    _uiState.value = AsteroidUiState.Error("No tracked asteroids were returned.")
                }
            } catch (exception: HttpException) {
                _uiState.value =
                    AsteroidUiState.Error("Mission Control link failed: HTTP ${exception.code()}.")
            } catch (_: Exception) {
                _uiState.value =
                    AsteroidUiState.Error("Mission Control lost asteroid tracking data.")
            }
        }
    }
}
