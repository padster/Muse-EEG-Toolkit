package eeg.useit.today.eegtoolkit;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Collection of miscellaneous utility functions available within the library.
 */
public class Util {
  /**
   * @return The text contents of the resource file matching the given identity.
   */
  public static String resourceToText(Context ctx, int resId) {
    InputStream is = ctx.getResources().openRawResource(resId);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int len;
    try {
      while ((len = is.read(buf)) != -1) {
        baos.write(buf, 0, len);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load resource");
    }
    return baos.toString();
  }
}
