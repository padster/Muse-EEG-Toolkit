package eeg.useit.today.eegtoolkit.model.fft;

/**
 * FFT transform of a real periodic sequence.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group of University of Lethbridge.
 */
public class FFT extends FFTDouble
{
  /**
   * <em>norm_factor</em> can be used to normalize this FFT transform. This is because
   * a call of forward transform (<em>ft</em>) followed by a call of backward transform
   * (<em>bt</em>) will multiply the input sequence by <em>norm_factor</em>.
   */
  public double norm_factor;
  private double wavetable[];
  private int ndim;

  /**
   * Construct a wavenumber table with size <em>n</em>.
   * The sequences with the same size can share a wavenumber table. The prime
   * factorization of <em>n</em> together with a tabulation of the trigonometric functions
   * are computed and stored.
   *
   * @param  n  the size of a real data sequence. When <em>n</em> is a multiplication of small
   * numbers (4, 2, 3, 5), this FFT transform is very efficient.
   */
  public FFT(int n)
  {
    ndim = n;
    norm_factor = n;
    if(wavetable == null || wavetable.length !=(2*ndim+15))
    {
      wavetable = new double[2*ndim + 15];
    }
    rffti(ndim, wavetable);
  }

  /**
   * Forward real FFT transform. It computes the discrete transform of a real data sequence.
   *
   * @param x an array which contains the sequence to be transformed. After FFT,
   * <em>real</em> contains the transform coeffients used to construct <em>n</em> complex FFT coeffients.
   * <br>
   * The remaining complex FFT coeffients can be obtained by the symmetry relation:
   * the (<em>n</em>-<em>k</em>)-th complex FFT coeffient is the conjugate of <em>n</em>-th complex FFT coeffient.
   *
   */
  public Complex1D ft(double x[])
  {
    Complex1D result = new Complex1D();
    if(x.length != ndim)
      throw new IllegalArgumentException("The length of data can not match that of the wavetable");
    rfftf(ndim, x, wavetable);

    if(ndim%2 == 0)
    {
      result.real = new double[ndim/2 + 1];
      result.imag = new double[ndim/2 + 1];
    }
    else
    {
      result.real = new double[(ndim+1)/2];
      result.imag = new double[(ndim+1)/2];
    }


    result.real[0] = x[0];
    result.imag[0] = 0.0D;
    for(int i=1; i<(ndim+1)/2; i++)
    {
      result.real[i] = x[2*i-1];
      result.imag[i] = x[2*i];
    }
    if(ndim%2 == 0)
    {
      result.real[ndim/2] = x[ndim-1];
      result.imag[ndim/2] = 0.0D;
    }
    return result;
  }
}
