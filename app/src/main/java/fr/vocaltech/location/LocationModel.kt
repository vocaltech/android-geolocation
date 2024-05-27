package fr.vocaltech.location

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.vocaltech.location.models.Position
import fr.vocaltech.location.services.PositionService
import fr.vocaltech.location.services.retrofit.RetrofitClient
import fr.vocaltech.location.services.retrofit.RetrofitInterface
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "LocationModel"

class LocationModel(private val application: LocationApplication): ViewModel() {
    private var apiUrl: String

    private val _isLocationServiceStarted = MutableLiveData(false)
    val isLocationServiceStarted: MutableLiveData<Boolean> = _isLocationServiceStarted
    val currentPos: MutableLiveData<Position> = MutableLiveData<Position>()
    private val _isRetrofitLoading = MutableLiveData(true)
    val isRetrofitLoading: MutableLiveData<Boolean> = _isRetrofitLoading
    private val _positionsByUserId: MutableLiveData<List<Position>> = MutableLiveData(emptyList())
    val positionsByUserId: MutableLiveData<List<Position>> = _positionsByUserId

    private var retrofitService: RetrofitInterface

    // --------------------------------------------------------------------
    // --- retrofit section - begin ---

    fun positionsByUserId(userid: String) {
        viewModelScope.launch {
            //val retrofitService = RetrofitClient.getClient(apiUrl).create(RetrofitInterface::class.java)
            val call: Call<List<Position>> = retrofitService.positionsByUserId(userid)
            call.enqueue(object: Callback<List<Position>> {
                override fun onResponse(call: Call<List<Position>>, response: Response<List<Position>>) {
                    if (response.isSuccessful) {
                        _isRetrofitLoading.value = false
                        _positionsByUserId.value = response.body()

                        Log.d(TAG, "[positionsByUserId.onResponse()]: ${_positionsByUserId.value}")
                    }
                }
                override fun onFailure(call: Call<List<Position>>, t: Throwable) {
                    _isRetrofitLoading.value = false
                    Log.d(TAG, "[positionsByUserId().onFailure()] $t")
                }
            })
        }
    }

    fun deletePositionsByUserId(userid: String) {
        viewModelScope.launch {
            val call: Call<Void> = retrofitService.deletePositionsByUserId(userid)
            call.enqueue(object: Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        _isRetrofitLoading.value = false
                        Log.d(TAG, "[deletePositionsByUserId.onResponse()]...")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _isRetrofitLoading.value = false
                    Log.d(TAG, "[deletePositionsByUserId().onFailure()] $t")
                }
            })
        }
    }

    // --- retrofit section - end ---
    // --------------------------------------------------------------------

    fun toggleStartLocationService() = if (_isLocationServiceStarted.value == true) stopLocationService() else startLocationService()

    private fun startLocationService() {
        _isLocationServiceStarted.value = true
        application.startService(Intent(application.applicationContext, PositionService::class.java))
    }

    private fun stopLocationService() {
        _isLocationServiceStarted.value = false
        application.stopService(Intent(application.applicationContext, PositionService::class.java))
    }

    init {
        // dotenv handling
        val dotenv = Dotenv.configure()
            .directory("/assets")
            .filename("env")
            .load()

        apiUrl = dotenv.get("LOCATIONS_URL")

        // create retrofit interface
        retrofitService = RetrofitClient.getClient(apiUrl).create(RetrofitInterface::class.java)
        //positionsByUserId("userId")
    }
}

class LocationModelFactory(private val application: LocationApplication): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}