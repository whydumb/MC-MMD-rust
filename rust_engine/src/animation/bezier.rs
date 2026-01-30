//! 贝塞尔曲线插值

/// 贝塞尔曲线（用于 VMD 动画插值）
#[derive(Clone, Debug, Default)]
pub struct BezierCurve {
    pub x1: f32,
    pub y1: f32,
    pub x2: f32,
    pub y2: f32,
}

impl BezierCurve {
    pub fn new(x1: f32, y1: f32, x2: f32, y2: f32) -> Self {
        Self { x1, y1, x2, y2 }
    }
    
    /// 线性插值
    pub fn linear() -> Self {
        Self::new(0.25, 0.25, 0.75, 0.75)
    }
    
    /// 从 VMD 插值数据创建
    pub fn from_vmd_data(data: &[u8; 4]) -> Self {
        Self {
            x1: data[0] as f32 / 127.0,
            y1: data[1] as f32 / 127.0,
            x2: data[2] as f32 / 127.0,
            y2: data[3] as f32 / 127.0,
        }
    }
    
    /// 评估贝塞尔曲线
    pub fn evaluate(&self, t: f32) -> f32 {
        if t <= 0.0 {
            return 0.0;
        }
        if t >= 1.0 {
            return 1.0;
        }
        
        // 使用牛顿法求解 x(s) = t 的 s 值
        let mut s = t;
        for _ in 0..15 {
            let x = self.bezier_x(s);
            let dx = self.bezier_dx(s);
            
            if dx.abs() < 1e-6 {
                break;
            }
            
            let new_s = s - (x - t) / dx;
            if (new_s - s).abs() < 1e-6 {
                break;
            }
            s = new_s;
        }
        
        self.bezier_y(s)
    }
    
    fn bezier_x(&self, s: f32) -> f32 {
        let s2 = s * s;
        let s3 = s2 * s;
        let t = 1.0 - s;
        let t2 = t * t;
        
        3.0 * t2 * s * self.x1 + 3.0 * t * s2 * self.x2 + s3
    }
    
    fn bezier_y(&self, s: f32) -> f32 {
        let s2 = s * s;
        let s3 = s2 * s;
        let t = 1.0 - s;
        let t2 = t * t;
        
        3.0 * t2 * s * self.y1 + 3.0 * t * s2 * self.y2 + s3
    }
    
    fn bezier_dx(&self, s: f32) -> f32 {
        let s2 = s * s;
        let t = 1.0 - s;
        
        3.0 * t * t * self.x1 + 6.0 * t * s * (self.x2 - self.x1) + 3.0 * s2 * (1.0 - self.x2)
    }
}
