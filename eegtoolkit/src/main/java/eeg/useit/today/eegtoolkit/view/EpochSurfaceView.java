package eeg.useit.today.eegtoolkit.view;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.Observable.OnPropertyChangedCallback;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
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

  /** Bounds for min/max voltage in the epoch view. */
  private final double minValue;
  private final double maxValue;

  /** Whether to draw the average epoch as well as the individual ones. */
  private final boolean showAverage;

  /** Source of data for all the epochs, if using a live version. */
  private EpochCollector collector;

  /** Fixed source of epoch data, for when they aren't changing live. */
  private List<Map<String, TimeSeriesSnapshot<Double>>> epochs;

  /** Creates a SurfaceView graph by parsing attributes. */
  public EpochSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);

    int color = Color.BLACK;
    double minValue = Constants.VOLTAGE_MIN;
    double maxValue = Constants.VOLTAGE_MAX;
    boolean showAverage = false;
    for (int i = 0; i < attrs.getAttributeCount(); i++) {
      switch (attrs.getAttributeName(i)) {
        case "lineColor":
          color = Color.parseColor(attrs.getAttributeValue(i));
          break;
        case "minValue":
          minValue = Double.parseDouble(attrs.getAttributeValue(i));
          break;
        case "maxValue":
          maxValue = Double.parseDouble(attrs.getAttributeValue(i));
          break;
        case "showAverage":
          showAverage = Boolean.parseBoolean(attrs.getAttributeValue(i));
          break;
      }
    }
    this.color = color;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.showAverage = showAverage;
  }

  /** Attach a live epoch collector to the view, and repaint whenever it gets new epochs. */
  public void setEpochCollector(EpochCollector collector) {
    if (this.epochs != null) {
      throw new IllegalStateException("Can't have both an epoch collector and fixed epochs.");
    }
    this.collector = collector;
    this.collector.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
        @Override public void onPropertyChanged(Observable sender, int propertyId) {
          EpochSurfaceView.this.invalidate();
        }
      }
    );
    this.invalidate();
  }

  /** Attach a fixed collection of epochs to the view. */
  public void setEpochs(List<Map<String, TimeSeriesSnapshot<Double>>> epochs) {
    if (this.collector != null) {
      throw new IllegalStateException("Can't have both an epoch collector and fixed epochs.");
    }
    this.epochs = epochs;
    this.invalidate();
  }

  @Override
  public void onDraw(Canvas canvas) {
    Paint background = new Paint();
    background.setColor(Color.WHITE);
    background.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPaint(background);

    List<Map<String, TimeSeriesSnapshot<Double>>> epochs = null;
    if (this.epochs != null) {
      epochs = this.epochs;
    } else if (collector != null) {
      epochs = collector.getEpochs();
    }
    if (epochs == null || epochs.isEmpty()) {
      return; // Nothing yet, so skip.
    }

    long maxTimeDelta = -1;
    for (Map<String, TimeSeriesSnapshot<Double>> snapshots : epochs) {
      for (TimeSeriesSnapshot<Double> snapshot : snapshots.values()) {
        if (snapshot.timestamps == null || snapshot.timestamps.length == 0) {
          continue;
        }
        long timeDelta = snapshot.timestamps[snapshot.length - 1] - snapshot.timestamps[0];
        maxTimeDelta = Math.max(maxTimeDelta, timeDelta);
      }
    }
    double timeDeltaInv = 1.0 / (double)maxTimeDelta;

    int allLength = allSnapshotsEqualLength(epochs);
    boolean canShowAverage = this.showAverage && (allLength != -1);
    double[] xSum = canShowAverage ? new double[allLength] : null;
    double[] ySum = canShowAverage ? new double[allLength] : null;
    int nSnapshots = 0;

    // Build the path by normalizing (time, value) to (real, imag) coordinates.
    Path path = new Path();
    for (Map<String, TimeSeriesSnapshot<Double>> snapshots : epochs) {
      for (TimeSeriesSnapshot<Double> snapshot : snapshots.values()) {
        if (snapshot.timestamps == null || snapshot.timestamps.length == 0) {
          continue;
        }
        long timeStart = snapshot.timestamps[0];
        for (int i = 0; i < snapshot.length; i++) {
          double x = (snapshot.timestamps[i] - timeStart) * timeDeltaInv;
          double y = (snapshot.values[i] - this.minValue) / (this.maxValue - this.minValue);
          double asX = x * canvas.getWidth();
          double asY = (1 - y) * canvas.getHeight();
          if (i == 0) {
            path.moveTo((float) asX, (float) asY);
          } else {
            path.lineTo((float) asX, (float) asY);
          }
          if (canShowAverage) {
            xSum[i] += asX;
            ySum[i] += asY;
          }
        }
        nSnapshots++;
      }
    }

    Paint line = new Paint();
    line.setColor(canShowAverage ? Color.GRAY : this.color);
    line.setStyle(Paint.Style.STROKE);
    line.setAntiAlias(true);
    line.setStrokeWidth(2f);
    canvas.drawPath(path, line);

    // Create and draw average path, if we need to:
    if (canShowAverage) {
      Path averagePath = new Path();
      for (int i = 0; i < xSum.length; i++) {
        double x = xSum[i] / nSnapshots;
        double y = ySum[i] / nSnapshots;
        if (i == 0) {
          averagePath.moveTo((float) x, (float) y);
        } else {
          averagePath.lineTo((float) x, (float) y);
        }
      }
      Paint averageLine = new Paint();
      averageLine.setColor(this.color);
      averageLine.setStyle(Paint.Style.STROKE);
      averageLine.setAntiAlias(true);
      averageLine.setStrokeWidth(2f);
      canvas.drawPath(averagePath, averageLine);
    }
  }

  /** @return Common length between all snapshots, or -1 if not all have the same length. */
  private static int allSnapshotsEqualLength(
      List<Map<String, TimeSeriesSnapshot<Double>>> epochList
  ) {
    int snapshotLength = -1;
    for (Map<String, TimeSeriesSnapshot<Double>> snapshots : epochList) {
      for (TimeSeriesSnapshot<Double> snapshot : snapshots.values()) {
        if (snapshotLength == -1) {
          snapshotLength = snapshot.length;
        } else if (snapshot.length != snapshotLength) {
          return -1;
        }
      }
    }
    Log.d("ESV", "TOTAL = " + snapshotLength + " from " + epochList.size() + " epochs");
    return snapshotLength;
  }
}
