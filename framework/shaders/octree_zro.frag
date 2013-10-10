#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;
flat in vec4 normal_g;

// Output variable, will be written to framebuffer automatically
out vec4 out_color;

void main()
{		
	out_color = (abs(dot(normal_g.xyz, vec3(0,0,1)))* color_g)*(1.5+sign(dot(normal_g.xyz, vec3(0,0,1)))/2.5);		
}
