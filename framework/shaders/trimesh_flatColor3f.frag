#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec4 color_g;
flat in vec3 normal_g;

// Output variable, will be written to framebuffer automatically
out vec4 out_color;

void main()
{		
	out_color = color_g * (abs(dot(normal_g, vec3(0,0,-1)))+0.1f) * ((3 + sign(dot(normal_g, vec3(0,0,-1))))/4); //plus ambient	
}
