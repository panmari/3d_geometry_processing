#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// and interpolated automatically to each fragment
in vec3 curvature_f;

// Output variable, will be written to framebuffer automatically
out vec4 out_color;

void main()
{		
	float mean_curvature = length(curvature_f)/2;
	float curv_log = log(1 + mean_curvature/10);
	
	out_color = vec4(curv_log - 1,
					 curv_log,
					 1 - curv_log,
					 0);
	if (curv_log > 1) {
		out_color.y = 2 - curv_log;
	}
	// out_color = vec4(curv_log,curv_log,curv_log,0); //grayscale representation
	out_color = clamp(out_color, 0, 1);
}
