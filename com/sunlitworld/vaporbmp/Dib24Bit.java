package com.sunlitworld.vaporbmp;

public class Dib24Bit
{
  public Dib24Bit(int xsize, int ysize)
  { this.xsize = xsize;
    arrayxsize = bump(xsize*3);
    this.ysize = ysize;
    byteImage = new byte[arrayxsize * ysize];
  }

  private int xsize;
  private int arrayxsize;
  private int ysize;

  private byte[] byteImage;

  private int bump(int p)
  { return (p + 3) & (~3);
  }

  public int width()
  { return xsize;
  }

  public int height()
  { return ysize;
  }

  private int getOffset(int x, int y)
  { return ((ysize - y - 1) * arrayxsize) + (x * 3);
  }

  public int getPixel(int x, int y)
  { int offset = getOffset(x,y);
    int b = (int)byteImage[offset++] & 255;
    int g = (int)byteImage[offset++] & 255;
    int r = (int)byteImage[offset] & 255;
    return (r << 16) | (g << 8) | b;
  }

  public void putPixel(int x, int y, int c)
  { int offset = getOffset(x,y);
    byteImage[offset++] = (byte)(c & 255);
    c >>= 8;
    byteImage[offset++] = (byte)(c & 255);
    c >>= 8;
    byteImage[offset] = (byte)(c & 255);
  }

  public void writeTo(java.io.DataOutput out) throws java.io.IOException
  { int imageSize = arrayxsize * ysize;
    int fileSize = imageSize + 14 + 40;
    int offsetToPixels = 14 + 40;

    // file header = 14 bytes

    out.writeBytes("BM");
    writeWinInt(out, fileSize);
    out.writeInt(0);
    writeWinInt(out, offsetToPixels);

    // info header = 40 bytes

    writeWinInt(out, 40); // size of this header
    writeWinInt(out, xsize);
    writeWinInt(out, ysize);
    writeWinShort(out, 1); // planes
    writeWinShort(out, 24); // bits per pixel
    out.writeInt(0); // compression
    writeWinInt(out, imageSize);
    out.writeInt(0); // X pixels per meter (unused)
    out.writeInt(0); // Y pixels per meter (unused)
    out.writeInt(0); // number of colors used
    out.writeInt(0); // number of important colors

    // image data

    out.write(byteImage);
  }

  private static void writeWinShort(java.io.DataOutput out, int i)
  throws java.io.IOException
  { out.writeByte(i); i >>= 8;
    out.writeByte(i);
  }

  private static void writeWinInt(java.io.DataOutput out, int i)
  throws java.io.IOException
  { out.writeByte(i); i >>= 8;
    out.writeByte(i); i >>= 8;
    out.writeByte(i); i >>= 8;
    out.writeByte(i);
  }

}