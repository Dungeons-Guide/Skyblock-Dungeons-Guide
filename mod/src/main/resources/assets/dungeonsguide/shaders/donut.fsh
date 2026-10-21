#version 330

layout(std140) uniform Donut {
    float radius;
    float thickness;
    vec2 centerPos;
    float smoothness;
};

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 stuff = gl_FragCoord.xy - centerPos;
    float sdf = abs(length(stuff)-radius);
    float clampd = 1.0 - smoothstep(thickness, thickness+smoothness, sdf);

    fragColor = vertexColor * vec4(1.0, 1.0, 1.0, clampd);
}
