package fr.vocaltech.location.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.vocaltech.location.LocationModel
import fr.vocaltech.location.models.Coordinates
import fr.vocaltech.location.models.Position

class PositionReceiver(private val viewModel: LocationModel): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val currentPos: Position
        val bundle = intent?.extras

        if (bundle != null) {
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")
            val ts = bundle.getLong("ts")

            currentPos = Position(
                Coordinates(lat, lng),
                ts,
                "cur_track_id",
                "cur_user_id"
            )
            viewModel.currentPos.value = currentPos
        }
    }
}