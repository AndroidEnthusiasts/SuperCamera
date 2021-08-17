#version 300 es
layout(location=0) in vec3 vPos;
layout(location=1) in vec2 vTexCoord;
out vec2 fTexCoord;

layout(location=0) uniform mat4 model;
layout(location=1) uniform mat4 view;
layout(location=2) uniform mat4 projection;
layout(location=3) uniform mat4 mvp;

void main() {
    gl_Position = mvp * vec4(vPos, 1.0f);
    fTexCoord = vTexCoord;
}