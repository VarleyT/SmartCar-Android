package plus.a66.smartcar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import plus.a66.smartcar.constant.MotorSpeed
import plus.a66.smartcar.constant.MotorStatus
import plus.a66.smartcar.databinding.ActivityMainBinding
import plus.a66.smartcar.model.ControlStatusViewModel
import plus.a66.smartcar.model.ControlStatusViewModelFactory
import plus.a66.smartcar.model.WiFiStatusViewModel
import plus.a66.smartcar.model.WiFiStatusViewModelFactory


class MainActivity : AppCompatActivity(), View.OnTouchListener {
    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private lateinit var wifiStatusVM: WiFiStatusViewModel
    private lateinit var controlStatusVM: ControlStatusViewModel

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        initEvent()
    }

    private fun initView() {
        wifiStatusVM = ViewModelProvider(
            this,
            WiFiStatusViewModelFactory(this)
        ).get(WiFiStatusViewModel::class.java)
        controlStatusVM = ViewModelProvider(
            this,
            ControlStatusViewModelFactory(this)
        ).get(ControlStatusViewModel::class.java)

        binding.lifecycleOwner = this
        binding.controlStatusVM = controlStatusVM
        binding.wifiStatusVM = wifiStatusVM

        binding.connect.setTextColor(getColor(R.color.green))
        binding.carConnStatus.setTextColor(getColor(R.color.green))
    }

    private fun initData() {
        wifiStatusVM.wifiStatusData.observe(this) {
            binding.wifiStatus.text = it
        }
        controlStatusVM.carConnectionData.observe(this) {
            binding.carConnStatus.apply {
                text = it
                if (text == getString(R.string.car_disconnected)) setTextColor(getColor(R.color.red))
                else setTextColor(getColor(R.color.green))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        binding.connect.setOnClickListener {
            when ((it as Button).text) {
                getString(R.string.connect) -> {
                    it.apply {
                        text = getString(R.string.disconnect)
                        setTextColor(getColor(R.color.red))
                    }
                    controlStatusVM.startServer()
                }

                getString(R.string.disconnect) -> {
                    it.apply {
                        text = getString(R.string.connect)
                        setTextColor(getColor(R.color.green))
                    }
                    controlStatusVM.setCarConnectionData(getString(R.string.car_disconnected))
                    controlStatusVM.stopServer()
                }
            }
        }
        binding.updateSpeed.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("选择速度")
                .setItems(arrayOf("低速", "中速", "高速")) { _, i ->
                    when (i) {
                        0 -> controlStatusVM.setSpeed(MotorSpeed.LOW)
                        1 -> controlStatusVM.setSpeed(MotorSpeed.MEDIUM)
                        2 -> controlStatusVM.setSpeed(MotorSpeed.HIGH)
                    }
                }
                .show()
        }
        binding.wifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        binding.forward.setOnTouchListener(this)
        binding.backward.setOnTouchListener(this)
        binding.left.setOnTouchListener(this)
        binding.right.setOnTouchListener(this)
        binding.stop.setOnTouchListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                when (v!!.id) {
                    R.id.forward -> {
                        binding.forward.isPressed = true
                        controlStatusVM.setDirection(MotorStatus.FORWARD)
                    }
                    R.id.backward -> {
                        binding.backward.isPressed = true
                        controlStatusVM.setDirection(MotorStatus.BACKWARD)
                    }
                    R.id.left -> {
                        binding.left.isPressed = true
                        controlStatusVM.setDirection(MotorStatus.LEFT)
                    }
                    R.id.right -> {
                        binding.right.isPressed = true
                        controlStatusVM.setDirection(MotorStatus.RIGHT)
                    }
                }
                true
            }
            MotionEvent.ACTION_UP -> {
                binding.forward.isPressed = false
                binding.backward.isPressed = false
                binding.left.isPressed = false
                binding.right.isPressed = false
                binding.stop.isPressed = false
                controlStatusVM.setDirection(MotorStatus.STOP)
                true
            }
            else -> true
        }
    }
}