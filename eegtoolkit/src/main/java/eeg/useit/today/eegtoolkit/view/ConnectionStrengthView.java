package eeg.useit.today.eegtoolkit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

import eeg.useit.today.eegtoolkit.model.LiveSeries;
import eeg.useit.today.eegtoolkit.vm.ConnectionStrengthViewModel;

/** View that draws the HSI connection strength to four circles. */
public class ConnectionStrengthView extends SurfaceView {
  private final static float PADDING = 0.8f;

  /** ViewModel backing this view, set once. */
  private ConnectionStrengthViewModel liveStrength = null;

  /** Creates an HSI view by parsing attributes. */
  public ConnectionStrengthView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
  }

  /** Connect the view to a viewmodel. */
  public void setLiveStrength(ConnectionStrengthViewModel liveStrength) {
    assert this.liveStrength == null;
    this.liveStrength = liveStrength;
    this.liveStrength.addListener(new LiveSeries.Listener<Double[]>() {
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

    if (this.liveStrength == null) {
      // Nothing to draw yet...
      return;
    }

    float midX = canvas.getWidth() / 2f;
    float midY = canvas.getHeight() / 2f;
    float rad = Math.min(midX / 4f, midY);
    for (int i = 0; i < 4; i++) {
      float atX = midX + (i * 2 - 3) * rad; // i = [0, 1, 2, 3] -> [-3, -1, 1, 3]
      Paint paint = paintForStrength(liveStrength.getChannelStatus(i));
      canvas.drawCircle(atX, midY, rad * PADDING, paint);
    }
  }

  // Convert strength (0 = bad, 0.5 = ok, 1 = good) into a paint, using red-green scale.
  private static Paint paintForStrength(double strength) {
    Paint paint = new Paint();
    int r = (int)(255 * (1 - strength));
    int g = (int)(255 * strength);
    paint.setARGB(255, r, g, 0);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    return paint;
  }
}
