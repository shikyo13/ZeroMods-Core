package com.zeromods.core.animation;

/** Stateless world-space surface presets. No particles, textures or per-frame caches are allocated. */
public final class EnergySurface {
  private EnergySurface() {}

  public static void render(float left, float right, float bottom, float top, float time,
      float impactAge, float hitU, float hitV, int color, int accent,
      int pattern, int formation, float progress, HexFieldPattern.Stroke stroke) {
    render(left,right,bottom,top,time,impactAge,hitU,hitV,color,accent,pattern,formation,progress,true,stroke);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      float impactAge, float hitU, float hitV, int color, int accent,
      int pattern, int formation, float progress, boolean includeFill, HexFieldPattern.Stroke stroke) {
    render(left, right, bottom, top, time, ImpactWaves.single(impactAge, hitU, hitV), color,
        accent, pattern, formation, progress, includeFill, stroke);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      ImpactWaves waves, int color, int accent, int pattern, int formation, float progress,
      boolean includeFill, HexFieldPattern.Stroke stroke) {
    render(left, right, bottom, top, time, (HexFieldPattern.Ripples) waves, color, accent,
        pattern, formation, progress, includeFill, stroke);
  }

  public static void render(float left, float right, float bottom, float top, float time,
      HexFieldPattern.Ripples waves, int color, int accent, int pattern, int formation,
      float progress, boolean includeFill, HexFieldPattern.Stroke stroke) {
    if (right <= left || top <= bottom || progress <= 0) return;
    progress = Math.min(1, progress);
    final float p = progress;
    boolean dissolve = formation == 1 && p < 1;
    if (includeFill && dissolve) {
      // A fixed quarter-block grid bounds work by visible field area; coordinates match at seams.
      for (int x = (int)Math.floor(left*4); x < Math.ceil(right*4); x++)
        for (int y = (int)Math.floor(bottom*4); y < Math.ceil(top*4); y++) {
          float a = Math.max(left,x*.25f), b = Math.min(right,(x+1)*.25f);
          float c = Math.max(bottom,y*.25f), d = Math.min(top,(y+1)*.25f);
          float u=(a+b)/2, v=(c+d)/2;
          float n=noise(u,v,time), reveal=smooth((p-n)*9);
          if (reveal <= 0) continue;
          float edge=4*reveal*(1-reveal);
          stroke.draw(a,(c+d)/2,b,(c+d)/2,(d-c)/2,
              edge > .25 ? accent : color, (.15f+edge*.48f)*reveal);
        }
    } else if (includeFill) stroke.draw(left,(bottom+top)/2,right,(bottom+top)/2,(top-bottom)/2,color,.17f*p);

    HexFieldPattern.Stroke detail = (x1,y1,x2,y2,w,c,a) -> {
      float mask = dissolve ? smooth((p-noise((x1+x2)/2,(y1+y2)/2,time))*9) : p;
      if (mask > .01f) stroke.draw(x1,y1,x2,y2,w,c,a*mask);
    };
    if (pattern == 0) {
      HexFieldPattern.render(left,right,bottom,top,time,waves,color,accent,HexFieldPattern.Style.forAccent(accent),detail);
      return;
    }
    if (pattern == 3) {
      var plasmaImpact = new HexFieldPattern.Style(40, .35f, 1, accent, 1);
      PlasmaSurface.render(left, right, bottom, top, time, accent, (x1,y1,x2,y2,w,c,a) -> {
        float pulse = waves.sample((x1+x2)*.5f, (y1+y2)*.5f, plasmaImpact);
        detail.draw(x1,y1,x2,y2,w,EnergyColors.highlight(c,pulse*.18f),Math.min(1,a*(1+pulse*3)));
      });
      return;
    }
    if (pattern == 2) {
      pixels(left, right, bottom, top, time, waves, accent, detail);
      return;
    }
    waves.rings(left, right, bottom, top, accent, HexFieldPattern.Style.FIELD, detail);
  }

  private static void pixels(float left, float right, float bottom, float top, float time,
      HexFieldPattern.Ripples waves, int accent, HexFieldPattern.Stroke stroke) {
    for (int x=(int)Math.floor(left)-1; x<=Math.ceil(right); x++)
      for (int y=(int)Math.floor(bottom)-1; y<=Math.ceil(top); y++) {
        float seed=(float)(Math.sin(x*127.1+y*311.7)*43758.5453);
        seed-=Math.floor(seed);
        float u=x+.5f+(float)Math.sin(time*.018+seed*19)*.3f;
        float v=y+(seed+time*.007f-(float)Math.floor(seed+time*.007f));
        float pulse=waves.sample(u,v,HexFieldPattern.Style.FIELD);
        float flicker=.4f+.25f*(float)Math.sin(time*.06+seed*31);
        float spark=(float)Math.sqrt(pulse);
        float size=.035f+spark*.065f;
        square(stroke,u,v,size,EnergyColors.highlight(accent,spark*.55f),Math.min(1,flicker+spark*.8f));
        square(stroke,u,v,Math.min(.18f,size*2),accent,flicker*.14f+spark*.22f);
        if (pulse < .06f) continue;
        // A repeatable burst belongs to each cell, keeping adjoining field tiles in sync.
        for(int fragment=0;fragment<5;fragment++) {
          double angle=seed*Math.PI*2+fragment*Math.PI*2/5;
          float travel=(1-pulse)*.55f+.05f;
          float a=u+(float)Math.cos(angle)*travel;
          float b=v+(float)Math.sin(angle)*travel+travel*.25f;
          square(stroke,a,b,.025f+spark*.045f,EnergyColors.highlight(accent,spark*.4f),spark*.95f);
          square(stroke,a,b,.09f,accent,spark*.14f);
        }
      }
  }

  private static void square(HexFieldPattern.Stroke stroke, float x, float y, float size,
      int color, float alpha) {
    stroke.draw(x-size,y,x+size,y,size,color,alpha);
  }

  private static float noise(float u,float v,float time) {
    return .5f + .21f*(float)Math.sin(u*2.1+v*.9+time*.023)
        + .16f*(float)Math.sin(v*3.2-u*.7-time*.017)
        + .08f*(float)Math.sin(u*5.1+v*4.3);
  }
  private static float smooth(float x) { x=Math.max(0,Math.min(1,x));return x*x*(3-2*x); }
}
