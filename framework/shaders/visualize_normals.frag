#version 140

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;
in vec3 normal_g;

uniform float max_valence;
uniform float min_valence;

// Output variable, will be written to the display automatically
out vec4 out_color;
void main()
{		
	//out_color = vec4((normal_g + 1)/2, 1);
	out_color = vec4(abs(normal_g), 1);
}
