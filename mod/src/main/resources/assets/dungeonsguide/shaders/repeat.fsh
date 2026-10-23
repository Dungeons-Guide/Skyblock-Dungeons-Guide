#version 330

layout(std140) uniform Repeat {
    vec2 position;
    vec2 size;
};

// 1.8.9: uniform sampler2D tex
uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main(void)
{
    // Result
    vec2 coord = position + size * (texCoord0 - floor(texCoord0));

    // Output
    fragColor = texture(Sampler0, coord);
}
