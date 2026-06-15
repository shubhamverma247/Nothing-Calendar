package com.dotfield.dotcal

import android.app.Application
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.DotCalRepository

class DotCalApplication : Application() {
    val database: DotCalDatabase by lazy { DotCalDatabase.create(this) }
    val repository: DotCalRepository by lazy { DotCalRepository(database.calendarDao()) }
}
