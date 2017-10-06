package eeg.useit.today.eegtoolkit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

import eeg.useit.today.eegtoolkit.model.LiveSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * View that draws a short history of dots on a 2d grid, showing two dimensions of values.
 */
public class Plot2DView extends SurfaceView {
  private final static float DOT_RADIUS = 0.05f;

  private TimeSeries<Double[]> timeSeries;

  /** Creates a Plot2D graph by parsing attributes. */
  public Plot2DView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
  }

  /** Connect the view to a viewmodel. */
  public void setTimeSeries(TimeSeries<Double[]> ts) {
    assert this.timeSeries == null;
    this.timeSeries = ts;
    this.timeSeries.addListener(new LiveSeries.Listener<Double[]>() {
      @Override public void valueAdded(long timestampMicro, Double[] data) {
        invalidate();
      }
    });
  }

  @Override
  public void onDraw(Canvas canvas) {
    Paint background = new Paint();
    background.setColor(Color.WHITE);
    background.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPaint(background);

    if (timeSeries == null || timeSeries.isEmpty()) {
      return; // No points, so skip.
    }

    // Calculate bounds for the X axis.
    TimeSeriesSnapshot<Double[]> snapshot = timeSeries.getRecentSnapshot(Double[].class);
    long timeEndMicro = snapshot.timestamps[snapshot.length - 1];
    long timeStartMicro = snapshot.timestamps[0];
    double timeDeltaInv = 1.0 / (double)(timeEndMicro - timeStartMicro);

    for (int i = 0; i < snapshot.length; i++) {
      double x = snapshot.values[i][0]; // Relative Theta
      double y = snapshot.values[i][1]; // Relative Beta
      float fX = (float)(x * canvas.getWidth());
      float fY = (float)((1 - y) * canvas.getHeight());
      float fR = DOT_RADIUS * canvas.getWidth();
      double age = Math.min(1, Math.max(0,
          (timeEndMicro - snapshot.timestamps[i]) * timeDeltaInv));
      canvas.drawCircle(fX, fY, fR, paintForAge(age));
    }
  }

  // Convert age (0 = new, 1 = old) into a paint, using red-green scale.
  private static Paint paintForAge(double age) {
    Paint paint = new Paint();
    int r = (int)(255 * (1 - age));
    int g = (int)(255 * age);
    paint.setARGB(255, r, g, 0);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    return paint;
  }
}
