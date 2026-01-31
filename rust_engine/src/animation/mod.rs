//! VMD/VPD 动画系统

mod vmd_file;
mod vpd_file;
mod vmd_animation;
mod keyframe;
mod bezier;
mod animation_layer;

pub use vmd_file::VmdFile;
pub use vpd_file::{VpdFile, VpdBone, VpdMorph};
pub use vmd_animation::VmdAnimation;
pub use keyframe::{BoneKeyframe, MorphKeyframe, CameraKeyframe};
pub use bezier::BezierCurve;
pub use animation_layer::{AnimationLayer, AnimationLayerManager, AnimationLayerState, AnimationLayerConfig};

use glam::{Vec3, Quat};

/// 动画帧数据
#[derive(Clone, Debug)]
pub struct AnimationFrame {
    pub frame: u32,
    pub translation: Vec3,
    pub rotation: Quat,
}

/// 插值参数
#[derive(Clone, Debug, Default)]
pub struct InterpolationParams {
    pub x: BezierCurve,
    pub y: BezierCurve,
    pub z: BezierCurve,
    pub rotation: BezierCurve,
}
