package com.example.arcorebasics

import android.content.ContentValues.TAG
import android.media.MediaRecorder
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.example.arcorebasics.Helper.CameraPermissionHelper
import com.example.arcorebasics.ui.theme.ArcoreBasicsTheme
import com.google.ar.core.ArCoreApk
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.SessionPausedException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumSet
import java.util.Locale
import java.util.TimeZone
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : ComponentActivity(), GLSurfaceView.Renderer {
    private var glSurfaceView: GLSurfaceView? = null

    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private var backgroundRenderer: BackgroundRenderer? = null
    private var renderCameraView = false
    lateinit var settings: ImageButton
    private lateinit var mediaRecorder: MediaRecorder
    lateinit var arcorehelper: ARCoreSessionLifecycleHelper



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaRecorder = MediaRecorder()
        glSurfaceView = findViewById(R.id.surfaceview)
        arcorehelper = ARCoreSessionLifecycleHelper(this)



        if (isARCoreSupportedAndUpToDate()) {
            Toast.makeText(this, "Hurrah, it's working", Toast.LENGTH_LONG).show()
            initializeARCore()
            initGLSurfaceView();
        } else {
            Toast.makeText(this, "ARCore not supported", Toast.LENGTH_LONG).show()
        }

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            ArcoreBasicsTheme {

                ImageCompose(glSurfaceView!!)
            }
        }

    }
    fun startRenderModeDirty() {
        var dirtyRenderThread = Thread(Runnable {
            while (true) {
                glSurfaceView!!.requestRender()
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return@Runnable
                }
            }
        })
        dirtyRenderThread.start()
    }
        // This code is for intialize the glsurfaceview
    private fun initGLSurfaceView() {
        // Configure the basic properties of GLSurfaceView and set renderer.
        glSurfaceView!!.preserveEGLContextOnPause = true
        glSurfaceView!!.setEGLContextClientVersion(2)

        glSurfaceView!!.setRenderer(this)
        glSurfaceView!!.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


        Toast.makeText(this, "hurrah glSurfaceView initialization", Toast.LENGTH_LONG).show()
    }

    private fun initializeARCore() {
        try {
            if (mSession == null) {
                // Create AR session.
                mSession = Session(this)
                val config = Config(mSession)
                mSession?.configure(config)

                // Create a camera config filter for the session.
                val filter = CameraConfigFilter(mSession)
                filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)
                val cameraConfigList = mSession!!.getSupportedCameraConfigs(filter)
                mSession!!.cameraConfig = cameraConfigList[0]

            }
        } catch (e: UnavailableException) {
            handleSessionException(e)
        }
        Log.d(TAG, "ARCore initialized successfully")


    }


    private fun handleSessionException(exception: Exception) {
        Log.e("ARCore", "Exception creating AR session", exception)
        Toast.makeText(this, "Failed to create AR session", Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        super.onResume()

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            println("Please enable camera permission")
            return
        } else {
            Toast.makeText(this, "Hurrah, Camera permission enabled", Toast.LENGTH_SHORT).show()
        }

        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        Toast.makeText(this, "AR session created", Toast.LENGTH_LONG).show()
                    }

                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, "TODO: handle exception $e", Toast.LENGTH_LONG).show()
            return
        } catch (e: Exception) {
            return
        }

        // Attempt to resume the AR session
        try {
            mSession?.resume()
        } catch (e: CameraNotAvailableException) {
            // Handle the exception appropriately.
            e.printStackTrace()
        }
        glSurfaceView?.onResume()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            ).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
        if (mSession != null) {
            mSession!!.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSession != null) {
            mSession!!.close()
            mSession = null
        }
    }


    private fun isARCoreSupportedAndUpToDate(): Boolean {
        return when (ArCoreApk.getInstance().checkAvailability(this)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD, ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                try {
                    when (ArCoreApk.getInstance().requestInstall(this, true)) {
                        ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                            Log.i(TAG, "ARCore installation requested.")
                            false
                        }

                        ArCoreApk.InstallStatus.INSTALLED -> true
                    }
                } catch (e: UnavailableException) {
                    Log.e(TAG, "ARCore not installed", e)
                    false
                }
            }

            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE ->
                false

            ArCoreApk.Availability.UNKNOWN_CHECKING, ArCoreApk.Availability.UNKNOWN_ERROR, ArCoreApk.Availability.UNKNOWN_TIMED_OUT ->
                false
        }
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f)

        try {
            Log.d(TAG, "surface created sdaffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
            // Initialize OpenGL settings for drawing background and Virtual Object
            // It mainly includes textureId, Texture Coordinations, Shader, Program, and so on.
            backgroundRenderer = BackgroundRenderer()

            // Call createOnGlThread with the session
            backgroundRenderer?.createOnGlThread(this, mSession!!)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read an asset file", e)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val displayRotation = getSystemService(WindowManager::class.java).defaultDisplay.rotation
        mSession?.setDisplayGeometry(displayRotation, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (mSession == null || backgroundRenderer == null) {
            return
        }

        try {
            val frame: Frame = mSession!!.update()



            // Get the capture time of the AR frame in milliseconds

            val frameCaptureTimeNanos = frame.timestamp

            // Convert the capture time to a human-readable format
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date(frameCaptureTimeNanos / 1_000_000))


            Log.d(TAG, "AR Frame captured at: $formattedDate")








            val screenWidth = glSurfaceView?.width ?: 1
            val screenHeight = glSurfaceView?.height ?: 1

            // Calculate center coordinates
            val centerX = screenWidth / 2
            val centerY = screenHeight / 2

            Log.d("center of the screen","centerX:${centerX};;;;;;;;;centerY:${centerY}")





                backgroundRenderer?.draw(frame)



                // Update previous camera pose


        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
        } catch (e: SessionPausedException) {
            Log.e(TAG, "AR session is paused", e)
        }
    }





}
