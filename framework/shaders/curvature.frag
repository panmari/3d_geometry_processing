#version 150

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;
in float valence_f;
in float curvature_f;

uniform float max_valence;
uniform float min_valence;

// Output variable, will be written to the display automatically
out vec4 out_color;
void main()
{		
	out_color = vec4(1,1,1,0)*log(1 + curvature_f/100);
}
