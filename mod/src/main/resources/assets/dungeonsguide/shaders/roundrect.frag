#version 110
uniform float radius;
uniform vec2 halfSize;
uniform vec2 centerPos;
uniform float smoothness;

float roundedBoxSDF(vec2 CenterPosition, vec2 Size, float Radius) {
    return length(max(abs(CenterPosition)-Size+Radius,0.0))-Radius;
}

void main() {
    float distance 		= roundedBoxSDF(gl_FragCoord.xy - centerPos, halfSize, radius);
    float smoothedAlpha = smoothstep(-smoothness,0.0, -distance);
    gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, smoothedAlpha);
}