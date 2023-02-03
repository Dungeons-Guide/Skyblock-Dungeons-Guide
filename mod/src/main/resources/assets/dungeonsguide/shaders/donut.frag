#version 120


uniform float radius;
uniform float thickness;
uniform vec2 centerPos;
uniform float smoothness;



void main() {
    vec2 stuff = gl_FragCoord.xy - centerPos;
    float sdf = abs(length(stuff)-radius);
    float clampd = 1.0 - smoothstep(thickness, thickness+smoothness, sdf);

    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, clampd);
}