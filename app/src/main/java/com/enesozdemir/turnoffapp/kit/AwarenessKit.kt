package com.enesozdemir.turnoffapp.kit


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.enesozdemir.turnoffapp.AwarenessTimeInfo
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.capture.AmbientLightResponse
import com.huawei.hms.kit.awareness.capture.TimeCategoriesResponse


class AwarenessKit {

    val TAG = "AwarenessKit"

    fun getLightIntensity(context: Context) {
        Awareness.getCaptureClient(context).lightIntensity
            // Callback listener for execution success.
            .addOnSuccessListener { ambientLightResponse: AmbientLightResponse ->
                val ambientLightStatus = ambientLightResponse.ambientLightStatus
                Log.i(TAG, "Light intensity is " + ambientLightStatus.lightIntensity + " lux")
            }
            // Callback listener for execution failure.
            .addOnFailureListener { e: Exception? ->
                Log.e(TAG, "get light intensity failed", e)
            }

    }

    fun getAwarenessTimeInformation(
        context: Context,
        onSuccess: ((awarenessTimeInfo: AwarenessTimeInfo) -> Unit)? = null,
        onFail: ((e: Exception) -> Unit)? = null
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Awareness.getCaptureClient(context).timeCategories
            // Callback listener for execution success.
            .addOnSuccessListener { timeCategoriesResponse: TimeCategoriesResponse ->
                val timeCategory =
                    timeCategoriesResponse.timeCategories.timeCategories[timeCategoriesResponse.timeCategories.timeCategories.size - 1]
                val categories = timeCategoriesResponse.timeCategories
                val timeInfo: IntArray = categories.timeCategories
                Log.i(
                    TAG,
                    "Time intensity is " + timeCategoriesResponse.timeCategories.timeCategories + " "
                )
                onSuccess?.invoke(
                    AwarenessTimeInfo(timeCategory)
                )
            }
            // Callback listener for execution failure.
            .addOnFailureListener { e: Exception? ->
                Log.e(TAG, "get Time Categories failed", e)
                onFail?.invoke(e!!)
            }
    }
}