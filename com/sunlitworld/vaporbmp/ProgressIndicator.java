package com.sunlitworld.vaporbmp;

public interface ProgressIndicator
{ void notify(double d) throws WorkInterruptedException;
}