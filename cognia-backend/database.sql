-- 创建数据库
CREATE DATABASE IF NOT EXISTS cognia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cognia;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) COMMENT '密码',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `learning_type` VARCHAR(50) COMMENT '学习类型',
    `emotion_state` VARCHAR(50) COMMENT '情绪状态',
    `study_level` INT DEFAULT 1 COMMENT '学习等级',
    `continuous_days` INT DEFAULT 0 COMMENT '连续学习天数',
    `today_focus_time` DOUBLE DEFAULT 0 COMMENT '今日专注时长(小时)',
    `status` INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `deleted` INT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学习人格表
CREATE TABLE IF NOT EXISTS `learning_dna` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `dna_type` VARCHAR(50) COMMENT '人格类型',
    `sub_type` VARCHAR(50) COMMENT '子类型',
    `tags` VARCHAR(255) COMMENT '标签，逗号分隔',
    `strengths` TEXT COMMENT '优势',
    `weaknesses` TEXT COMMENT '弱点',
    `suggestions` TEXT COMMENT '建议',
    `understanding` INT DEFAULT 0 COMMENT '理解能力',
    `memory` INT DEFAULT 0 COMMENT '记忆能力',
    `focus` INT DEFAULT 0 COMMENT '专注持久度',
    `execution` INT DEFAULT 0 COMMENT '计划执行力',
    `emotion` INT DEFAULT 0 COMMENT '情绪稳定度',
    `logic` INT DEFAULT 0 COMMENT '逻辑思维',
    `deleted` INT DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习人格表';

-- 学习记录表
CREATE TABLE IF NOT EXISTS `study_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `subject` VARCHAR(50) COMMENT '学科',
    `duration` INT COMMENT '学习时长(分钟)',
    `score` INT COMMENT '得分',
    `focus_level` INT COMMENT '专注度',
    `content` TEXT COMMENT '学习内容',
    `study_date` DATE COMMENT '学习日期',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `deleted` INT DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_study_date` (`study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 错题表
CREATE TABLE IF NOT EXISTS `mistake` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `subject` VARCHAR(50) COMMENT '学科',
    `mistake_type` VARCHAR(50) COMMENT '错误类型',
    `error_count` INT DEFAULT 1 COMMENT '错误次数',
    `difficulty` VARCHAR(20) COMMENT '难度',
    `ai_analysis` TEXT COMMENT 'AI分析',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待复习，reviewing-复习中，mastered-已掌握',
    `image_url` VARCHAR(255) COMMENT '图片URL',
    `deleted` INT DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_subject` (`subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题表';

-- 情绪记录表
CREATE TABLE IF NOT EXISTS `emotion_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `emotion_type` VARCHAR(50) COMMENT '情绪类型',
    `emotion_label` VARCHAR(50) COMMENT '情绪标签',
    `content` TEXT COMMENT '内容',
    `related_activity` VARCHAR(100) COMMENT '相关活动',
    `ai_response` TEXT COMMENT 'AI回复',
    `record_date` DATE COMMENT '记录日期',
    `emotion_score` INT COMMENT '情绪分数(1-5)',
    `deleted` INT DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪记录表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `avatar`, `learning_type`, `emotion_state`, `study_level`, `continuous_days`, `today_focus_time`) VALUES
('小明同学', '123456', '', '理解驱动型', '专注', 12, 7, 4.2);

INSERT INTO `learning_dna` (`user_id`, `dna_type`, `sub_type`, `tags`, `strengths`, `weaknesses`, `suggestions`, `understanding`, `memory`, `focus`, `execution`, `emotion`, `logic`) VALUES
(1, '理解驱动型', '夜间高效型', '理解驱动型,夜间高效型,焦虑型学习者', '理解速度快,图像记忆强', '容易拖延,长时间学习效率下降', '25分钟番茄钟,晚间学习最佳,先理解后刷题', 85, 72, 65, 58, 70, 78);

INSERT INTO `study_record` (`user_id`, `subject`, `duration`, `score`, `focus_level`, `content`, `study_date`, `start_time`, `end_time`) VALUES
(1, '高等数学', 60, 85, 82, '定积分应用练习', '2024-01-21', '2024-01-21 19:00:00', '2024-01-21 20:00:00'),
(1, '英语', 40, 78, 75, '阅读理解训练', '2024-01-21', '2024-01-21 20:30:00', '2024-01-21 21:10:00'),
(1, '线性代数', 45, 72, 70, '矩阵运算专项', '2024-01-20', '2024-01-20 19:00:00', '2024-01-20 19:45:00');

INSERT INTO `mistake` (`user_id`, `title`, `content`, `subject`, `mistake_type`, `error_count`, `difficulty`, `status`) VALUES
(1, '定积分求面积问题', '在计算定积分时，没有正确找出两个函数的交点', '高等数学', '概念混淆', 2, '中等', 'pending'),
(1, '矩阵乘法计算', '在进行矩阵乘法时，行与列的对应元素相乘后相加的计算过程中出现错误', '线性代数', '计算错误', 3, '简单', 'reviewing'),
(1, '英语阅读理解', '对文章主旨的把握出现偏差', '英语', '理解偏差', 1, '困难', 'pending');

INSERT INTO `emotion_record` (`user_id`, `emotion_type`, `emotion_label`, `content`, `related_activity`, `ai_response`, `record_date`, `emotion_score`) VALUES
(1, 'good', '还不错', '今天学习有点累，但是收获也很大！', '高数学习', '太棒了！你的努力正在积累成果。', '2024-01-21', 4),
(1, 'tired', '有点累', '连续学了3个小时，感觉有点疲惫', '英语学习', '学习疲劳是正常的！建议采用番茄工作法。', '2024-01-20', 2),
(1, 'great', '超棒', '终于搞懂了矩阵乘法的原理！', '线代学习', '恭喜你！这种顿悟的感觉是最美妙的。', '2024-01-19', 5);
