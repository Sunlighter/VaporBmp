package com.sunlitworld.vaporbmp;

public final class Rectangle
{ public Rectangle(Vector2 origin, double width, double height)
  { this.origin = origin;
    this.width = width;
    this.height = height;
  }

  public Vector2 origin;
  public double width;
  public double height;

  public Vector2 getCorner(int which)
  { switch(which & 3)
    { case 0: return origin;
      case 1: return new Vector2(origin.x + width, origin.y);
      case 2: return new Vector2(origin.x + width, origin.y + height);
      case 3: return new Vector2(origin.x, origin.y + height);
      default: throw new UnknownError("Something wrong with \"&\" operator");
    }
  }

  public Vector2 getCenter()
  { return new Vector2(origin.x + width/2, origin.y + height/2);
  }

  public double getMinimumOffset(Vector2 direc)
  { double smallest = java.lang.Double.POSITIVE_INFINITY;
    for (int i = 0; i < 4; ++i)
    { double test = getCorner(i).dividedBy(direc);
      if (smallest > test) smallest = test;
    }
    return smallest;
  }

  public double getMaximumOffset(Vector2 direc)
  { double largest = java.lang.Double.NEGATIVE_INFINITY;
    for (int i = 0; i < 4; ++i)
    { double test = getCorner(i).dividedBy(direc);
      if (largest < test) largest = test;
    }
    return largest;
  }

  public ClipLine getRandomClipLine()
  { Vector2 dir = Vector2.getRandomDirection();
    double d1 = getMinimumOffset(dir);
    double d2 = getMaximumOffset(dir);
    double d = (d2 - d1) * Math.random() + d1;
    return new ClipLine(dir.times(d), dir);
  }

  public static final int ENTIRELY_INSIDE = 2;
  public static final int ENTIRELY_OUTSIDE = -2;
  public static final int MOSTLY_INSIDE = 1;
  public static final int MOSTLY_OUTSIDE = -1;

  public int clipStatus(ClipLine clip)
  { boolean anyInside = false;
    boolean anyOutside = false;
    for (int i = 0; i < 4; ++i)
    { boolean t = clip.includes(getCorner(i));
      if (t) anyInside = true;
      else anyOutside = true;
    }
    if (anyInside)
    { if (anyOutside)
      { boolean t = clip.includes(getCenter());
        if (t) return MOSTLY_INSIDE;
        else return MOSTLY_OUTSIDE;
      }
      else return ENTIRELY_INSIDE;
    } else
    { if (anyOutside) return ENTIRELY_OUTSIDE;
      else throw new UnknownError("Something Wrong with Booleans!");
    }
  }
}
