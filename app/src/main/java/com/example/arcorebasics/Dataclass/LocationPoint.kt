package com.example.arcorebasics.Dataclass



import com.google.ar.sceneform.math.Vector3

data class LocationPoint(
    val timestamp:String,
    val frameTime:String,
    val translation:Vector3,
)

