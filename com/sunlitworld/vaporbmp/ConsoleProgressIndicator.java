package com.sunlitworld.vaporbmp;

public class ConsoleProgressIndicator implements ProgressIndicator
{
  public ConsoleProgressIndicator()
  { 
  }

  public void notify(double progress) throws WorkInterruptedException
  { System.out.println(""+(progress*100)+"% done.");
  }
};