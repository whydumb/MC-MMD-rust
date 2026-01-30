//! 动画关键帧

use glam::{Vec3, Quat};
use super::BezierCurve;

/// 骨骼关键帧
#[derive(Clone, Debug)]
pub struct BoneKeyframe {
    pub frame: u32,
    pub translation: Vec3,
    pub rotation: Quat,
    pub interp_x: BezierCurve,
    pub interp_y: BezierCurve,
    pub interp_z: BezierCurve,
    pub interp_rotation: BezierCurve,
}

impl BoneKeyframe {
    pub fn new(frame: u32) -> Self {
        Self {
            frame,
            translation: Vec3::ZERO,
            rotation: Quat::IDENTITY,
            interp_x: BezierCurve::linear(),
            interp_y: BezierCurve::linear(),
            interp_z: BezierCurve::linear(),
            interp_rotation: BezierCurve::linear(),
        }
    }
}

/// Morph 关键帧
#[derive(Clone, Debug)]
pub struct MorphKeyframe {
    pub frame: u32,
    pub weight: f32,
}

impl MorphKeyframe {
    pub fn new(frame: u32, weight: f32) -> Self {
        Self { frame, weight }
    }
}

/// 相机关键帧
#[derive(Clone, Debug)]
pub struct CameraKeyframe {
    pub frame: u32,
    pub distance: f32,
    pub position: Vec3,
    pub rotation: Vec3,
    pub fov: u32,
    pub perspective: bool,
    pub interp_x: BezierCurve,
    pub interp_y: BezierCurve,
    pub interp_z: BezierCurve,
    pub interp_rotation: BezierCurve,
    pub interp_distance: BezierCurve,
    pub interp_fov: BezierCurve,
}

impl CameraKeyframe {
    pub fn new(frame: u32) -> Self {
        Self {
            frame,
            distance: 0.0,
            position: Vec3::ZERO,
            rotation: Vec3::ZERO,
            fov: 30,
            perspective: true,
            interp_x: BezierCurve::linear(),
            interp_y: BezierCurve::linear(),
            interp_z: BezierCurve::linear(),
            interp_rotation: BezierCurve::linear(),
            interp_distance: BezierCurve::linear(),
            interp_fov: BezierCurve::linear(),
        }
    }
}
