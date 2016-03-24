
package uk.co.bitethebullet.android.token.zxing;

/**
 * &lt;p&gt;Encapsulates the result of a barcode scan invoked through {@link IntentIntegrator}.&lt;/p&gt;
 *
 * @author Sean Owen
 */
public final class IntentResult {

  private final String contents;
  private final String formatName;

  IntentResult(String contents, String formatName) {
    this.contents = contents;
    this.formatName = formatName;
  }

  /**
   * @return raw content of barcode
   */
  public String getContents() {
    return contents;
  }

  /**
   * @return name of format, like "QR_CODE", "UPC_A". See &lt;code&gt;BarcodeFormat&lt;/code&gt; for more format names.
   */
  public String getFormatName() {
    return formatName;
  }

}