//! 动画句柄

use std::sync::Arc;
use crate::animation::VmdAnimation;

/// 动画句柄包装
pub struct AnimationHandle {
    pub id: i64,
    pub animation: Arc<VmdAnimation>,
}

impl AnimationHandle {
    pub fn new(id: i64, animation: VmdAnimation) -> Self {
        Self {
            id,
            animation: Arc::new(animation),
        }
    }
}
