package com.zeromods.core.animation;

public final class EnergyColors {
  private EnergyColors() {}

  public static int mix(int from, int to, float amount) {
    amount=Math.max(0,Math.min(1,amount));
    int result=0;
    for(int shift=0;shift<=16;shift+=8) {
      int a=(from>>shift)&255,b=(to>>shift)&255;
      result|=(a+Math.round((b-a)*amount))<<shift;
    }
    return result;
  }

  public static int highlight(int color, float amount) {
    return mix(color,0xFFFFFF,amount);
  }
}
