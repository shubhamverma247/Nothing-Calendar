package com.dotfield.dotcal.quickadd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dotfield.dotcal.MainActivity
import com.dotfield.dotcal.R

class QuickAddTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.quick_add_title)
            icon = Icon.createWithResource(this@QuickAddTileService, R.mipmap.ic_launcher)
            updateTile()
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent = quickAddIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

internal const val QUICK_ADD_DEEP_LINK = "dotcal://quick-add"

internal fun quickAddUri(): Uri = Uri.parse(QUICK_ADD_DEEP_LINK)

internal fun quickAddIntent(context: Context): Intent = Intent(Intent.ACTION_VIEW, quickAddUri()).apply {
    setClass(context, MainActivity::class.java)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
}
