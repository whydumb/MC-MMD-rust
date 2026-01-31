//! VPD (Vocaloid Pose Data) 文件解析
//!
//! VPD 是 Shift-JIS 编码的文本文件，包含骨骼位姿和 Morph 表情数据。
//! Morph 数据是 MMM (MikuMikuMoving) 的扩展，但被广泛支持。

use std::fs;
use std::path::Path;
use glam::{Vec3, Quat};

use crate::{Result, MmdError};

/// VPD 骨骼数据
#[derive(Clone, Debug)]
pub struct VpdBone {
    pub name: String,
    pub translation: Vec3,
    pub rotation: Quat,
}

/// VPD Morph 数据
#[derive(Clone, Debug)]
pub struct VpdMorph {
    pub name: String,
    pub weight: f32,
}

/// VPD 文件数据
#[derive(Clone, Debug)]
pub struct VpdFile {
    pub model_name: String,
    pub bones: Vec<VpdBone>,
    pub morphs: Vec<VpdMorph>,
}

impl VpdFile {
    /// 从文件加载 VPD
    pub fn load<P: AsRef<Path>>(path: P) -> Result<Self> {
        let bytes = fs::read(path).map_err(MmdError::Io)?;
        Self::parse(&bytes)
    }
    
    /// 解析 VPD 数据
    fn parse(bytes: &[u8]) -> Result<Self> {
        // VPD 使用 Shift-JIS 编码
        let content = decode_shift_jis(bytes);
        
        // 验证文件头
        if !content.starts_with("Vocaloid Pose Data file") {
            return Err(MmdError::VpdParse("Invalid VPD header".to_string()));
        }
        
        let mut model_name = String::new();
        let mut bones = Vec::new();
        let mut morphs = Vec::new();
        
        let lines: Vec<&str> = content.lines().collect();
        let mut i = 0;
        
        while i < lines.len() {
            let line = lines[i].trim();
            
            // 跳过空行和注释
            if line.is_empty() || line.starts_with("//") {
                i += 1;
                continue;
            }
            
            // 解析模型名称（第二行，以 .osm 结尾）
            if line.ends_with(".osm;") || line.ends_with(".pmx;") || line.ends_with(".pmd;") {
                model_name = line.trim_end_matches(';').to_string();
                i += 1;
                continue;
            }
            
            // 解析骨骼数量（格式：数字;）
            if line.ends_with(';') && !line.contains('{') {
                // 这可能是骨骼数量行，跳过
                i += 1;
                continue;
            }
            
            // 解析骨骼数据块
            if line.starts_with("Bone") && line.contains('{') {
                if let Some(bone) = Self::parse_bone_block(&lines, &mut i) {
                    bones.push(bone);
                }
                continue;
            }
            
            // 解析 Morph 数据块
            if line.starts_with("Morph") && line.contains('{') {
                if let Some(morph) = Self::parse_morph_block(&lines, &mut i) {
                    morphs.push(morph);
                }
                continue;
            }
            
            i += 1;
        }
        
        log::info!("VPD 解析完成: {} 个骨骼, {} 个表情", bones.len(), morphs.len());
        
        Ok(Self {
            model_name,
            bones,
            morphs,
        })
    }
    
    /// 解析骨骼数据块
    fn parse_bone_block(lines: &[&str], index: &mut usize) -> Option<VpdBone> {
        let start_line = lines[*index].trim();
        *index += 1;
        
        // 提取骨骼名称（在 { 后面的下一行，或同一行）
        let name = if start_line.contains('{') && !start_line.ends_with('{') {
            // 格式: Bone0{骨骼名
            let after_brace = start_line.split('{').nth(1)?;
            after_brace.trim().to_string()
        } else {
            // 名称在下一行
            if *index >= lines.len() {
                return None;
            }
            let name_line = lines[*index].trim();
            *index += 1;
            name_line.to_string()
        };
        
        // 读取位移数据
        let mut translation = Vec3::ZERO;
        let mut rotation = Quat::IDENTITY;
        
        while *index < lines.len() {
            let line = lines[*index].trim();
            *index += 1;
            
            if line.starts_with('}') {
                break;
            }
            
            // 解析位移: x,y,z; // trans x,y,z
            if line.contains("trans") || (line.contains(',') && !line.contains("Quaternion")) {
                if let Some(trans) = Self::parse_vec3(line) {
                    translation = trans;
                }
            }
            // 解析旋转: x,y,z,w; // Quaternion x,y,z,w
            else if line.contains("Quaternion") || line.split(',').count() >= 4 {
                if let Some(quat) = Self::parse_quat(line) {
                    rotation = quat;
                }
            }
        }
        
        Some(VpdBone {
            name,
            translation,
            rotation,
        })
    }
    
    /// 解析 Morph 数据块
    fn parse_morph_block(lines: &[&str], index: &mut usize) -> Option<VpdMorph> {
        let start_line = lines[*index].trim();
        *index += 1;
        
        // 提取 Morph 名称
        let name = if start_line.contains('{') && !start_line.ends_with('{') {
            let after_brace = start_line.split('{').nth(1)?;
            after_brace.trim().to_string()
        } else {
            if *index >= lines.len() {
                return None;
            }
            let name_line = lines[*index].trim();
            *index += 1;
            name_line.to_string()
        };
        
        // 读取权重
        let mut weight = 0.0f32;
        
        while *index < lines.len() {
            let line = lines[*index].trim();
            *index += 1;
            
            if line.starts_with('}') {
                break;
            }
            
            // 解析权重: 0.500000;
            if let Some(w) = Self::parse_weight(line) {
                weight = w;
            }
        }
        
        Some(VpdMorph { name, weight })
    }
    
    /// 解析 Vec3 数据
    fn parse_vec3(line: &str) -> Option<Vec3> {
        // 格式: 0.000000,0.000000,0.000000; // trans x,y,z
        let clean = line.split("//").next()?.trim().trim_end_matches(';');
        let parts: Vec<&str> = clean.split(',').collect();
        
        if parts.len() >= 3 {
            let x = parts[0].trim().parse::<f32>().ok()?;
            let y = parts[1].trim().parse::<f32>().ok()?;
            let z = parts[2].trim().parse::<f32>().ok()?;
            return Some(Vec3::new(x, y, -z)); // Z 轴反转
        }
        None
    }
    
    /// 解析 Quaternion 数据
    fn parse_quat(line: &str) -> Option<Quat> {
        // 格式: 0.176789,-0.061290,0.747712,0.637114; // Quaternion x,y,z,w
        let clean = line.split("//").next()?.trim().trim_end_matches(';');
        let parts: Vec<&str> = clean.split(',').collect();
        
        if parts.len() >= 4 {
            let x = parts[0].trim().parse::<f32>().ok()?;
            let y = parts[1].trim().parse::<f32>().ok()?;
            let z = parts[2].trim().parse::<f32>().ok()?;
            let w = parts[3].trim().parse::<f32>().ok()?;
            return Some(Quat::from_xyzw(x, y, -z, -w)); // Z 轴反转
        }
        None
    }
    
    /// 解析权重值
    fn parse_weight(line: &str) -> Option<f32> {
        // 格式: 0.500000; 或 0;
        let clean = line.split("//").next()?.trim().trim_end_matches(';');
        clean.parse::<f32>().ok()
    }
    
    /// 获取 Morph 数量
    pub fn morph_count(&self) -> usize {
        self.morphs.len()
    }
    
    /// 获取骨骼数量
    pub fn bone_count(&self) -> usize {
        self.bones.len()
    }
}

/// 解码 Shift-JIS 字符串
fn decode_shift_jis(bytes: &[u8]) -> String {
    use encoding_rs::SHIFT_JIS;
    let (decoded, _, _) = SHIFT_JIS.decode(bytes);
    decoded.to_string()
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_parse_vec3() {
        let line = "0.1,0.2,0.3; // trans x,y,z";
        let v = VpdFile::parse_vec3(line).unwrap();
        assert!((v.x - 0.1).abs() < 0.001);
        assert!((v.y - 0.2).abs() < 0.001);
        assert!((v.z + 0.3).abs() < 0.001); // Z 反转
    }
    
    #[test]
    fn test_parse_weight() {
        assert_eq!(VpdFile::parse_weight("0.5;"), Some(0.5));
        assert_eq!(VpdFile::parse_weight("1;"), Some(1.0));
        assert_eq!(VpdFile::parse_weight("0.123456;"), Some(0.123456));
    }
}
