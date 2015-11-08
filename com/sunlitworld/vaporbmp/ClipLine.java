package com.sunlitworld.vaporbmp;

public final class ClipLine
{ public ClipLine(Vector2 point, Vector2 direc)
  { this.point = point;
    this.direc = direc;
  }

  public Vector2 point;
  public Vector2 direc;

  public double distanceIn(Vector2 other)
  { return other.minus(point).dividedBy(direc);
  }

  public boolean includes(Vector2 other)
  { return distanceIn(other) >= 0;
  }

  public ClipLine flip()
  { return new ClipLine(point, direc.inverse());
  }

  public String toString()
  { StringBuffer sb = new StringBuffer();
    sb.append("(Point=");
    sb.append(point);
    sb.append(", Direc=");
    sb.append(direc);
    sb.append(")");
    return sb.toString();
  }
}