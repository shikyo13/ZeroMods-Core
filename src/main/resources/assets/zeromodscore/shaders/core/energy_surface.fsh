#version 150
uniform float GameTime;
in vec4 tint;
in vec3 accent;
in vec2 surfacePoint;
in vec3 viewPosition;
in float progress;
in float impact;
flat in int mode;
flat in int flags;
out vec4 fragColor;
const float PI = 3.14159265359;
float noise(vec3 p) {
    return (sin(p.x * 2.1 + sin(p.z * 1.8))
        + sin(p.y * 2.7 + p.z * .9 + sin(p.x * 1.3))) * .25 + .5;
}
float hex(vec2 p) {
    p.x *= 1.73205;
    vec2 a = mod(p, vec2(1.73205, 3.)) - vec2(.866, 1.5);
    vec2 b = mod(p - vec2(.866, 1.5), vec2(1.73205, 3.)) - vec2(.866, 1.5);
    vec2 q = dot(a,a) < dot(b,b) ? a : b;
    return max(abs(q.x) * .866 + abs(q.y) * .5, abs(q.y));
}
void main() {
    bool sphere = (flags & 4) != 0;
    int style = flags & 3;
    float time = (flags & 16) != 0 ? 0. : GameTime * 1200.;
    float az = surfacePoint.x;
    float latitude = surfacePoint.y;
    vec3 p = sphere ? 5. * vec3(cos(latitude) * cos(az), sin(latitude), cos(latitude) * sin(az))
        : vec3(surfacePoint, 0.);
    float elev = (flags & 8) != 0 ? (latitude + PI * .5) / PI : max(latitude, 0.) / (PI * .5);
    float n = noise(p * .85 + vec3(time * .13, -time * .08, time * .1));
    float mask = progress, edgeGlow = 0.;
    if (sphere) {
        if (mode == 0) {
            float travel = progress * progress * 2.;
            float dist = abs(atan(sin(az), cos(az))) / PI;
            mask = smoothstep(dist - .06, dist + .06, travel);
            edgeGlow = exp(-abs(dist - travel) * 95.) * step(travel, 1.);
        } else if (mode == 1) {
            mask = 1. - smoothstep(progress - .035, progress + .035, elev);
            edgeGlow = exp(-abs(elev - progress) * 110.);
        } else if (mode == 2) {
            float wave = floor(elev * 18.) / 18. + .06 * sin(floor(az * 14.) * 2.4);
            mask = 1. - smoothstep(progress - .025, progress + .025, wave);
            edgeGlow = exp(-abs(wave - progress) * 70.);
        } else if (mode == 3) {
            float sector = mod(az + PI, .523599) / .523599;
            float seams = exp(-min(sector, 1. - sector) * 65.);
            mask = smoothstep(.35, 1., progress) * (1. - smoothstep(progress - .1, progress + .1, elev));
            edgeGlow = seams * (1. - smoothstep(clamp(progress * 1.8, 0., 1.) - .03,
                clamp(progress * 1.8, 0., 1.) + .03, elev)) * (1. - smoothstep(.75, 1., progress));
        } else if (mode == 4) {
            float edge = 1. - clamp((progress - .13) / .87, 0., 1.);
            mask = smoothstep(edge - .035, edge + .035, elev) * smoothstep(.1, .18, progress);
            edgeGlow = exp(-abs(elev - edge) * 110.) * smoothstep(.1, .18, progress);
        }
    }
    if (mode == 5) {
        float edge = 1.1 - progress * 1.2;
        mask = smoothstep(edge - .025, edge + .025, n);
        edgeGlow = exp(-abs(n - edge) * 100.);
    }
    if (progress >= .999) { mask = 1.; edgeGlow = 0.; }
    if (progress < .001) discard;
    vec3 normal = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    float fresnel = pow(1. - abs(dot(normal, normalize(-viewPosition))), 2.8);
    float detail = 0.;
    vec2 uv = sphere ? vec2(az * 11., latitude * 15.9155) : surfacePoint * 2.;
    if (style == 0) {
        float cellEdge = 1. - smoothstep(.018, .065, abs(hex(uv) - .95));
        detail = cellEdge * (.24 + impact * .7) + impact * .09;
    } else if (style == 1) {
        detail = .014 * sin(p.y * 5. - time) + impact * .48;
    } else if (style == 2) {
        uv = sphere ? vec2(az * 16., latitude * 16.5521 - time * .2) : surfacePoint * 3. - vec2(0., time * .2);
        vec2 cell = floor(uv);
        float rnd = fract(sin(dot(cell, vec2(12.9898, 78.233))) * 43758.5453);
        vec2 d = abs(fract(uv) - .5);
        float pixel = 1. - smoothstep(.06, .13 + impact * .06, max(d.x, d.y));
        detail = step(.9 - impact * .55, rnd) * pixel * (.8 + impact) + impact * .025;
    } else {
        float veins = exp(-abs(n - .50) * 95.);
        detail = veins * (.4 + impact * 1.2) + impact * .065;
    }
    float alpha = ((.045 + fresnel * .19 + detail) * mask + edgeGlow * .65) * tint.a;
    vec3 base = style >= 2 ? mix(tint.rgb, accent, clamp(detail / max(.001, .045 + fresnel * .19 + detail), 0., 1.)) : tint.rgb;
    vec3 color = mix(base, vec3(1.), clamp(edgeGlow * .65 + impact * .18, 0., .75));
    float coverage = clamp(alpha, 0., .95);
    fragColor = vec4(color * coverage, coverage);
}
