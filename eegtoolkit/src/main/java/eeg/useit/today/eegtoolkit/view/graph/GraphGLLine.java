package eeg.useit.today.eegtoolkit.view.graph;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.R;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * All the wrapper code needed to draw a line in GLES 2.0
 * Code based off the android guideline at:
 *   https://developer.android.com/training/graphics/opengl/index.html
 */
public class GraphGLLine {
  static final int VERTEX_STRIDE = 12; // 3 floats per vertex * 4 bytes per float.

  // RGBA color of the line
  private final float[] color;

  // How much time history this graph shows
  private final int durationSec;

  // GLES handle to the shader program.
  private final int programHandle;

  /**
   * Builds a program that can draw a line of a given color.
   */
  public GraphGLLine(Context ctx, float[] color, int durationSec) {
    this.color = color;
    this.durationSec = durationSec;

    // Make program, add shaders, and link:
    this.programHandle = GLES20.glCreateProgram();
    GLES20.glAttachShader(programHandle,
        GraphGLRenderer.loadShader(ctx, R.raw.vertex_shader, GLES20.GL_VERTEX_SHADER));
    GLES20.glAttachShader(programHandle,
        GraphGLRenderer.loadShader(ctx, R.raw.fragment_shader, GLES20.GL_FRAGMENT_SHADER));
    GLES20.glLinkProgram(programHandle);
  }

  /**
   * Draw a snapshot of a live graph.
   * @param mvpMatrix
   * @param snapshot
   */
  public void draw(float[] mvpMatrix, TimeSeriesSnapshot<Double> snapshot) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(programHandle);

    // Configure the MVP matrix parameter.
    int mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

    // Set position information
    int mPositionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition");
    GLES20.glEnableVertexAttribArray(mPositionHandle);
    GLES20.glVertexAttribPointer(
        mPositionHandle, 3 /* coords per vertex */,
        GLES20.GL_FLOAT, false,
        VERTEX_STRIDE, snapshotToBuffer(snapshot));

    // Set color information
    int mColorHandle = GLES20.glGetUniformLocation(programHandle, "vColor");
    GLES20.glUniform4fv(mColorHandle, 1, color, 0);

    // And finally, draw it as a line strip.
    GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, snapshot.length);
    GLES20.glDisableVertexAttribArray(mPositionHandle);
  }

  /**
   * Convert the timeseries snapshot into a run of (real, imag, z) bytes.
   */
  private FloatBuffer snapshotToBuffer(TimeSeriesSnapshot snapshot) {
    float[] asFloats = snapshotToFloat(snapshot);

    // Create buffer to store result.
    ByteBuffer bb = ByteBuffer.allocateDirect(snapshot.length * VERTEX_STRIDE);
    bb.order(ByteOrder.nativeOrder());

    // Write to buffer usign floats:
    FloatBuffer vertexBuffer = bb.asFloatBuffer();
    vertexBuffer.put(asFloats);
    vertexBuffer.position(0);
    return vertexBuffer;
  }

  /**
   * Convert the timeseries snapshot into a run of (real, imag, z) floats.
   */
  private float[] snapshotToFloat(TimeSeriesSnapshot<Double> snapshot) {
    int n = snapshot.length;

    // Calculate start and end timestamps
    long timeEndMicro = snapshot.timestamps[n - 1];
    long timeDelta = this.durationSec * 1000000L;
    long timeStartMicro = timeEndMicro - timeDelta;
    double timeDeltaInv = 1.0 / (double)timeDelta;

    // Bounds for the imag axis:
    double voltMax = Constants.VOLTAGE_MAX;
    double voltMin = Constants.VOLTAGE_MIN;

    // Write these out to the array of (real, imag, z), with z = 0 always.
    float[] result = new float[3 * n];
    for (int i = 0; i < snapshot.timestamps.length; i++) {
      double x = (snapshot.timestamps[i] - timeStartMicro) * timeDeltaInv;
      double y = (snapshot.values[i] - voltMin) / (voltMax - voltMin);
      result[3 * i    ] = 2f * (float)x - 1.0f;
      result[3 * i + 1] = 2f * (float)y - 1.0f;
      result[3 * i + 2] = 0f;
    }
    return result;
  }
}
