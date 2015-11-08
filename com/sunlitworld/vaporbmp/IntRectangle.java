package com.sunlitworld.vaporbmp;

public final class IntRectangle
{ public IntRectangle(int xoffset, int yoffset, int width, int height)
  { this.xoffset = xoffset;
    this.yoffset = yoffset;
    this.width = width;
    this.height = height;
  }

  private int xoffset;
  private int yoffset;
  private int width;
  private int height;

  public IntRectangle leftHalf()
  { return new IntRectangle(xoffset, yoffset, width/2, height);
  }

  public IntRectangle rightHalf()
  { int halfWidth = width/2;
    return new IntRectangle(xoffset+halfWidth, yoffset, width - halfWidth, height);
  }

  public IntRectangle topHalf()
  { return new IntRectangle(xoffset, yoffset, width, height/2);
  }

  public IntRectangle bottomHalf()
  { int halfHeight = height/2;
    return new IntRectangle(xoffset, yoffset+halfHeight, width, height - halfHeight);
  }

  public int left()
  { return xoffset;
  }

  public int top()
  { return yoffset;
  }

  public int width()
  { return width;
  }

  public int height()
  { return height;
  }

  public int right()
  { return xoffset + width;
  }

  public int bottom()
  { return yoffset + height;
  }

  public int area()
  { return width * height;
  }

  public boolean contains(int x, int y)
  { return (x >= xoffset) && (y >= yoffset)
    && (x < (xoffset + width)) && (y < (yoffset + height));
  }

  public int index(int x, int y)
  { return (y - yoffset) * width + (x - xoffset);
  }

  public Rectangle asRectangle()
  { return new Rectangle
    ( new Vector2
      ( (double)xoffset,
        (double)yoffset
      ),
      (double)width,
      (double)height
    );
  }

  public String toString()
  { return "("+xoffset+","+yoffset+")-("+(xoffset+width)+","+(yoffset+height)+")";
  }
}
