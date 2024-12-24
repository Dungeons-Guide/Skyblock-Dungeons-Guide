#version 120

#define TWO_PI 6.28318530718

uniform float radius;
uniform vec2 centerPos;
uniform float smoothness;


void main() {
    vec2 stuff = gl_FragCoord.xy - centerPos;
    float sdf = length(stuff)-radius;
    float clampd = 1.0 - smoothstep(-smoothness, smoothness, sdf);

    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, clampd);
}