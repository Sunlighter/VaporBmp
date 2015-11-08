package com.sunlitworld.vaporbmp;

public class StepProgressIndicator implements ProgressIndicator
{
  public StepProgressIndicator(ProgressIndicator base, int step, int outof)
  { this.step = step;
    this.outof = outof;
    this.base = base;
  }

  private ProgressIndicator base;
  private int step;
  private int outof;

  public void notify(double progress) throws WorkInterruptedException
  { progress /= (double) outof;
    progress += (double)(step - 1) / (double)outof;
    base.notify(progress);
  }
};