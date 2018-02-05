package eeg.useit.today.eegtoolkit.view.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.model.LiveSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * SurfaceView that draws a timeseries line to the view.
 */
public class GraphSurfaceView extends SurfaceView {
  private static final int DEFAULT_DURATION_SEC = 10;

  /** Duration of history of the time series to show. */
  private final int durationSec;

  /** Color of line to draw. */
  private final int color;

  /** Source of timeseries data to draw. Set when view is attached. */
  private TimeSeries<Double> timeSeries;

  /** Creates a SurfaceView graph by parsing attributes. */
  public GraphSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    int color = Color.WHITE;
    int durationSec = DEFAULT_DURATION_SEC;
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      if ("lineColor".equals(attrs.getAttributeName(i))) {
        color = Color.parseColor(attrs.getAttributeValue(i));
      } else if ("durationSec".equals(attrs.getAttributeName(i))) {
        durationSec = attrs.getAttributeIntValue(i, DEFAULT_DURATION_SEC);
      }
    }
    this.color = color;
    this.durationSec = durationSec;
  }

  /** Connect the view to a viewmodel. */
  public void setTimeSeries(TimeSeries<Double> ts) {
    assert this.timeSeries == null;
    this.timeSeries = ts;
    ts.addListener(new LiveSeries.Listener<Double>() {
      @Override public void valueAdded(long timestampMicro, Double data) {
        GraphSurfaceView.this.invalidate();
      }
    });
  }

  @Override
  public void onDraw(Canvas canvas) {
    Paint background = new Paint();
    background.setColor(Color.BLACK);
    background.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPaint(background);

    if (timeSeries == null || timeSeries.isEmpty()) {
      return; // No points, so skip.
    }

    // Calculate bounds for the X axis.
    TimeSeriesSnapshot<Double> snapshot = timeSeries.getRecentSnapshot(Double.class);
    long timeEndMicro = snapshot.timestamps[snapshot.length - 1];
    long timeDelta = this.durationSec * 1000000L; // POIUY
    long timeStartMicro = timeEndMicro - timeDelta; // HACK
    double timeDeltaInv = 1.0 / (double)timeDelta;

    // Bounds for the Y axis:
    double voltMax = Constants.VOLTAGE_MAX;
    double voltMin = Constants.VOLTAGE_MIN;

    // Build the path by normalizing (time, value) to (real, imag) coordinates.
    Path path = new Path();
    for (int i = 0; i < snapshot.length; i++) {
      double x = (snapshot.timestamps[i] - timeStartMicro) * timeDeltaInv;
      double y = (snapshot.values[i] - voltMin) / (voltMax - voltMin);
      double asX = x * canvas.getWidth();
      double asY = (1 - y) * canvas.getHeight();
      if (i == 0) {
        path.moveTo((float) asX, (float)asY);
      } else {
        path.lineTo((float) asX, (float)asY);
      }
    }

    Paint dots = new Paint();
    dots.setColor(this.color);
    dots.setStyle(Paint.Style.STROKE);
    dots.setAntiAlias(true);
    canvas.drawPath(path, dots);
  }
}
