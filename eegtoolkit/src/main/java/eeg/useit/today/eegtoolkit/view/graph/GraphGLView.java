package eeg.useit.today.eegtoolkit.view.graph;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import eeg.useit.today.eegtoolkit.model.LiveSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeries;

/** GLSurfaceView that draws a timeseries line to the view. */
public class GraphGLView extends GLSurfaceView {
  private static final int DEFAULT_DURATION_SEC = 10;

  /** Renderer that handles the drawing for this GL surface.*/
  private final GraphGLRenderer renderer;

  /**
   * Create a GraphGL view, set a renderer to show the configured duration to time.
   */
  public GraphGLView(Context context, AttributeSet attrs) {
    super(context, attrs);
    // OpenGL 2.0
    setEGLContextClientVersion(2);

    int durationSec = DEFAULT_DURATION_SEC;
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      if ("durationSec".equals(attrs.getAttributeName(i))) {
        durationSec = attrs.getAttributeIntValue(i, DEFAULT_DURATION_SEC);
      }
    }

    this.renderer = new GraphGLRenderer(this.getContext(), durationSec);
    setRenderer(this.renderer);
  }

  /** Connect the view to a viewmodel. */
  public void setTimeSeries(TimeSeries<Double> ts) {
    // Delegate to renderer.
    this.renderer.setTimeSeries(ts);
    ts.addListener(new LiveSeries.Listener<Double>() {
      @Override public void valueAdded(long timestampMicro, Double data) {
        GraphGLView.this.invalidate();
      }
    });
  }
}
