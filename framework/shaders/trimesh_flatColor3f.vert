#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes; passed from main program to shader 
// via vertex buffer objects
in vec4 position;
in vec4 color;

out vec4 position_g;
out vec4 color_v;

//pass stuff through
void main()
{
	position_g = modelview *position;
	color_v = color;
}
