#version 120
uniform float radius;
uniform vec2 halfSize;
uniform vec2 centerPos;
uniform float smoothness;
varying vec4 color;


float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    return length(max(abs(CenterPosition)-Size+Radius,0.0))-Radius;
}

void main() {
    float distance 		= roundedBoxSDF(gl_FragCoord.xy - centerPos, halfSize, radius);
    float smoothedAlpha = smoothstep(-smoothness,0.0, -distance);
    gl_FragColor = color * vec4(1.0, 1.0, 1.0, smoothedAlpha);
}