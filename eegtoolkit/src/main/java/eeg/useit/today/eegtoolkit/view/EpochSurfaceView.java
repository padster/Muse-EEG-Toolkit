package eeg.useit.today.eegtoolkit.view;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.Observable.OnPropertyChangedCallback;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.util.List;
import java.util.Map;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.model.EpochCollector;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * SurfaceView that draws multiple epochs for a single timeseries onto one graph.
 */
public class EpochSurfaceView extends SurfaceView {
  /** Color of line to draw. */
  private final int color;

  /** Source of data for all the epochs. */
  private EpochCollector collector;

  /** Creates a SurfaceView graph by parsing attributes. */
  public EpochSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    int color = Color.BLACK;
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      if ("lineColor".equals(attrs.getAttributeName(i))) {
        color = Color.parseColor(attrs.getAttributeValue(i));
      }
    }
    this.color = color;
  }

  public void setEpochCollector(EpochCollector collector) {
    this.collector = collector;
    this.collector.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
        @Override public void onPropertyChanged(Observable sender, int propertyId) {
          EpochSurfaceView.this.invalidate();
        }
      }
    );
  }

  @Override
  public void onDraw(Canvas canvas) {
    Paint background = new Paint();
    background.setColor(Color.WHITE);
    background.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPaint(background);

    if (collector == null) {
      return; // Nothing yet, so skip.
    }
    List<Map<String, TimeSeriesSnapshot<Double>>> epochs = collector.getEpochs();
    if (epochs.isEmpty()) {
      return; // Nothing yet, so skip.
    }

    long maxTimeDelta = -1;
    for (Map<String, TimeSeriesSnapshot<Double>> snapshots : epochs) {
      for (TimeSeriesSnapshot<Double> snapshot : snapshots.values()) {
        long timeDelta = snapshot.timestamps[snapshot.length - 1] - snapshot.timestamps[0];
        maxTimeDelta = Math.max(maxTimeDelta, timeDelta);
      }
    }
    double timeDeltaInv = 1.0 / (double)maxTimeDelta;

    // Bounds for the Y axis:
    double voltMax = Constants.VOLTAGE_MAX;
    double voltMin = Constants.VOLTAGE_MIN;

    // Build the path by normalizing (time, value) to (x, y) coordinates.
    Path path = new Path();
    for (Map<String, TimeSeriesSnapshot<Double>> snapshots : epochs) {
      for (TimeSeriesSnapshot<Double> snapshot : snapshots.values()) {
        long timeStart = snapshot.timestamps[0];
        for (int i = 0; i < snapshot.length; i++) {
          double x = (snapshot.timestamps[i] - timeStart) * timeDeltaInv;
          double y = (snapshot.values[i] - voltMin) / (voltMax - voltMin);
          double asX = x * canvas.getWidth();
          double asY = (1 - y) * canvas.getHeight();
          if (i == 0) {
            path.moveTo((float) asX, (float) asY);
          } else {
            path.lineTo((float) asX, (float) asY);
          }
        }
      }
    }

    Paint line = new Paint();
    line.setColor(this.color);
    line.setStyle(Paint.Style.STROKE);
    line.setAntiAlias(true);
    line.setStrokeWidth(2f);
    canvas.drawPath(path, line);
  }
}
