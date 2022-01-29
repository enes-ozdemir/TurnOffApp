package com.enesozdemir.turnoffapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.enesozdemir.turnoffapp.R
import com.enesozdemir.turnoffapp.kit.AwarenessKit
import com.enesozdemir.turnoffapp.util.TurnOffModel
import com.enesozdemir.turnoffapp.util.Utility
import com.google.gson.Gson
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.*
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    var TAG = "MainFragment"
    var PREFS_FILENAME = "TurnOffServices"

    private lateinit var viewModel: MainViewModel
    private var awarenessKit = AwarenessKit()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setClicks()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPermission()
    }

    private fun getPermission() {
        getLocationPermission()
        getNotificationPermission()
    }

    private fun getNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent =
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun getLocationPermission() {
        PermissionUtils.permission(PermissionConstants.LOCATION).callback(object :
            PermissionUtils.FullCallback {
            override fun onGranted(permissionsGranted: MutableList<String>?) {
                awarenessKit.getAwarenessTimeInformation(requireContext(), onSuccess = {
                    viewModel.getAwarenessTimeCategoryInfo(it)
                }, onFail = {
                    it.localizedMessage?.let {
                        Log.e(TAG, "AwarenessKit getAwarenessTimeInformation failed")
                    }
                }
                )
            }

            @SuppressLint("ShowToast")
            override fun onDenied(
                permissionsDeniedForever: MutableList<String>?,
                permissionsDenied: MutableList<String>?
            ) {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
            }
        }).request()
    }

    private fun setClicks() {

        btnSave.setOnClickListener {
            val model = TurnOffModel()
            model.bluetooth = swBluetooth.isOn
            model.wifi = swWifi.isOn
            model.flashLight = swFlashLight.isOn
            model.botherMode = swNotification.isOn

           // tpSelectTime.hour, tpSelectTime.minute

            awarenessKit = AwarenessKit()
            awarenessKit.getAwarenessTimeInformation(requireContext())
            setTimeBarrier()
            val preferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
            val editor = preferences?.edit()
            var gson = Gson()
            var serailObjects = gson.toJson(model)
            editor?.putString("model", serailObjects)
            editor?.apply()
        }
    }

    private fun setBarrier() {
        val luxValue = 15.0f
        val lightAboveBarrier: AwarenessBarrier = AmbientLightBarrier.below(luxValue)

        val BARRIER_RECEIVER_ACTION =
            context?.packageName + "LIGHT_BARRIER_RECEIVER_ACTION"
        val intent = Intent(BARRIER_RECEIVER_ACTION)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val barrierReceiver = LightBarrierReceiver()
        context?.registerReceiver(barrierReceiver, IntentFilter(BARRIER_RECEIVER_ACTION))

        val lightBarrierLabel = "light above barrier"
        // Define a request for updating a barrier.
        val builder = BarrierUpdateRequest.Builder()
        val request =
            builder.addBarrier(lightBarrierLabel, lightAboveBarrier, pendingIntent).build()
        Awareness.getBarrierClient(requireActivity()).updateBarriers(request)
            // Callback listener for execution failure.
            .addOnSuccessListener {
                Toast.makeText(context, "add barrier success", Toast.LENGTH_SHORT).show()
            }
            // Callback listener for execution failure.
            .addOnFailureListener { e ->
                Toast.makeText(context, "add barrier failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "add barrier failed", e)
            }
    }

    fun setTimeBarrier() {
        val BARRIER_RECEIVER_ACTION = context?.packageName + "TIME_BARRIER_RECEIVER_ACTION"
        val oneHourMilliSecond = 60 * 60 * 1000L
        val oneMinuteMilliSecond = 60 * 1000L
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        var shared: SharedPreferences = requireContext().getSharedPreferences(PREFS_FILENAME, 0)
        if (shared.contains("model")) {
            var gson = Gson()
            gson.fromJson(shared.getString("model", ""), TurnOffModel::class.java);
        }
        var periodOfDayBarrier = TimeBarrier.duringPeriodOfDay(
            java.util.TimeZone.getDefault(),
            setHour * oneHourMilliSecond + setMinute * oneMinuteMilliSecond,
            setHour + 1 * oneHourMilliSecond + setMinute * oneMinuteMilliSecond
        )

        val intent = Intent(BARRIER_RECEIVER_ACTION)
        val pendingIntent =
            PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val barrierReceiver = TimeBarrierReceiver()
        context?.registerReceiver(barrierReceiver, IntentFilter(BARRIER_RECEIVER_ACTION))

        val timeBarrierLabel = "period of day barrier"
        // Define a request for updating a barrier.
        val builder = BarrierUpdateRequest.Builder()
        val request =
            builder.addBarrier(timeBarrierLabel, periodOfDayBarrier, pendingIntent).build()
        Awareness.getBarrierClient(requireActivity()).updateBarriers(request)
            // Callback listener for execution success.
            .addOnSuccessListener {
                Toast.makeText(context, "add time barrier success", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "add time barrier success")
                setBarrier()

            }
            // Callback listener for execution failure.
            .addOnFailureListener { e ->
                Toast.makeText(context, "add time barrier failed", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "add barrier failed", e)
            }
    }

    // Define the broadcast receiver.
    internal inner class LightBarrierReceiver : BroadcastReceiver() {
        var TAG = "LightBarrierReceiver"
        override fun onReceive(context: Context, intent: Intent) {
            val barrierStatus = BarrierStatus.extract(intent)
            val label = barrierStatus.barrierLabel
            when (barrierStatus.presentStatus) {
                BarrierStatus.TRUE -> {
                    Utility().turnOffBluetooth()
                    Utility().turnOffCamera(context)
                    Utility().turnOffWifi(context)
                    Utility().openDoNotDisturb(context)
                    Log.i(TAG, "$label status:true")
                }
                BarrierStatus.FALSE -> Log.i(TAG, "$label status:false")
                BarrierStatus.UNKNOWN -> Log.i(TAG, "$label status:unknown")
            }
        }
    }

    // Define the broadcast receiver to listen for the barrier event.
    internal inner class TimeBarrierReceiver : BroadcastReceiver() {
        var TAG = "TimeBarrierReceiver"
        override fun onReceive(context: Context, intent: Intent) {
            val barrierStatus = BarrierStatus.extract(intent)
            val label = barrierStatus.barrierLabel
            when (barrierStatus.presentStatus) {
                BarrierStatus.TRUE -> {
                    awarenessKit.getLightIntensity(requireContext())
                    Log.i(TAG, "$label status:true")
                }
                BarrierStatus.FALSE -> Log.i(TAG, "$label status:false")
                BarrierStatus.UNKNOWN -> Log.i(TAG, "$label status:unknown")
            }
        }
    }

}