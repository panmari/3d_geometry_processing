#version 150
// Default vertex shader

// Uniform variables, set in main program
uniform mat4 projection; 
uniform mat4 modelview;

// Input vertex attributes passed from the main program to shader 
// The position variable corresponds to data passed using
// glDisplayable.addElement(float[], Semantic.POSITION, 3);
in vec4 position;
in float valence;
in float curvature;

//The following would declare and additional variable that could be passed to the shader.
//It would correspond to the data passed via
//glDisplayable.addElement(float[], Semantic.USERDEFINED, 3, "color");
//in vec4 color;

// Output variables are passed to the fragment shader, or, if existent to the geometry shader.
// They have to be declared as in variables in the next shader.
out float valence_f;
out float curvature_f;
out vec4 color_g;

void main()
{
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position, this variable has to be computed
	// either in the vertex shader or in the geometry shader, if present.
	gl_Position = projection * modelview * position;
	
	valence_f = valence;
	curvature_f = curvature;
	float valence = valence_f;
	vec4 frag_color;
	if (valence > 6){
		frag_color = vec4(1,0,0,.5);
	}
	else {
		if (valence < 6){
			frag_color = vec4(0,0,1,.5);
		}
		else {
			frag_color = vec4(0,1,0,.5);
		}

	}
	color_g = frag_color;
}
