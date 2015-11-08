package com.sunlitworld.vaporbmp;

/**
 * A two-dimensional vector made of double-precision floats,
 * useful for generating Vapor Bitmaps.
 */
public final class Vector2
{
  public Vector2()
  { this.x = 0.;
    this.y = 0.;
  }

  public Vector2(double x, double y)
  { this.x = x;
    this.y = y;
  }

  public double x;
  public double y;

  public double dot(Vector2 other)
  { return x * other.x + y * other.y;
  }

  public double lengthSq()
  { return x*x + y*y;
  }

  public Vector2 times(double d)
  { return new Vector2(x*d, y*d);
  }

  public Vector2 dividedBy(double d)
  { return new Vector2(x/d, y/d);
  }

  public double dividedBy(Vector2 other)
  { return dot(other)/other.lengthSq();
  }

  public Vector2 plus(Vector2 other)
  { return new Vector2(x+other.x, y+other.y);
  }

  public Vector2 minus(Vector2 other)
  { return new Vector2(x-other.x, y-other.y);
  }

  public Vector2 inverse()
  { return new Vector2(-x, -y);
  }

  public Vector2 r90ccw()
  { return new Vector2(-y, x);
  }

  public static Vector2 getRandomDirection()
  { double x;
    double y = Math.random() * 2. - 1.;
    double r;
    do
    { x = y;
      y = Math.random() * 2. - 1.;
      r = x*x + y*y;
    } while ((r >= 1.0) || (r <= 0.04));
    return new Vector2(x,y);
  }

  public String toString()
  { StringBuffer sb = new StringBuffer();
    sb.append("< ");
    sb.append(x);
    sb.append(", ");
    sb.append(y);
    sb.append(">");
    return sb.toString();
  }
}
