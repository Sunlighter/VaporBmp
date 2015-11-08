package com.sunlitworld.vaporbmp;

public class Vapor
{
  private static ProgressIndicator step(ProgressIndicator basic, int s, int outof)
  { if (outof != 1)
    { return new TimeBasedProgressThinner
      ( new StepProgressIndicator(basic,s,outof), 1000L
      );
    }
    else
    { return new TimeBasedProgressThinner(basic, 1000L);
    }
  }

  private static class SurfaceParams
  { public int size;
    public int iters;
    public boolean folded;
    public ProgressIndicator pi;
  }

  private static AbstractSurface makeSurface
  ( SurfaceParams sp, int mystep, int outof
  ) throws WorkInterruptedException
  { if (sp.folded)
    { return new CrossFadedSurface(sp.size, sp.iters, step(sp.pi, mystep, outof));
    }
    else
    { return new Surface(sp.size, sp.iters, step(sp.pi, mystep, outof));
    }
  };

  private interface Renderer
  { Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException;
  };

  private interface BrightnessCurve
  { double curve(double x);
  }

  private static class SharpBoost implements BrightnessCurve
  {
    public SharpBoost(double x1, double y1)
    { this.x1 = x1;
      this.y1 = y1;
      m1 = (y1 / x1);
      m2 = (1. - y1)/(1. - x1);
      b2 = y1 + (1. - y1) * -x1 / (1. - x1);
    }

    private double x1;
    private double y1;
    private double m1;
    private double m2;
    private double b2;

    public double curve(double x)
    { if (x < x1) return (x * m1);
      else return (x * m2) + b2;
    }
  }

  private static class RangeLimit implements BrightnessCurve
  {
    public RangeLimit(double min, double max)
    { start = min;
      range = max - min;
    }

    private double start;
    private double range;

    public double curve(double x)
    { return (x * range) + start;
    }
  }

  private static class Sawtooth implements BrightnessCurve
  {
    public Sawtooth(double iters)
    { this.iters = iters;
    }

    private double iters;

    public double curve(double x)
    { double r = x * iters;
      return r - java.lang.Math.floor(r);
    }
  }

  private static class Triangle implements BrightnessCurve
  {
    public Triangle(double iters)
    { this.iters = iters;
    }

    private double iters;

    public double curve(double x)
    { double r = x * iters;
      double s = java.lang.Math.floor(r);
      double t = r - s;
      if (((int)(s+0.1) & 1) != 0)
        t = 1. - t;
      return t;
    }
  }

  private static class Composite implements BrightnessCurve
  {
    public Composite(BrightnessCurve first, BrightnessCurve second)
    { this.a = second;
      this.b = first;
    }

    private BrightnessCurve a;
    private BrightnessCurve b;

    public double curve(double x)
    { return a.curve(b.curve(x));
    }
  }

  private static class TrichromaticRenderer implements Renderer
  { 
    public TrichromaticRenderer(BrightnessCurve br)
    { this.br = br;
    }

    private BrightnessCurve br;

    public TrichromaticRenderer()
    { br = null;
    }

    public Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException
    { AbstractSurface sr = makeSurface(sp, 1, 3);
      AbstractSurface sg = makeSurface(sp, 2, 3);
      AbstractSurface sb = makeSurface(sp, 3, 3);
      Dib24Bit dib = new Dib24Bit(sp.size, sp.size);
      for (int x = 0; x < sp.size; ++x)
      { for (int y = 0; y < sp.size; ++y)
        { double rr = sr.getElevationAt(x,y);
          double gg = sg.getElevationAt(x,y);
          double bb = sb.getElevationAt(x,y);
          if (br != null)
          { rr = br.curve(rr);
            gg = br.curve(gg);
            bb = br.curve(bb);
          }
          int r = (int)(0.5 + (255. * rr));
          int g = (int)(0.5 + (255. * gg));
          int b = (int)(0.5 + (255. * bb));
          int c = ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
          dib.putPixel(x,y,c);
        }
      }
      return dib;
    }
  };

  private static class AltTrichromaticRenderer implements Renderer
  { public AltTrichromaticRenderer(BrightnessCurve br)
    { this.br = br;
    }

    public AltTrichromaticRenderer()
    { br = null;
    }

    private BrightnessCurve br;

    public Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException
    { AbstractSurface sc = makeSurface(sp, 1, 3);
      AbstractSurface sm = makeSurface(sp, 2, 3);
      AbstractSurface sy = makeSurface(sp, 3, 3);
      Dib24Bit dib = new Dib24Bit(sp.size, sp.size);
      for (int x = 0; x < sp.size; ++x)
      { for (int y = 0; y < sp.size; ++y)
        { double cc = sc.getElevationAt(x,y);
          double mm = sm.getElevationAt(x,y);
          double yy = sy.getElevationAt(x,y);
          if (br != null)
          { cc = br.curve(cc);
            mm = br.curve(mm);
            yy = br.curve(yy);
          }
          double rr = mm * 0.5 + yy * 0.5;
          double gg = yy * 0.5 + cc * 0.5;
          double bb = cc * 0.5 + mm * 0.5;
          int r = (int)(0.5 + (255. * rr));
          int g = (int)(0.5 + (255. * gg));
          int b = (int)(0.5 + (255. * bb));
          int c = ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
          dib.putPixel(x,y,c);
        }
      }
      return dib;
    }
  };

  private static enum DichromaticStyle
  { RC   (1.0, 0.0, 0.0),
    RYCB (1.0, 0.5, 0.0),
    YB   (1.0, 1.0, 0.0),
    YGMB (0.5, 1.0, 0.0),
    GM   (0.0, 1.0, 0.0),
    CGMR (0.0, 1.0, 0.5);

    DichromaticStyle(double ri, double gi, double bi)
    { this.ri = ri; this.gi = gi; this.bi = bi;
    }

    private double ri;
    private double gi;
    private double bi;

    public double GetRI() { return ri; }
    public double GetGI() { return gi; }
    public double GetBI() { return bi; }
  }

  private static class DichromaticRenderer implements Renderer
  { public DichromaticRenderer(DichromaticStyle ds, BrightnessCurve br)
    { this.ri = ds.GetRI();
      this.gi = ds.GetGI();
      this.bi = ds.GetBI();
      this.br = br;
    }

    public DichromaticRenderer(DichromaticStyle ds)
    { this.ri = ds.GetRI();
      this.gi = ds.GetGI();
      this.bi = ds.GetBI();
      br = null;
    }

    private double ri;
    private double gi;
    private double bi;

    private BrightnessCurve br;

    public Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException
    { AbstractSurface s1 = makeSurface(sp, 1, 2);
      AbstractSurface s2 = makeSurface(sp, 2, 2);
      Dib24Bit dib = new Dib24Bit(sp.size, sp.size);
      for (int x = 0; x < sp.size; ++x)
      { for (int y = 0; y < sp.size; ++y)
        { double xx = s1.getElevationAt(x,y);
          double yy = s2.getElevationAt(x,y);
          if (br != null)
          { xx = br.curve(xx);
            yy = br.curve(yy);
          }
          double rr = xx * ri + yy * (1. - ri);
          double gg = xx * gi + yy * (1. - gi);
          double bb = xx * bi + yy * (1. - bi);
          int r = (int)(0.5 + (255. * rr));
          int g = (int)(0.5 + (255. * gg));
          int b = (int)(0.5 + (255. * bb));
          int c = ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
          dib.putPixel(x,y,c);
        }
      }
      return dib;
    }
  };

  private static enum AltDichromaticStyle
  { RED_BLUE, RED_GREEN, BLUE_GREEN
  }

  private static class AltDichromaticRenderer implements Renderer
  { public AltDichromaticRenderer(AltDichromaticStyle ads, BrightnessCurve br)
    { this.ads = ads;
      this.br = br;
    }

    public AltDichromaticRenderer(AltDichromaticStyle ads)
    { this.ads = ads;
      br = null;
    }

    private AltDichromaticStyle ads;
    private BrightnessCurve br;

    public Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException
    { AbstractSurface s1 = makeSurface(sp, 1, 2);
      AbstractSurface s2 = makeSurface(sp, 2, 2);
      Dib24Bit dib = new Dib24Bit(sp.size, sp.size);
      for (int x = 0; x < sp.size; ++x)
      { for (int y = 0; y < sp.size; ++y)
        { double xx = s1.getElevationAt(x,y);
          double yy = s2.getElevationAt(x,y);
          if (br != null)
          { xx = br.curve(xx);
            yy = br.curve(yy);
          }
          double rr, gg, bb;
          switch(ads)
          { case RED_BLUE:
              rr = xx; gg = java.lang.Math.min(xx,yy); bb = yy; break;
            case RED_GREEN:
              rr = xx; gg = yy; bb = java.lang.Math.min(xx,yy); break;
            case BLUE_GREEN: default:
              rr = java.lang.Math.min(xx,yy); gg = xx; bb = yy; break;
          }
          int r = (int)(0.5 + (255. * rr));
          int g = (int)(0.5 + (255. * gg));
          int b = (int)(0.5 + (255. * bb));
          int c = ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
          dib.putPixel(x,y,c);
        }
      }
      return dib;
    }
  };

  private static class MonochromaticRenderer implements Renderer
  { public MonochromaticRenderer(BrightnessCurve br)
    { this.br = br;
    }

    public MonochromaticRenderer() { br = null; }

    private BrightnessCurve br;

    public Dib24Bit render(SurfaceParams sp)
    throws WorkInterruptedException
    { AbstractSurface s = makeSurface(sp, 1, 1);
      Dib24Bit dib = new Dib24Bit(sp.size, sp.size);
      for (int x = 0; x < sp.size; ++x)
      { for (int y = 0; y < sp.size; ++y)
        { double rr = s.getElevationAt(x,y);
          if (br != null) rr = br.curve(rr);
          int r = (int)(0.5 + (255. * rr));
          int c = ((r & 255) << 16) | ((r & 255) << 8) | (r & 255);
          dib.putPixel(x,y,c);
        }
      }
      return dib;
    }
  };

  static java.util.Map<String, Renderer> renderMap;

  private static void add1(String name, Renderer value)
  { renderMap.put(name, value);
  }

  private static void add2(String name1, String name2, Renderer value)
  { renderMap.put(name1, value);
    renderMap.put(name2, value);
  }

  static
  { renderMap = new java.util.HashMap<String, Renderer>();
    BrightnessCurve x = new Composite(new Triangle(6.), new SharpBoost(0.8, 0.5));
    add1("-rgb", new TrichromaticRenderer());
    add1("-rgblite", new TrichromaticRenderer(new RangeLimit(0.5, 1.0)));
    add1("-rgbdark", new TrichromaticRenderer(new RangeLimit(0.0, 0.5)));
    add1("-rgbx", new TrichromaticRenderer(x));
    add2("-rc", "-cr", new DichromaticRenderer(DichromaticStyle.RC));
    add2("-rcx", "-crx", new DichromaticRenderer(DichromaticStyle.RC, x));
    add2("-yrcb", "-cbyr", new DichromaticRenderer(DichromaticStyle.RYCB));
    add2("-yrcbx", "-cbyrx", new DichromaticRenderer(DichromaticStyle.RYCB, x));
    add2("-yb", "-by", new DichromaticRenderer(DichromaticStyle.YB));
    add2("-ybx", "-byx", new DichromaticRenderer(DichromaticStyle.YB, x));
    add2("-ygmb", "-mbyg", new DichromaticRenderer(DichromaticStyle.YGMB));
    add2("-ygmbx", "-mbygx", new DichromaticRenderer(DichromaticStyle.YGMB, x));
    add2("-gm", "-mg", new DichromaticRenderer(DichromaticStyle.GM));
    add2("-gmx", "-mgx", new DichromaticRenderer(DichromaticStyle.GM, x));
    add2("-cgmr", "-mrcg", new DichromaticRenderer(DichromaticStyle.CGMR));
    add2("-cgmrx", "-mrcgx", new DichromaticRenderer(DichromaticStyle.CGMR, x));
    add1("-gray", new MonochromaticRenderer());
    add1("-grayx", new MonochromaticRenderer(x));
    add1("-cmy", new AltTrichromaticRenderer());
    add1("-cmyx", new AltTrichromaticRenderer(x));
    add1("-usa", new AltDichromaticRenderer(AltDichromaticStyle.RED_BLUE));
    add1("-xmas", new AltDichromaticRenderer(AltDichromaticStyle.RED_GREEN));
    add1("-ocean", new AltDichromaticRenderer(AltDichromaticStyle.BLUE_GREEN));
    add1("-usax", new AltDichromaticRenderer(AltDichromaticStyle.RED_BLUE, x));
    add1("-xmasx", new AltDichromaticRenderer(AltDichromaticStyle.RED_GREEN, x));
    add1("-oceanx", new AltDichromaticRenderer(AltDichromaticStyle.BLUE_GREEN, x));
  }
    
  private static class ArgWalker
  { public ArgWalker(String[] args)
    { this.args = args;
      this.pos = 0;
    }

    public boolean at_end()
    { return pos == args.length;
    }

    public String peek()
    { return args[pos];
    }

    public String get()
    { String x = args[pos];
      ++pos;
      return x;
    }

    private int pos;
    private String[] args;
  };

  public static void main(String[] args)
  {
    ProgressIndicator basic = new ConsoleProgressIndicator();

    SurfaceParams sp = new SurfaceParams();
    sp.size = 2500;
    sp.iters = 0x6000;
    sp.folded = true;
    sp.pi = basic;

    String outfile = null;

    Renderer r = null;

    ArgWalker a = new ArgWalker(args);
    while (!a.at_end())
    { if (a.peek().equals("-tile"))
      { a.get(); sp.folded = false;
      }
      else if (a.peek().equals("-outfile"))
      { a.get(); outfile = a.get();
      }
      else if (a.peek().equals("-size"))
      { a.get(); sp.size = Integer.decode(a.get()).intValue();
      }
      else if (a.peek().equals("-iters"))
      { a.get(); sp.iters = Integer.decode(a.get()).intValue();
      }
      else
      { if (renderMap.containsKey(a.peek()))
        { r = renderMap.get(a.get());
        }
        else
        { System.err.println("Unknown option "+a.peek());
          return;
        }
      }
    }

    try
    { Dib24Bit db = r.render(sp);

      try
      { if (outfile == null)
        { do
          { outfile = "out"+Math.round(Math.random()*100000.)+".bmp";
          } while (new java.io.File(outfile).exists());
        }
        System.out.println("Writing to "+outfile);
        java.io.DataOutputStream dos =
        new java.io.DataOutputStream
        ( new java.io.FileOutputStream(outfile)
        );
        db.writeTo(dos);
        dos.close();
      }
      catch (java.io.IOException ioe)
      { System.out.println("IO Exception!");
        ioe.printStackTrace();
        System.out.println(ioe);
      }
    }
    catch (WorkInterruptedException wie)
    { System.out.println("Interrupted.");
    }
  }
}