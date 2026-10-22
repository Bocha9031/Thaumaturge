#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;

out vec3 viewRay;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

void main() {
    vec4 clip = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = clip;

    float scaleX = length(vec3(ProjMat[0][0], ProjMat[1][0], ProjMat[2][0]));
    float scaleY = length(vec3(ProjMat[0][1], ProjMat[1][1], ProjMat[2][1]));
    vec3 steadyEye = vec3(clip.x / scaleX, clip.y / scaleY, -clip.w);
    viewRay = transpose(mat3(ModelViewMat)) * steadyEye;
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
