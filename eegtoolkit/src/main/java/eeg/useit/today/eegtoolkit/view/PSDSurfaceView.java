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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import eeg.useit.today.eegtoolkit.Constants;
import eeg.useit.today.eegtoolkit.model.EpochCollector;
import eeg.useit.today.eegtoolkit.model.LiveSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import eeg.useit.today.eegtoolkit.model.TimeSeriesSnapshot;

/**
 * SurfaceView that draws multiple epochs for a single timeseries onto one graph.
 */
public class PSDSurfaceView extends SurfaceView {
  private final int psdHistoryLength;

  private Deque<Double[]> psdHistory;

  /** Creates a SurfaceView graph by parsing attributes. */
  public PSDSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
    this.psdHistoryLength = 10;
    this.psdHistory = new ArrayDeque<>(psdHistoryLength);
  }

  public void setPSDLiveSeries(LiveSeries<Double[]> liveSeries) {
    liveSeries.addListener(new LiveSeries.Listener<Double[]>() {
      @Override
      public void valueAdded(long timestampMicro, Double[] data) {
        if (psdHistory.size() == psdHistoryLength) {
          psdHistory.removeFirst();
        }
        psdHistory.addLast(data);
        PSDSurfaceView.this.invalidate();
      }
    });
  }

  @Override
  public void onDraw(Canvas canvas) {
    Paint background = new Paint();
    background.setColor(Color.WHITE);
    background.setStyle(Paint.Style.FILL_AND_STROKE);
    canvas.drawPaint(background);

    if (psdHistory.isEmpty()) {
      return; // Nothing yet, so skip.
    }

    drawLine(canvas, psdHistory.peekLast());
    // TODO: Draw average too.
  }

  private void drawLine(Canvas canvas, Double[] values) {
    // Bounds for the Y axis:
    double yMax = 100;
    double yMin = 0;

    // Build the path by normalizing (time, value) to (real, imag) coordinates.
    Path path = new Path();
    for (int i = 0; i < values.length; i++) {
      double x = i * 1.0 / (values.length - 1);
      double y = (values[i] - yMin) / (yMax - yMin);
      double asX = x * canvas.getWidth();
      double asY = (1.0 - y) * canvas.getHeight();
      if (i == 0) {
        path.moveTo((float) asX, (float) asY);
      } else {
        path.lineTo((float) asX, (float) asY);
      }
    }

    Paint line = new Paint();
    line.setColor(Color.BLACK);
    line.setStyle(Paint.Style.STROKE);
    line.setAntiAlias(true);
    line.setStrokeWidth(2f);
    canvas.drawPath(path, line);
  }
}
