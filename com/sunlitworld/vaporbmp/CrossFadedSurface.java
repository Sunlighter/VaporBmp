package com.sunlitworld.vaporbmp;

public class CrossFadedSurface implements AbstractSurface
{
  public CrossFadedSurface(int size, int elevations, ProgressIndicator pi)
  throws WorkInterruptedException
  { ProgressIndicator s1 = new StepProgressIndicator(pi,1,2);

    this.size = size;

    Surface sf = new Surface(size*2, elevations, s1);
    s1 = new StepProgressIndicator(pi,2,2);
    elevationArray = new float[size * size];
    double qmin = Double.POSITIVE_INFINITY;
    double qmax = Double.NEGATIVE_INFINITY;
    for (int y = 0; y < size; ++y)
    { double yTop = (double)y / (double)size;
      double yBottom = (double)(size - y) / (double)size;
      s1.notify(yTop);
      for (int x = 0; x < size; ++x)
      { double xLeft = (double)x / (double)size;
        double xRight = (double)(size - x) / (double)size;
        double elev = sf.getElevationAt(x,y) * yTop * xLeft
                    + sf.getElevationAt(x+size, y) * yTop * xRight
                    + sf.getElevationAt(x, y+size) * yBottom * xLeft
                    + sf.getElevationAt(x+size, y+size) * yBottom * xRight;
        elevationArray[index(x,y)] = (float)elev;
        if (elev < qmin) qmin = elev;
        if (elev > qmax) qmax = elev;
      }
    }

    sf = null;
    double scale = qmax - qmin;
    for (int i = 0; i < elevationArray.length; ++i)
    { elevationArray[i] = (float)((elevationArray[i] - qmin) / scale);
    }

  }

  private int size;
  private float[] elevationArray;

  private int index(int x, int y)
  { return y * size + x;
  };

  public int size()
  { return size;
  }

  public double getElevationAt(int x, int y)
  { return (double)elevationArray[index(x,y)];
  }

}