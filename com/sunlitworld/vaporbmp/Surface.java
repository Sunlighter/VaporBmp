package com.sunlitworld.vaporbmp;

public class Surface implements AbstractSurface
{
  private static double interpolate(double x1, double y1, double x2, double y2, double x3)
  { double scale = (x3-x1)/(x2-x1);
    return y1 + (y2-y1)*scale;
  }

  private interface InnerSurface
  { boolean contains(int x, int y);
    short getElevationAt(int x, int y);
    short minElevation();
    short maxElevation();
    void elevate(ClipLine cl);
  }

  private static final int AREA_LIMIT = 128;

  private static InnerSurface createSurface(IntRectangle ir)
  { if (ir.area() > AREA_LIMIT)
    { if (ir.width() > ir.height()) return new SplitSurface(ir, true);
      else return new SplitSurface(ir, false);
    } else return new SimpleSurface(ir);
  }

  private static final class SimpleSurface implements InnerSurface
  { public SimpleSurface(IntRectangle ir)
    { this.ir = ir;
      r = ir.asRectangle();
      height = new short[ir.area()];
    }

    private IntRectangle ir;
    private Rectangle r;
    private short[] height;
    private short heightOffset;

    public boolean contains(int x, int y)
    { return ir.contains(x,y);
    }

    public short getElevationAt(int x, int y)
    { if (ir.contains(x,y))
      { return (short)(height[ir.index(x,y)] + heightOffset);
      } else return 1000;
    }

    public short minElevation()
    { short min = java.lang.Short.MAX_VALUE;
      for (int i = 0; i < height.length; ++i)
      { if (height[i] < min) min = height[i];
      }
      return (short)(min + heightOffset);
    }

    public short maxElevation()
    { short max = java.lang.Short.MIN_VALUE;
      for (int i = 0; i < height.length; ++i)
      { if (height[i] > max) max = height[i];
      }
      return (short)(max + heightOffset);
    }

    private void elevateRangeOnLine(int x1, int x2, int y, short changeToMake)
    { try
      { for (int x = x1; x < x2; ++x)
        { if (!ir.contains(x,y))
          throw new ArrayIndexOutOfBoundsException
          ( "x="+x+", y="+y+" not in "+ir
          );
          else
          height[ir.index(x,y)] += changeToMake;
        }
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      { System.err.println("Error: x1="+x1+", x2="+x2+", y="+y);
        throw aioobe;
      }
    }

    private static int icpt(ClipLine cl, int y)
    { Vector2 pdirec = cl.direc.r90ccw();
      return (int)interpolate
      ( cl.point.y, cl.point.x,
        cl.point.y + pdirec.y, cl.point.x + pdirec.x,
        (double)y
      );
    }

    private void elevateLine(ClipLine cl, int y, short changeToMake)
    { int left = ir.left();
      int right = ir.right();
      Vector2 begin = new Vector2((double)left, (double)y);
      Vector2 end = new Vector2((double)right, (double)y);
      try
      { if (cl.includes(begin))
        { if (cl.includes(end)) elevateRangeOnLine(left, right, y, changeToMake);
          else elevateRangeOnLine(left, icpt(cl,y), y, changeToMake);
        } else
        { if (cl.includes(end)) elevateRangeOnLine(icpt(cl,y), right, y, changeToMake);
          else /* do nothing */;
        } 
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      { System.err.println("Error: cl="+cl+", y="+y);
        throw aioobe;
      }
    }

    private void elevateLines(ClipLine cl, short changeToMake)
    { int bottom = ir.bottom();
      int top = ir.top();
      try
      { for (int y = top; y < bottom; ++y)
        { elevateLine(cl, y, changeToMake);
        }
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      { System.out.println(aioobe);
      }
    }

    public void elevate(ClipLine cl)
    { int clipStatus = r.clipStatus(cl);
      switch(clipStatus)
      { case Rectangle.ENTIRELY_INSIDE:
        { ++heightOffset;
        } break;
        case Rectangle.ENTIRELY_OUTSIDE:
        { // do nothing
        } break;
        case Rectangle.MOSTLY_INSIDE:
        { ++heightOffset;
          elevateLines(cl.flip(), (short)-1);
        } break;
        case Rectangle.MOSTLY_OUTSIDE:
        { elevateLines(cl, (short)1);
        } break;
      }
    }
  }

  private static final class SplitSurface implements InnerSurface
  { public SplitSurface(IntRectangle ir, boolean splitHorizontally)
    { this.splitHorizontally = splitHorizontally;
      this.ir = ir;
      r = ir.asRectangle();
      if (splitHorizontally)
      { firstHalf = createSurface(ir.leftHalf());
        secondHalf = createSurface(ir.rightHalf());
      } else
      { firstHalf = createSurface(ir.topHalf());
        secondHalf = createSurface(ir.bottomHalf());
      }
    }

    private boolean splitHorizontally;

    private IntRectangle ir;
    private Rectangle r;

    private InnerSurface firstHalf;
    private InnerSurface secondHalf;

    private short heightOffset;

    public boolean contains(int x, int y)
    { return ir.contains(x,y);
    }

    public short getElevationAt(int x, int y)
    { if (firstHalf.contains(x,y))
      return (short)(firstHalf.getElevationAt(x,y) + heightOffset);
      else if (secondHalf.contains(x,y))
      return (short)(secondHalf.getElevationAt(x,y) + heightOffset);
      else throw new UnknownError("Point out of range");
    }

    public short minElevation()
    { short a = firstHalf.minElevation();
      short b = secondHalf.minElevation();
      short min = (a<b)?a:b;
      return (short)(min + heightOffset);
    }

    public short maxElevation()
    { short a = firstHalf.maxElevation();
      short b = secondHalf.maxElevation();
      short max = (a>b)?a:b;
      return (short)(max + heightOffset);
    }

    public void elevate(ClipLine cl)
    { int clipStatus = r.clipStatus(cl);
      switch(clipStatus)
      { case Rectangle.ENTIRELY_INSIDE:
        { ++heightOffset;
        } break;
        case Rectangle.ENTIRELY_OUTSIDE:
        { // do nothing
        } break;
        case Rectangle.MOSTLY_OUTSIDE:
        case Rectangle.MOSTLY_INSIDE:
        { firstHalf.elevate(cl);
          secondHalf.elevate(cl);
        }
      }
    }
  }

  private InnerSurface surf;

  private short min;
  private short max;
  private int size;
  private double dmin;
  private double drange;

  public Surface(int size, int elevations, ProgressIndicator pi) throws WorkInterruptedException
  { this.size = size;
    IntRectangle ir = new IntRectangle(0, 0, size, size);
    Rectangle r = ir.asRectangle();
    surf = createSurface(ir);
    for (int i = 0; i < elevations; ++i)
    { if ((i & 7) == 0) pi.notify((double)i / (double)elevations);
      surf.elevate(r.getRandomClipLine());
    }
    min = surf.minElevation();
    max = surf.maxElevation();
    System.out.println("min: "+min+", max: "+max);
    dmin = (double)min;
    drange = (double)(max-min);
    if (min == max) drange = 1.0;
  }

  public int granularity()
  { return max - min + 1;
  }

  public int size()
  { return size;
  }

  public double getElevationAt(int x, int y)
  { double d = (double)surf.getElevationAt(x,y);
    return (d - dmin) / drange;
  }
};