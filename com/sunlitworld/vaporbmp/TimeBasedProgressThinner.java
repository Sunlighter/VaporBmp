package com.sunlitworld.vaporbmp;

public class TimeBasedProgressThinner implements ProgressIndicator
{
  public TimeBasedProgressThinner(ProgressIndicator base, long ms)
  { this.ms = ms;
    this.base = base;
    lastNotification = 0L;
  }

  private ProgressIndicator base;
  private long ms;

  private long lastNotification;

  public void notify(double progress) throws WorkInterruptedException
  { long time = System.currentTimeMillis();
    long dur = time - lastNotification;
    if (dur > ms)
    { lastNotification = time;
      base.notify(progress);
    }
  }
}