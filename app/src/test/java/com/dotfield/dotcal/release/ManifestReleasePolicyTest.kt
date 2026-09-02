package com.dotfield.dotcal.release

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ManifestReleasePolicyTest {
    @Test
    fun scheduleExactAlarmIsCappedBelowUseExactAlarmDevices() {
        val manifest = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(File("src/main/AndroidManifest.xml"))

        val permissions = manifest.getElementsByTagName("uses-permission")
        var scheduleExactAlarmMaxSdk: String? = null
        var hasUseExactAlarm = false

        for (index in 0 until permissions.length) {
            val node = permissions.item(index)
            val name = node.attributes.getNamedItem("android:name")?.nodeValue
            if (name == "android.permission.SCHEDULE_EXACT_ALARM") {
                scheduleExactAlarmMaxSdk = node.attributes.getNamedItem("android:maxSdkVersion")?.nodeValue
            }
            if (name == "android.permission.USE_EXACT_ALARM") {
                hasUseExactAlarm = true
            }
        }

        assertEquals("32", scheduleExactAlarmMaxSdk)
        assertNotNull(scheduleExactAlarmMaxSdk)
        assertEquals(true, hasUseExactAlarm)
    }
}
