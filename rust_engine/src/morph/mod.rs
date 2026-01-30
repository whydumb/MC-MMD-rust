//! Morph 变形系统

mod morph;
mod manager;

pub use morph::Morph;
pub use manager::MorphManager;

use glam::{Vec3, Vec4};

/// Morph 类型
#[derive(Clone, Debug, PartialEq, Eq)]
pub enum MorphType {
    Group,
    Vertex,
    Bone,
    Uv,
    AdditionalUv1,
    AdditionalUv2,
    AdditionalUv3,
    AdditionalUv4,
    Material,
    Flip,
    Impulse,
}

/// 顶点 Morph 偏移
#[derive(Clone, Debug)]
pub struct VertexMorphOffset {
    pub vertex_index: u32,
    pub offset: Vec3,
}

/// 骨骼 Morph 偏移
#[derive(Clone, Debug)]
pub struct BoneMorphOffset {
    pub bone_index: u32,
    pub translation: Vec3,
    pub rotation: Vec4,
}

/// 材质 Morph 偏移
#[derive(Clone, Debug)]
pub struct MaterialMorphOffset {
    pub material_index: i32,
    pub operation: u8,
    pub diffuse: Vec4,
    pub specular: Vec3,
    pub specular_strength: f32,
    pub ambient: Vec3,
    pub edge_color: Vec4,
    pub edge_size: f32,
    pub texture_tint: Vec4,
    pub environment_tint: Vec4,
    pub toon_tint: Vec4,
}
