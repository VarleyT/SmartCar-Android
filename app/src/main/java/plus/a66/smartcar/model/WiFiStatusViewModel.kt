package plus.a66.smartcar.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import plus.a66.smartcar.R

class WiFiStatusViewModel(private val context: Context) : ViewModel() {
    private val mutableLiveData =
        MutableLiveData<String>().apply { value = context.getString(R.string.wifi_disconnected) }

    val wifiStatusData: LiveData<String> get() = mutableLiveData

    fun setWiFiStatus(wifiStatus: String) {
        mutableLiveData.postValue(wifiStatus)
    }
}

class WiFiStatusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WiFiStatusViewModel::class.java)) {
            return WiFiStatusViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}