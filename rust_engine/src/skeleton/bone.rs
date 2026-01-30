//! 骨骼节点

use glam::{Vec3, Quat, Mat4};
use mmd::pmx::bone as pmx_bone;
use mmd::pmx::types::DefaultConfig;


/// IK 链接信息
#[derive(Clone, Debug)]
pub struct IkLink {
    pub bone_index: i32,
    pub has_limits: bool,
    pub limit_min: Vec3,
    pub limit_max: Vec3,
}

/// IK 配置
#[derive(Clone, Debug)]
pub struct IkConfig {
    pub target_bone: i32,
    pub iterations: u32,
    pub limit_angle: f32,
    pub links: Vec<IkLink>,
}

/// 骨骼节点
#[derive(Clone, Debug)]
pub struct Bone {
    pub name: String,
    pub parent_index: i32,
    pub transform_level: i32,
    
    // 初始位置（世界空间）
    pub initial_position: Vec3,
    // 相对于父骨骼的偏移（在build_hierarchy中计算）
    pub bone_offset: Vec3,
    // 逆绑定矩阵（在build_hierarchy中计算）
    pub inverse_bind_matrix: Mat4,
    
    // 骨骼标志
    pub is_rotatable: bool,
    pub is_movable: bool,
    pub is_ik: bool,
    pub is_append_rotate: bool,
    pub is_append_translate: bool,
    pub is_append_local: bool,
    pub is_fixed_axis: bool,
    pub is_local_axis: bool,
    pub deform_after_physics: bool,
    pub enable_ik: bool,
    
    // 附加变换
    pub append_parent: i32,
    pub append_rate: f32,
    pub append_translate: Vec3,
    pub append_rotate: Quat,
    
    // 固定轴
    pub fixed_axis: Vec3,
    
    // 本地轴
    pub local_axis_x: Vec3,
    pub local_axis_z: Vec3,
    
    // IK
    pub ik_config: Option<IkConfig>,
    
    // 动画状态
    pub animation_translate: Vec3,
    pub animation_rotate: Quat,
    pub ik_rotate: Quat,
    
    // 初始状态（用于 append local 计算）
    pub initial_translate: Vec3,
    pub initial_rotate: Quat,
    
    // 变换结果
    pub local_transform: Mat4,
    pub global_transform: Mat4,
}

impl Bone {
    pub fn new(name: String) -> Self {
        Self {
            name,
            parent_index: -1,
            transform_level: 0,
            initial_position: Vec3::ZERO,
            bone_offset: Vec3::ZERO,
            inverse_bind_matrix: Mat4::IDENTITY,
            is_rotatable: true,
            is_movable: false,
            is_ik: false,
            is_append_rotate: false,
            is_append_translate: false,
            is_append_local: false,
            is_fixed_axis: false,
            is_local_axis: false,
            deform_after_physics: false,
            enable_ik: false,
            append_parent: -1,
            append_rate: 0.0,
            append_translate: Vec3::ZERO,
            append_rotate: Quat::IDENTITY,
            fixed_axis: Vec3::Z,
            local_axis_x: Vec3::X,
            local_axis_z: Vec3::Z,
            ik_config: None,
            animation_translate: Vec3::ZERO,
            animation_rotate: Quat::IDENTITY,
            ik_rotate: Quat::IDENTITY,
            initial_translate: Vec3::ZERO,
            initial_rotate: Quat::IDENTITY,
            local_transform: Mat4::IDENTITY,
            global_transform: Mat4::IDENTITY,
        }
    }
    
    /// 从 PMX 骨骼数据创建
    pub fn from_pmx_bone(pmx: &pmx_bone::Bone<DefaultConfig>) -> Self {
        use mmd::pmx::bone::BoneFlags;
        
        let flags = pmx.bone_flags;
        // MMD使用左手坐标系，翻转Z轴转换为右手坐标系
        let position = Vec3::new(
            pmx.position[0],
            pmx.position[1],
            -pmx.position[2],
        );
        
        let mut bone = Self::new(pmx.local_name.clone());
        bone.parent_index = pmx.parent;
        bone.transform_level = pmx.transform_level;
        bone.initial_position = position;
        // inverse_bind_matrix 将在 BoneManager::build_hierarchy 中计算
        bone.inverse_bind_matrix = Mat4::IDENTITY;
        
        bone.is_rotatable = flags.contains(BoneFlags::Rotatable);
        bone.is_movable = flags.contains(BoneFlags::Movable);
        bone.is_ik = flags.contains(BoneFlags::InverseKinematics);
        bone.is_append_rotate = flags.contains(BoneFlags::AddRotation);
        bone.is_append_translate = flags.contains(BoneFlags::AddMovement);
        bone.is_append_local = flags.contains(BoneFlags::AddLocalDeform);
        bone.is_fixed_axis = flags.contains(BoneFlags::FixedAxis);
        bone.is_local_axis = flags.contains(BoneFlags::LocalAxis);
        bone.deform_after_physics = flags.contains(BoneFlags::PhysicalTransform);
        
        // 保存初始平移（用于 append local 计算）
        bone.initial_translate = position;
        
        // 附加变换
        if let Some(ref additional) = pmx.additional {
            bone.append_parent = additional.parent;
            bone.append_rate = additional.rate;
        }
        
        // 固定轴（Z轴翻转）
        if let Some(ref axis) = pmx.fixed_axis {
            bone.fixed_axis = Vec3::new(axis[0], axis[1], -axis[2]);
        }
        
        // 本地轴（Z轴翻转）
        if let Some(ref local) = pmx.local_axis {
            bone.local_axis_x = Vec3::new(local.x[0], local.x[1], -local.x[2]);
            bone.local_axis_z = Vec3::new(local.z[0], local.z[1], -local.z[2]);
        }
        
        // IK 角度限制坐标系转换（与 C++ saba PMXModel.cpp 第 664-666 行一致）
        // C++: limitMax = m_limitMin * vec3(-1); limitMin = m_limitMax * vec3(-1);
        // 即：所有分量取负，并交换 min/max
        if let Some(ref ik) = pmx.inverse_kinematics {
            let links: Vec<IkLink> = ik.links.iter().map(|link| {
                let (has_limits, limit_min, limit_max) = match link.limits {
                    Some((min, max)) => (
                        true,
                        Vec3::new(-max[0], -max[1], -max[2]),
                        Vec3::new(-min[0], -min[1], -min[2]),
                    ),
                    None => (false, Vec3::ZERO, Vec3::ZERO),
                };
                IkLink {
                    bone_index: link.ik_bone,
                    has_limits,
                    limit_min,
                    limit_max,
                }
            }).collect();
            
            bone.ik_config = Some(IkConfig {
                target_bone: ik.ik_bone,
                iterations: ik.iterations,
                limit_angle: ik.limit_angle,
                links,
            });
        }
        
        bone
    }
    
    /// 重置动画状态（对应 C++ MMDNode::BeginUpdateTransform）
    pub fn reset_animation(&mut self) {
        self.animation_translate = Vec3::ZERO;
        self.animation_rotate = Quat::IDENTITY;
        self.ik_rotate = Quat::IDENTITY;
        self.append_translate = Vec3::ZERO;
        self.append_rotate = Quat::IDENTITY;
    }
    
    /// 更新本地变换（对应 C++ PMXNode::OnUpdateLocalTransform）
    /// 与 C++ saba 一致：
    /// 1. 平移 = bone_offset + animation_translate + append_translate
    /// 2. 旋转 = ik_rotate * animation_rotate * append_rotate
    pub fn update_local_transform(&mut self) {
        // 计算平移
        let mut translate = self.bone_offset + self.animation_translate;
        if self.is_append_translate {
            translate += self.append_translate;
        }
        
        // 计算旋转：与 C++ 一致 r = ik_rotate * animation_rotate * append_rotate
        let mut rotation = self.animation_rotate;
        if self.enable_ik {
            rotation = self.ik_rotate * rotation;
        }
        if self.is_append_rotate {
            rotation = rotation * self.append_rotate;
        }
        
        self.local_transform = Mat4::from_rotation_translation(rotation, translate);
    }
    
    /// 获取蒙皮矩阵 = 当前全局变换 * 逆绑定矩阵
    pub fn get_skinning_matrix(&self) -> Mat4 {
        self.global_transform * self.inverse_bind_matrix
    }
}

impl Default for Bone {
    fn default() -> Self {
        Self::new(String::new())
    }
}
