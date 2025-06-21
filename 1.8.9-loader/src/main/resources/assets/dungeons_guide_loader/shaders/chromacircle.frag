#version 120

#define TWO_PI 6.28318530718

uniform float radius;
uniform vec2 centerPos;
uniform float smoothness;
uniform float value;


vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 stuff = gl_FragCoord.xy - centerPos;
    float sdf = length(stuff)-radius;
    float saturation =  1.0+(sdf / radius);
    float clampd = 1.0 - smoothstep(-smoothness, smoothness, sdf);
    float angle = atan(-stuff.x, -stuff.y) / TWO_PI + 0.5;

    vec3 col = hsv2rgb(vec3(angle, saturation, value));

    gl_FragColor = vec4(col,clampd);
}