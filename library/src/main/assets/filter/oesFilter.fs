#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

in vec2 fTexCoord;
layout(location=4) uniform samplerExternalOES inputTexture;
out vec4 gl_FragColor;
void main(){
    gl_FragColor = texture(inputTexture, fTexCoord);
}