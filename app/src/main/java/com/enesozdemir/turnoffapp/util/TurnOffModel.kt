package com.enesozdemir.turnoffapp.util

import java.util.*

data class TurnOffModel(
    var bluetooth: Boolean = false,
    var wifi: Boolean = false,
    var flashLight: Boolean = false,
    var botherMode: Boolean = false,
    var selectedTime: TimeZone? = null
)