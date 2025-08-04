package stream_consumer

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

// QueueConfig 队列配置
type QueueConfig struct {
	StreamName string `json:"stream_name"`
	GroupName  string `json:"group_name"`
	ConsumerID string `json:"consumer_id"`
	AutoStart  bool   `json:"auto_start"`
	Enabled    bool   `json:"enabled"`
	MaxRetries int    `json:"max_retries"`
	BatchSize  int    `json:"batch_size"`
	Timeout    int    `json:"timeout"` // 秒
}

// ConfigManager 配置管理器
type ConfigManager struct {
	configPath string
	configs    map[string]QueueConfig
}

// NewConfigManager 创建配置管理器
func NewConfigManager(configPath string) *ConfigManager {
	return &ConfigManager{
		configPath: configPath,
		configs:    make(map[string]QueueConfig),
	}
}

// LoadConfigs 加载配置
func (cm *ConfigManager) LoadConfigs() error {
	// 读取配置文件
	data, err := ioutil.ReadFile(cm.configPath)
	if err != nil {
		return fmt.Errorf("failed to read config file: %w", err)
	}

	// 解析配置
	var configs []QueueConfig
	if err := json.Unmarshal(data, &configs); err != nil {
		return fmt.Errorf("failed to parse config file: %w", err)
	}

	// 加载到内存
	for _, config := range configs {
		if config.Enabled {
			cm.configs[config.StreamName] = config
			log.Printf("Loaded config for stream: %s", config.StreamName)
		}
	}

	log.Printf("Loaded %d queue configurations", len(cm.configs))
	return nil
}

// GetConfig 获取配置
func (cm *ConfigManager) GetConfig(streamName string) (QueueConfig, bool) {
	config, exists := cm.configs[streamName]
	return config, exists
}

// GetAllConfigs 获取所有配置
func (cm *ConfigManager) GetAllConfigs() map[string]QueueConfig {
	return cm.configs
}

// AddConfig 添加配置
func (cm *ConfigManager) AddConfig(config QueueConfig) {
	cm.configs[config.StreamName] = config
}

// RemoveConfig 移除配置
func (cm *ConfigManager) RemoveConfig(streamName string) {
	delete(cm.configs, streamName)
}

// SaveConfigs 保存配置到文件
func (cm *ConfigManager) SaveConfigs() error {
	// 转换为数组
	configs := make([]QueueConfig, 0, len(cm.configs))
	for _, config := range cm.configs {
		configs = append(configs, config)
	}

	// 序列化
	data, err := json.MarshalIndent(configs, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal configs: %w", err)
	}

	// 确保目录存在
	dir := filepath.Dir(cm.configPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create config directory: %w", err)
	}

	// 写入文件
	if err := ioutil.WriteFile(cm.configPath, data, 0644); err != nil {
		return fmt.Errorf("failed to write config file: %w", err)
	}

	log.Printf("Saved %d queue configurations to %s", len(configs), cm.configPath)
	return nil
}

// CreateDefaultConfig 创建默认配置文件
func (cm *ConfigManager) CreateDefaultConfig() error {
	defaultConfigs := []QueueConfig{
		{
			StreamName: "book_similarity",
			GroupName:  "book_similarity_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
		{
			StreamName: "user_events",
			GroupName:  "user_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
		{
			StreamName: "system_events",
			GroupName:  "system_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
		{
			StreamName: "notifications",
			GroupName:  "notification_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
		{
			StreamName: "orders",
			GroupName:  "order_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
		{
			StreamName: "payments",
			GroupName:  "payment_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
			Enabled:    true,
			MaxRetries: 3,
			BatchSize:  10,
			Timeout:    30,
		},
	}

	// 序列化
	data, err := json.MarshalIndent(defaultConfigs, "", "  ")
	if err != nil {
		return fmt.Errorf("failed to marshal default configs: %w", err)
	}

	// 确保目录存在
	dir := filepath.Dir(cm.configPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return fmt.Errorf("failed to create config directory: %w", err)
	}

	// 写入文件
	if err := ioutil.WriteFile(cm.configPath, data, 0644); err != nil {
		return fmt.Errorf("failed to write default config file: %w", err)
	}

	log.Printf("Created default config file: %s", cm.configPath)
	return nil
}
