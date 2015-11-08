# VaporBmp
A bitmap generator in Java

I wrote this in 2001, and it was my third Vapor Bitmap Generator. It is based on an algorithm I read about in the book
*Fundamentals of Interactive Computer Graphics*. I think.

This took a long time to run when I wrote it, but now it runs pretty swiftly.

To compile it with Oracle's JDK, put the Java compiler on your path, go to the `com/sunlitworld/vaporbmp` directory,
and compile it with `javac *.java`. To run it, make sure your `CLASSPATH` includes the directory where your git
repository is (the parent of the `com` directory) and just run `com.sunlitworld.vaporbmp.Vapor` with
command-line options.

The command-line options are:

Option | Meaning
------|------
`-tile` |  Don't crossfade the edges. Cuts the work by 75%.
`-outfile` *name* | Set the name of the output file, which will always be in `.bmp` format.
`-size` *size* | Set the size of the bitmap in pixels. The bitmap is always square. The default is `2500`.
`-iters` *count* | Set the number of iterations. More iterations make it look nicer but take longer. The default is `24576`.
*rendermode* | Set the color scheme used.

Render modes are as follows:

Option | Meaning
------|------
`-gray` | One channel, varying black to white.
`-rgb` | Three independent channels, one for each of red, green, and blue.
`-cmy` | Three independent channels, one for each of cyan, magenta, and yellow.
`-rc` | Two independent channels, red and cyan.
`-yrcb` | Two independent channels, orange and cyan-blue.
`-yb` | Two independent channels, yellow and blue.
`-ygmb` | Two independent channels, yellow-green and magenta-blue.
`-gm` | Two independent channels, green and magenta.
`-cgmr` | Two independent channels, cyan-green and magenta-red.
`-usa` | Two independent channels, red and blue. Green is set to max(red, blue), giving the appearance of red, white, and blue.
`-xmas` | Two indeendent channels, red and green. Blue is set to max(red, green), giving the appearance of red, white, and green.
`-ocean` | Two independent channels, green and blue. Red is set to max(green, blue), giving the appearance of green, white, and blue.

Add an `x` to any of these (e.g., `-oceanx`) to get a weird shimmery effect. (I don't think this works for desktop
wallpaper, though, because it makes desktop icons harder to see.)

Also, `-rgblite` and `-rgbdark` are the same as `-rgb` but mixed with white or black, respectively.

A batch file is provided for Windows which runs the generator at "below normal" priority, and a `bash` script is
provided for Linux which generates a large number of bitmaps in a single run.
