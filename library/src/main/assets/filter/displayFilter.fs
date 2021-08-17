#version 300 es
in vec2 fTexCoord;
layout(location=4) uniform sampler2D inputTexture;
out vec4 gl_FragColor;
void main() {
    gl_FragColor = texture(inputTexture, fTexCoord);
}
