/** ディメンションモジュールテーブル **/
CREATE TABLE `kj_hw_drama_backend`.`dimension_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主キーID',
  `dimension_name` varchar(100) NOT NULL DEFAULT '' COMMENT 'ディメンション名、直接公開されない名前はタイムスタンプを含めることを推奨',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT 'ディメンションタイプ、1-ユーザープッシュ',
  `is_enable` tinyint NOT NULL DEFAULT '1' COMMENT '有効かどうか',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成時間',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '削除済みかどうか',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`dimension_name`),
  KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ポップアップ設定テーブル';

CREATE TABLE `kj_hw_drama_backend`.`dimension_config_condition` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主キーID',
    `dimension_config_id` int NOT NULL DEFAULT '0' COMMENT 'ディメンション主テーブルID',
    `condition_group` tinyint NOT NULL DEFAULT '0' COMMENT '条件グループ',
    `condition_group_logic` varchar(10) NOT NULL DEFAULT '' COMMENT '条件グループ論理 デフォルトAND',
    `condition_logic` varchar(10) NOT NULL DEFAULT '' COMMENT '条件論理 EQ,NEQ,GTE,LTE...',
    `condition_name` varchar(30) NOT NULL DEFAULT '' COMMENT '条件名、列挙型マッピング',
    `condition_value` varchar(50) NOT NULL DEFAULT '' COMMENT '条件値単一選択値、複数選択は子テーブル',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成時間',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '削除済みかどうか',
    PRIMARY KEY (`id`),
    KEY `idx_condition` (`dimension_config_id`,`condition_group`,`condition_name`),
    KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ディメンション条件テーブル';

CREATE TABLE `kj_hw_drama_backend`.`dimension_config_condition_value` (
      `id` int NOT NULL AUTO_INCREMENT COMMENT '主キーID',
      `dimension_config_condition_id` int NOT NULL DEFAULT '0' COMMENT 'ディメンション条件テーブルID',
      `condition_value` varchar(100) NOT NULL DEFAULT '' COMMENT '条件値',
      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作成時間',
      `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
      `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '削除済みかどうか',
      PRIMARY KEY (`id`),
      KEY `idx_condition_id` (`dimension_config_condition_id`),
      KEY `idx_update_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ディメンション条件値テーブル';