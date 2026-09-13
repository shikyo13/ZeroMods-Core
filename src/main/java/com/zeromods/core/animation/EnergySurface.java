package com.zeromods.core.animation;

/** Stateless world-space surface presets. No particles, textures or per-frame caches are allocated. */
public final class EnergySurface {
  private EnergySurface() {}

  public static void render(float left, float right, float bottom, float top, float time,
      float impactAge, float hitU, float hitV, int color, int accent,
      int pattern, int formation, float progress, HexFieldPattern.Stroke stroke) {
    if (right <= left || top <= bottom || progress <= 0) return;
    progress = Math.min(1, progress);
    final float p = progress;
    boolean dissolve = formation == 1 && p < 1;
    if (dissolve) {
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
    } else stroke.draw(left,(bottom+top)/2,right,(bottom+top)/2,(top-bottom)/2,color,.17f*p);

    HexFieldPattern.Stroke detail = (x1,y1,x2,y2,w,c,a) -> {
      float mask = dissolve ? smooth((p-noise((x1+x2)/2,(y1+y2)/2,time))*9) : p;
      if (mask > .01f) stroke.draw(x1,y1,x2,y2,w,c,a*mask);
    };
    if (pattern == 0) {
      HexFieldPattern.render(left,right,bottom,top,time,impactAge,hitU,hitV,color,detail);
      return;
    }
    if (pattern == 3) PlasmaSurface.render(left, right, bottom, top, time, accent, detail);
    if (pattern == 2) {
      // Each lattice cell owns one drifting mote. Adjacent tiles clip the same world-space mote.
      for (int x=(int)Math.floor(left)-1;x<=Math.ceil(right);x++)
        for(int y=(int)Math.floor(bottom)-1;y<=Math.ceil(top);y++) {
          float seed=(float)(Math.sin(x*127.1+y*311.7)*43758.5453);
          seed-=Math.floor(seed);
          float u=x+.5f+(float)Math.sin(time*.018+seed*19)*.3f;
          float v=y+(seed+time*.007f-(float)Math.floor(seed+time*.007f));
          float flicker=.4f+.25f*(float)Math.sin(time*.06+seed*31);
          detail.draw(u-.035f,v,u+.035f,v,.035f,accent,flicker);
          detail.draw(u-.075f,v,u+.075f,v,.075f,accent,flicker*.14f);
        }
    }
    if(impactAge>=0 && impactAge<32) {
      float radius=impactAge*.115f, fade=1-impactAge/32;
      for(int i=0;i<64;i++) {
        double a=i*Math.PI/32, b=(i+1)*Math.PI/32;
        float x1=hitU+(float)Math.cos(a)*radius,y1=hitV+(float)Math.sin(a)*radius;
        float x2=hitU+(float)Math.cos(b)*radius,y2=hitV+(float)Math.sin(b)*radius;
        detail.draw(x1,y1,x2,y2,.09f,accent,fade*.13f);
        detail.draw(x1,y1,x2,y2,.015f,accent,fade*.7f);
      }
    }
  }

  private static float noise(float u,float v,float time) {
    return .5f + .21f*(float)Math.sin(u*2.1+v*.9+time*.023)
        + .16f*(float)Math.sin(v*3.2-u*.7-time*.017)
        + .08f*(float)Math.sin(u*5.1+v*4.3);
  }
  private static float smooth(float x) { x=Math.max(0,Math.min(1,x));return x*x*(3-2*x); }
}
