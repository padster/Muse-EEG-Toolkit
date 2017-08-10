package eeg.useit.today.eegtoolkit.view.graph;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import eeg.useit.today.eegtoolkit.Util;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * OpenGL renderer that draws a timeseries line onto a GLSurfaceView.
 */
public class GraphGLRenderer implements GLSurfaceView.Renderer {
  /** @return Shader ID given source to load from, and shader type. */
  public static int loadShader(Context context, int rawId, int type){
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, Util.resourceToText(context, rawId));
    GLES20.glCompileShader(shader);
    return shader;
  }

  // Context this runs within.
  private Context ctx;
  // Number of seconds of graph to display.
  private int durationSec;

  /** GLES Line that is used to draw the data. */
  private GraphGLLine line;

  /** Source of timeseries data to draw. Set when view is attached. */
  private TimeSeries<Double> timeSeries;

  /** Model View Projection Matrix to convert world to view coordinates. */
  private final float[] mMVPMatrix = new float[16];

  /** Create a renderer that draws a given duration of time. */
  public GraphGLRenderer(Context ctx, int durationSec) {
    this.ctx = ctx;
    this.durationSec = durationSec;

    // Set up the view matrix, projection, then combine to make MVP matrix
    float[] mViewMatrix = new float[16];
    float[] mProjectionMatrix = new float[16];
    Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1, 1, 3, 7);
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
  }

  /** Attach a viewmodel to the renderer. */
  public void setTimeSeries(TimeSeries<Double> ts) {
    assert this.timeSeries == null; // Only do once...
    this.timeSeries = ts;
  }

  @Override
  public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    // Set the background frame color
    GLES20.glClearColor(0f, 0f, 0f, 1f);
    GLES20.glEnable(GL10.GL_LINE_SMOOTH);

    // IMPORTANT: This must be created here, it can't be moved to the constructor.
    this.line = new GraphGLLine(ctx, new float[]{0f, 1f, 0f, 1f}, durationSec);
  }

  @Override
  public void onSurfaceChanged(GL10 gl10, int width, int height) {
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl10) {
    // Redraw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

    if (this.timeSeries == null || this.timeSeries.isEmpty()) {
      // If not set yet, just draw nothing.
      return;
    }

    // Otherwise, make a snapshot and draw it:
    TimeSeriesSnapshot<Double> snapshot = this.timeSeries.getRecentSnapshot(Double.class);
    line.draw(mMVPMatrix, snapshot);
  }
}
