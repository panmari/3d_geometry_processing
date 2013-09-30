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
	float curv_log = log(1 + curvature_f/10);
	out_color = vec4(curv_log - 1,
					 curv_log,
					 1 - curv_log,
					 0);
	if (curv_log > 1) {
		//out_color = vec4(1,1,1,0);
		out_color.y = 2 - curv_log;
	}
	out_color = clamp(out_color, 0, 1);
}
