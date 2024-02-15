
package com.example.arcorebasics;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtil {
    private static final String TAG = "ShaderUtil";

    public static int loadGLShader(String tag, Context context, int type, String filename) {
        String code = readShaderCodeFromResources(context, filename);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Check compilation status
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private static String readShaderCodeFromResources(Context context, String filename) {
        StringBuilder shaderCode = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                shaderCode.append(line).append('\n');
            }

            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not read shader file: " + filename);
            e.printStackTrace();
        }

        return shaderCode.toString();
    }
    public static void checkGLError(String tag, String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, label + ": OpenGL error: " + error);
            throw new RuntimeException(label + ": OpenGL error: " + error);
        }
    }
}
