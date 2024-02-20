package com.example.arcorebasics

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Frame

var startX: Float = 0.0f
var startY: Float = 0.0f
var startZ: Float =0.0f
fun ProcessFrame(frame: Frame, i: Int, context: Context?) {

    val camera = frame.camera

    val translationX = camera.pose.translation[0]
    val translationY = camera.pose.translation[1]
    val translationZ = camera.pose.translation[2]
    val handler = Handler(Looper.getMainLooper())


    if(i == 20) {
         startX = translationX
         startY = translationY
        startZ = translationZ

        handler.post {
            Toast.makeText(context, "Record started", Toast.LENGTH_SHORT).show()
        }

    }
    var endX=translationX
    var endY=translationY
    var endZ=translationZ



    if(i>20) {
        var distanceX=endX - startX
        var distanceY=endY - startY
        var distanceZ=endZ - startZ

        var sqdistance=distanceX*distanceX+distanceY*distanceY+distanceZ*distanceZ
        val distance= Math.sqrt(sqdistance.toDouble())*100
        handler.post {
            Toast.makeText(context, "Distance:${distance}", Toast.LENGTH_SHORT).show()
        }
    }





//    Log.e("ProcessFrame", "TranslationX:${translationX}")
//    Log.e("ProcessFrame", "TranslationY:${translationY}")
//
//    Log.e("ProcessFrame", "TranslationZ:${translationZ}")


}
