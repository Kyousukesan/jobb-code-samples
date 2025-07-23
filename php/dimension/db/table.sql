/** 纬度模块表 **/
CREATE TABLE `kj_hw_drama_backend`.`dimension_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dimension_name` varchar(100) NOT NULL DEFAULT '' COMMENT '维度名称,不直接暴露的名称最好带上时间戳',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '纬度类型,1-用户推送',
  `is_enable` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`dimension_name`),
  KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹窗配置表';

CREATE TABLE `kj_hw_drama_backend`.`dimension_config_condition` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dimension_config_id` int NOT NULL DEFAULT '0' COMMENT '维度主表ID',
    `condition_group` tinyint NOT NULL DEFAULT '0' COMMENT '条件分组',
    `condition_group_logic` varchar(10) NOT NULL DEFAULT '' COMMENT '条件分组逻辑 默认AND',
    `condition_logic` varchar(10) NOT NULL DEFAULT '' COMMENT '条件逻辑 EQ,NEQ,GTE,LTE...',
    `condition_name` varchar(30) NOT NULL DEFAULT '' COMMENT '条件名称,枚举映射',
    `condition_value` varchar(50) NOT NULL DEFAULT '' COMMENT '条件值单选值,多选在子表',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_condition` (`dimension_config_id`,`condition_group`,`condition_name`),
    KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度条件表';

CREATE TABLE `kj_hw_drama_backend`.`dimension_config_condition_value` (
      `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
      `dimension_config_condition_id` int NOT NULL DEFAULT '0' COMMENT '维度条件表ID',
      `condition_value` varchar(100) NOT NULL DEFAULT '' COMMENT '条件值',
      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
      PRIMARY KEY (`id`),
      KEY `idx_condition_id` (`dimension_config_condition_id`),
      KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度条件值表';