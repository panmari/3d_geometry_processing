#version 140

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec3 normal_g;

uniform float max_valence;
uniform float min_valence;

// Output variable, will be written to the display automatically
out vec4 out_color;
void main()
{	
	vec4 color_g = vec4(0.2f,0.2f,0.8f,1.f);
		
	out_color = color_g * (abs(dot(normal_g, vec3(0,0,1)))+0.1) //diffuse shading plus ambient (0.1)
	* (1.5 + sign(dot(normal_g, vec3(0,0,1)))/2.5); //scaled to a fifth when looking at the back of the triangle

}
