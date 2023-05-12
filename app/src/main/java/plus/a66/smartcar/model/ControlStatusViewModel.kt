package plus.a66.smartcar.model

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import plus.a66.smartcar.MESSAGE_NEW_CLIENT
import plus.a66.smartcar.R
import plus.a66.smartcar.TcpServer
import plus.a66.smartcar.constant.MotorStatus

const val PORT = 8000

class ControlStatusViewModel(private val context: Context) : ViewModel() {
    private val mutableLiveData =
        MutableLiveData<String>().apply { value = context.getString(R.string.car_disconnected) }

    val carConnectionData: LiveData<String> get() = mutableLiveData

    private val tcpServerHandler = Handler {
        if (it.what == MESSAGE_NEW_CLIENT) {
            mutableLiveData.value = context.getString(R.string.car_connected)
            true
        } else false
    }
    private var tcpServer: TcpServer? = null

    private val mThread: HandlerThread = HandlerThread("mThread").apply { start() }
    private val handler = Handler(mThread.looper)

    fun startServer() {
        tcpServer = TcpServer(PORT, tcpServerHandler)
        tcpServer?.start()
    }

    fun stopServer() = tcpServer?.quit()

    fun setDirection(motorStatus: MotorStatus) {
        if (tcpServer?.isAnyClientConnected()!!) {
            handler.post {
                tcpServer!!.sendMessageToClients(motorStatus.cmd)
            }
        } else {
            Toast.makeText(context, "无客户端连接！", Toast.LENGTH_SHORT).show()
        }
    }

    fun onStop(view: View?) {
        if (view is Button) {
            setDirection(MotorStatus.STOP)
        }
    }

    fun setCarConnectionData(carConnectionData: String) =
        mutableLiveData.postValue(carConnectionData)

    override fun onCleared() {
        super.onCleared()
        tcpServer?.quit()
    }
}

class ControlStatusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlStatusViewModel::class.java)) {
            return ControlStatusViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}