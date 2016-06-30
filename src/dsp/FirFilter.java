package dsp;

public class FirFilter {
    
    int m_numCoeffs;
    double[] m_coeffs;
    double[] m_delayLine;
    
    /** Creates a new instance of FIRFilter */
    public FirFilter()
    {
    }
    
    /*---------------------------------------------------------------*/
    /* Design FIR filter using window method. Hamming window is used */
    /* If sucess, return a point to the filter coefficient array,    */
    /* otherwise, return NULL. Calling program should release the    */
    /* allocated memory in this subroutine                           */
    /*                                                               */
    /*                                                               */
    /*  Suppose sampling rate is 2 Hz                                */
    /*                                                               */
    /*  Len : filter length, should be ODD and Len>=3                */
    /*  cutLow : low cutoff, when lowpass, cutLow = 0.0              */
    /*  cutHigh: high cutoff, when highpass, cutHigh = 1.0           */
    /*  when bandpass,    0.0 < cutLow < cutHigh < 1.0               */
    /*                                                               */
    /*  example:                                                     */
    /*      m_coeffs = fir_dsgn(127, 0.3, 0.8);                          */
    /*   return a bandpass filter                                    */
    /*---------------------------------------------------------------*/
    public int setFilter(int len,double cutLow, double cutHigh)
    {
	double Sum, TmpFloat;
        int CoefNum, HalfLen, Cnt,k;

       /*---------------------------------------------*/
       /* adjust the number of coefficients to be ODD */
       /*---------------------------------------------*/
       CoefNum = len;

       if (len % 2 == 0)
               CoefNum++;

       HalfLen = (CoefNum - 1) / 2;

       //--------------------------------------------------------
       // Allocate memory for coefficients if length changed
       //--------------------------------------------------------

       if(m_numCoeffs!=CoefNum)
       {
            m_coeffs = new double[CoefNum];
            m_delayLine = new double[CoefNum];

            for(k=0; k<CoefNum; k++)
                    m_delayLine[k] = 0.0f;

            m_numCoeffs=CoefNum;
       }

        /*------------------*/
        /*  Lowpass filter  */
        /*------------------*/
        if ((cutLow == 0.0) && (cutHigh < 1.0))
        {
            m_coeffs[HalfLen] = cutHigh;

            for (Cnt=1; Cnt<=HalfLen; Cnt++)
            {
                    TmpFloat = Math.PI * Cnt;
                    m_coeffs[HalfLen + Cnt] = Math.sin(cutHigh * TmpFloat) / TmpFloat;
                    m_coeffs[HalfLen - Cnt] = m_coeffs[HalfLen + Cnt];
            }

            /*------------------------------*/
            /* multiplying with a window    */
            /*------------------------------*/
            TmpFloat = 2.0 * Math.PI / (CoefNum - 1.0);
            Sum = 0.0;

            for (Cnt=0; Cnt<CoefNum; Cnt++)
            {
                m_coeffs[Cnt] *= (0.54 - 0.46 * Math.cos(TmpFloat * Cnt));
                Sum += m_coeffs[Cnt];
            }


         // m_coeffs[HalfLen] += 1;
         // Sum += 1;

          /*------------------------------*/
          /* Normalize GAIN to 1          */
          /*------------------------------*/
        for (Cnt=0; Cnt<CoefNum; Cnt++)
        {
                m_coeffs[Cnt] /= Math.abs (Sum);
        }
        return (m_numCoeffs);

    }  /* if Lowpass */

       /*------------------*/
       /* Highpass filter  */
       /*------------------*/
       if ((cutLow > 0.0) && (cutHigh == 1.0))
       {

          m_coeffs[HalfLen] = cutLow;
          for (Cnt=1; Cnt<=HalfLen; Cnt++)
              {
                    TmpFloat = Math.PI * Cnt;
                    m_coeffs[HalfLen + Cnt] = Math.sin(cutLow * TmpFloat) / TmpFloat;
                    m_coeffs[HalfLen - Cnt] = m_coeffs[HalfLen + Cnt];
          }

          /*------------------------------*/
          /* multiplying with a window    */
          /*------------------------------*/
          TmpFloat = 2.0 * Math.PI / (CoefNum - 1.0);
          Sum = 0.0;
          for (Cnt=0; Cnt<CoefNum; Cnt++)
          {
                    m_coeffs[Cnt] *= -(0.54 - 0.46 * Math.cos(TmpFloat * Cnt));
                    if (Cnt % 2 == 0) Sum += m_coeffs[Cnt];  /* poly(-1), even +, odd -*/
                    else Sum -= m_coeffs[Cnt] ;
          }

          m_coeffs[HalfLen] += 1;
          Sum += 1;

          /*------------------------------*/
          /* Normalize GAIN to 1          */
          /*------------------------------*/
          for (Cnt=0; Cnt<CoefNum; Cnt++)
              {
                      m_coeffs[Cnt] /= Math.abs (Sum);
              } 	
          return (m_numCoeffs);

       } /* if HighPass */


       /*------------------*/
       /* Bandpass filter  */
       /*------------------*/
       if ((cutLow > 0.0) && (cutHigh < 1.0) && (cutLow < cutHigh)) 
       {

          m_coeffs[HalfLen] = cutHigh - cutLow;
          for (Cnt=1; Cnt<=HalfLen; Cnt++)
              {
                    TmpFloat = Math.PI * Cnt;
                    m_coeffs[HalfLen + Cnt] = 2.0 * Math.sin( (cutHigh - cutLow) / 2.0 * TmpFloat) *
                                  Math.cos( (cutHigh + cutLow) / 2.0 * TmpFloat) / TmpFloat;
                    m_coeffs[HalfLen - Cnt] = m_coeffs[HalfLen + Cnt];
          }

          /*------------------------------*/
          /* multiplying with a window    */
          /*------------------------------*/
          TmpFloat = 2.0 * Math.PI / (CoefNum - 1.0);
          Sum = 0.0;
          for (Cnt=0; Cnt<CoefNum; Cnt++)
              {
                    m_coeffs[Cnt] *= (0.54 - 0.46 * Math.cos(TmpFloat * Cnt));
                    Sum += m_coeffs[Cnt];
          }


    //      m_coeffs[HalfLen] += 1;
    //      Sum += 1;

          /*------------------------------*/
          /* Normalize GAIN to 1          */
          /*------------------------------*/
          for (Cnt=0; Cnt<CoefNum; Cnt++)
              {
                      m_coeffs[Cnt] /= Math.abs (Sum);
              }

          return (m_numCoeffs);

       } /* if */

       /*------------------*/
       /* Bandstop filter  */
       /*------------------*/
       if ((cutLow > 0.0) && (cutHigh < 1.0) && (cutLow>cutHigh))
       {

          m_coeffs[HalfLen] = cutLow - cutHigh;
          for (Cnt=1; Cnt<=HalfLen; Cnt++)
              {
                    TmpFloat = Math.PI * Cnt;
                    m_coeffs[HalfLen + Cnt] = 2.0 * Math.sin( (cutLow - cutHigh) / 2.0 * TmpFloat) *
                                  Math.cos( (cutHigh + cutLow) / 2.0 * TmpFloat) / TmpFloat;
                    m_coeffs[HalfLen - Cnt] = m_coeffs[HalfLen + Cnt];
          }

          /*------------------------------*/
          /* multiplying with a window    */
          /*------------------------------*/
          TmpFloat = 2.0 * Math.PI / (CoefNum - 1.0);
          Sum = 0.0;
          for (Cnt=0; Cnt<CoefNum; Cnt++)
              {
                    m_coeffs[Cnt] *= -(0.54 - 0.46 * Math.cos(TmpFloat * Cnt));
                    Sum += m_coeffs[Cnt];
          }

          m_coeffs[HalfLen] += 1;
          Sum += 1;

          /*------------------------------*/
          /* Normalize GAIN to 1          */
          /*------------------------------*/
          for (Cnt=0; Cnt<CoefNum; Cnt++)
              {
                      m_coeffs[Cnt] /= Math.abs (Sum);
              }

              return (m_numCoeffs);

       }  /* if */

       return (m_numCoeffs);    /* never reach here */
    }

    // filter single sample
    public double filter(double io)
    {
        int k;
	double accumulator;
        
        // add new sample to start of delay line
        m_delayLine[0] = io;
        accumulator = 0.0f;

        // apply coefficients to data
        for(k=0; k<m_numCoeffs; k++)
                accumulator += m_coeffs[k]*m_delayLine[k];

        // update output
        io = accumulator;

        // rotate delay line
        for(k=m_numCoeffs-1; k>0; k--)
                m_delayLine[k] = m_delayLine[k-1];
        
        return io;///m_numCoeffs;
    }
    
    // filter sample buffer
    public double[] filter(double[] io)
    {
	int i;

	for(i=0; i<io.length; i++)
	{
		// update output
		io[i] = filter(io[i]);
        }
        return(io);
    }

    // filter sample buffer
    public byte[] filter(byte[] io)
    {
	int i;

	for(i=0; i<io.length; i++)
	{
		// update output
                double value = (double)io[i]/255.0;
                double result = filter(value);
                
		io[i] = (byte)(result*255);
        }
        return(io);
    }

}
