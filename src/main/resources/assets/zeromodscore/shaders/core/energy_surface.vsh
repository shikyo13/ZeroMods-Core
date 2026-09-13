#version 150
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
out vec4 tint;
out vec3 accent;
out vec2 surfacePoint;
out vec3 viewPosition;
out float progress;
out float impact;
flat out int mode;
flat out int flags;
void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    viewPosition = view.xyz;
    tint = Color;
    accent = Normal;
    surfacePoint = UV0;
    mode = UV1.x;
    impact = float(UV1.y) / 32767.0;
    flags = UV2.x;
    progress = float(UV2.y) / 32767.0;
}
