package com.example.arcorebasics


import android.opengl.GLSurfaceView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding


import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun ImageCompose(glSurfaceView: GLSurfaceView) {
    var Bool by remember {
        mutableStateOf(true)
    }
    var status by remember {
        mutableStateOf("start")
    }
    val context = LocalContext.current

    val imaageSource = if (Bool) {
        painterResource(id = R.drawable.play)
    } else {
        painterResource(id = R.drawable.stop)

    }
    if (status.equals("stop")) {
        DialogBox()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.Bottom)
            .wrapContentWidth(Alignment.CenterHorizontally)

    ) {
        Image(
            painter = imaageSource,
            contentDescription = "PlayImage",
            modifier = Modifier
                .padding(20.dp)
                .size(70.dp)
                .clickable {
                    if (status != "stop") {
                        Bool = !Bool
                    }

                    Toast
                        .makeText(context, "Play", Toast.LENGTH_SHORT)
                        .show()
                    var dirtyRenderThread = Thread(Runnable {
                        while (true) {
                            glSurfaceView.requestRender()
                            try {
                                Thread.sleep(50)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                                return@Runnable
                            }
                        }
                    })
                    dirtyRenderThread.start()

                    if (status == "start") {
                        status = "In progress"
                    } else {
                        status = "stop"
                    }
                }
        )


    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogBox() {
    val openDialog = remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (openDialog.value) {
        AlertDialog(onDismissRequest = { openDialog.value = false },
            title = { Text(text = "Alert") },
            text = { Text(text = "Do you want to save this file?") },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    Toast.makeText(context, "your file is saved", Toast.LENGTH_SHORT).show()
                }

                ) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    Toast.makeText(context, "Your file is not saved", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "No")
                }
            })

    }
}


