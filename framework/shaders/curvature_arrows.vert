#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in vec3 curvature;

out vec4 position_g;
out vec3 curvature_g;

//pass stuff through
void main()
{
	position_g = position;
	curvature_g = curvature;	
}
