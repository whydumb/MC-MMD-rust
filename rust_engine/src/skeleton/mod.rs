//! 骨骼系统和 IK 求解器

mod bone;
mod ik_solver;
mod manager;

pub use bone::Bone;
pub use ik_solver::IkSolver;
pub use manager::BoneManager;

use glam::{Vec3, Quat, Mat4};

/// 骨骼变换数据
#[derive(Clone, Debug)]
pub struct BoneTransform {
    pub translation: Vec3,
    pub rotation: Quat,
    pub scale: Vec3,
}

impl Default for BoneTransform {
    fn default() -> Self {
        Self {
            translation: Vec3::ZERO,
            rotation: Quat::IDENTITY,
            scale: Vec3::ONE,
        }
    }
}

impl BoneTransform {
    pub fn to_matrix(&self) -> Mat4 {
        Mat4::from_scale_rotation_translation(self.scale, self.rotation, self.translation)
    }
}
