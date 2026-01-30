//! VMD 文件解析

use std::io::{Read, BufReader};
use std::fs::File;
use std::path::Path;
use byteorder::{LittleEndian, ReadBytesExt};
use glam::{Vec3, Quat};

use crate::{Result, MmdError};
use super::{BoneKeyframe, MorphKeyframe, BezierCurve};

/// VMD 文件数据
#[derive(Clone, Debug)]
pub struct VmdFile {
    pub model_name: String,
    pub bone_keyframes: Vec<(String, BoneKeyframe)>,
    pub morph_keyframes: Vec<(String, MorphKeyframe)>,
}

impl VmdFile {
    /// 从文件加载 VMD
    pub fn load<P: AsRef<Path>>(path: P) -> Result<Self> {
        let file = File::open(path).map_err(MmdError::Io)?;
        let mut reader = BufReader::new(file);
        Self::parse(&mut reader)
    }
    
    /// 解析 VMD 数据
    fn parse<R: Read>(reader: &mut R) -> Result<Self> {
        // 读取魔数
        let mut magic = [0u8; 30];
        reader.read_exact(&mut magic).map_err(MmdError::Io)?;
        
        let magic_str = String::from_utf8_lossy(&magic);
        if !magic_str.starts_with("Vocaloid Motion Data") {
            return Err(MmdError::VmdParse("Invalid VMD magic".to_string()));
        }
        
        // 读取模型名
        let mut model_name_bytes = [0u8; 20];
        reader.read_exact(&mut model_name_bytes).map_err(MmdError::Io)?;
        let model_name = decode_shift_jis(&model_name_bytes);
        
        // 读取骨骼关键帧
        let bone_count = reader.read_u32::<LittleEndian>().map_err(MmdError::Io)?;
        let mut bone_keyframes = Vec::with_capacity(bone_count as usize);
        
        for _ in 0..bone_count {
            let mut name_bytes = [0u8; 15];
            reader.read_exact(&mut name_bytes).map_err(MmdError::Io)?;
            let bone_name = decode_shift_jis(&name_bytes);
            
            let frame = reader.read_u32::<LittleEndian>().map_err(MmdError::Io)?;
            
            let tx = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            let ty = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            let tz = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            
            let rx = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            let ry = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            let rz = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            let rw = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            
            // 读取插值数据 (64 bytes)
            let mut interp = [0u8; 64];
            reader.read_exact(&mut interp).map_err(MmdError::Io)?;
            
            let mut keyframe = BoneKeyframe::new(frame);
            keyframe.translation = Vec3::new(tx, ty, -tz); // Z 轴反转
            keyframe.rotation = Quat::from_xyzw(rx, ry, -rz, -rw); // Z 轴反转
            
            // 解析插值曲线
            keyframe.interp_x = BezierCurve::from_vmd_data(&[interp[0], interp[4], interp[8], interp[12]]);
            keyframe.interp_y = BezierCurve::from_vmd_data(&[interp[1], interp[5], interp[9], interp[13]]);
            keyframe.interp_z = BezierCurve::from_vmd_data(&[interp[2], interp[6], interp[10], interp[14]]);
            keyframe.interp_rotation = BezierCurve::from_vmd_data(&[interp[3], interp[7], interp[11], interp[15]]);
            
            bone_keyframes.push((bone_name, keyframe));
        }
        
        // 读取 Morph 关键帧
        let morph_count = reader.read_u32::<LittleEndian>().map_err(MmdError::Io)?;
        let mut morph_keyframes = Vec::with_capacity(morph_count as usize);
        
        for _ in 0..morph_count {
            let mut name_bytes = [0u8; 15];
            reader.read_exact(&mut name_bytes).map_err(MmdError::Io)?;
            let morph_name = decode_shift_jis(&name_bytes);
            
            let frame = reader.read_u32::<LittleEndian>().map_err(MmdError::Io)?;
            let weight = reader.read_f32::<LittleEndian>().map_err(MmdError::Io)?;
            
            morph_keyframes.push((morph_name, MorphKeyframe::new(frame, weight)));
        }
        
        Ok(Self {
            model_name,
            bone_keyframes,
            morph_keyframes,
        })
    }
}

/// 解码 Shift-JIS 字符串
fn decode_shift_jis(bytes: &[u8]) -> String {
    use encoding_rs::SHIFT_JIS;
    
    // 找到 null 终止符
    let end = bytes.iter().position(|&b| b == 0).unwrap_or(bytes.len());
    let (decoded, _, _) = SHIFT_JIS.decode(&bytes[..end]);
    decoded.to_string()
}
