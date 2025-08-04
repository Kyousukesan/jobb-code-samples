package stream_consumer

import (
	"context"
	"fmt"
	"log"
	"sync"

	"drama-go/pkg/redis"

	redisClient "github.com/redis/go-redis/v9"
)

// StreamConsumer Stream消费者
type StreamConsumer struct {
	client        *redis.Client
	manager       interface{} // TODO: 暂时使用interface{}，因为stream_handler包不存在
	ctx           context.Context
	cancel        context.CancelFunc
	wg            sync.WaitGroup
	streamConfigs map[string]StreamConfig
	configManager *ConfigManager
	registry      *ProcessorRegistry
}

// StreamConfig Stream配置
type StreamConfig struct {
	StreamName string `json:"stream_name"`
	GroupName  string `json:"group_name"`
	ConsumerID string `json:"consumer_id"`
	AutoStart  bool   `json:"auto_start"`
}

// NewStreamConsumer 创建Stream消费者
func NewStreamConsumer(client *redis.Client) *StreamConsumer {
	ctx, cancel := context.WithCancel(context.Background())

	return &StreamConsumer{
		client:        client,
		ctx:           ctx,
		cancel:        cancel,
		streamConfigs: make(map[string]StreamConfig),
		configManager: NewConfigManager("config/queues.json"),
		registry:      NewProcessorRegistry(),
	}
}

// AddStreamConfig 添加Stream配置
func (c *StreamConsumer) AddStreamConfig(config StreamConfig) {
	c.streamConfigs[config.StreamName] = config
}

// SetDefaultConfigs 设置默认配置
func (c *StreamConsumer) SetDefaultConfigs() {
	defaultConfigs := []StreamConfig{
		{
			StreamName: "book_similarity",
			GroupName:  "book_similarity_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
		{
			StreamName: "user_events",
			GroupName:  "user_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
		{
			StreamName: "system_events",
			GroupName:  "system_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
		{
			StreamName: "notifications",
			GroupName:  "notification_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
		{
			StreamName: "orders",
			GroupName:  "order_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
		{
			StreamName: "payments",
			GroupName:  "payment_group",
			ConsumerID: "consumer_1",
			AutoStart:  true,
		},
	}

	for _, config := range defaultConfigs {
		c.AddStreamConfig(config)
	}
}

// Start 启动消费者
func (c *StreamConsumer) Start() error {
	log.Println("Starting stream consumers...")

	// 加载配置文件
	if err := c.configManager.LoadConfigs(); err != nil {
		log.Printf("Failed to load configs, creating default config: %v", err)
		if err := c.configManager.CreateDefaultConfig(); err != nil {
			return fmt.Errorf("failed to create default config: %w", err)
		}
		if err := c.configManager.LoadConfigs(); err != nil {
			return fmt.Errorf("failed to load default config: %w", err)
		}
	}

	// 注册所有处理器（在主程序中调用）

	// 从配置管理器获取所有配置
	configs := c.configManager.GetAllConfigs()

	// 创建所有配置的Stream处理器
	for streamName, config := range configs {
		// 检查是否有注册的处理器
		if !c.registry.HasProcessor(streamName) {
			log.Printf("No processor registered for stream: %s, skipping", streamName)
			continue
		}

		// 获取处理器
		processor, err := c.registry.GetProcessor(streamName, c)
		if err != nil {
			log.Printf("Failed to get processor for stream %s: %v", streamName, err)
			continue
		}

		// TODO: 创建Stream管理器（如果还没有创建）
		// if c.manager == nil {
		// 	c.manager = stream_handler.NewStreamManager(c.client)
		// }

		// 确保Stream和消费者组存在
		if err := c.ensureStreamAndGroup(streamName, config.GroupName); err != nil {
			log.Printf("Failed to ensure stream and group for %s: %v", streamName, err)
			continue
		}

		// 创建Redis Stream消费者
		redisConsumer := redis.NewStreamConsumer(c.client, config.GroupName, config.ConsumerID)

		// 创建工作器
		worker := redis.NewStreamWorker(redisConsumer, processor, streamName)

		log.Printf("Created stream worker: %s (group: %s, consumer: %s)",
			streamName, config.GroupName, config.ConsumerID)

		// 如果配置为自动启动，则启动消费者
		if config.AutoStart {
			c.wg.Add(1)
			go func(name string, w *redis.StreamWorker) {
				defer c.wg.Done()
				log.Printf("Starting consumer for stream: %s", name)
				if err := w.Start(c.ctx); err != nil {
					log.Printf("Consumer for stream %s stopped: %v", name, err)
				}
			}(streamName, worker)
		}
	}

	log.Printf("Started %d stream consumers", len(configs))
	return nil
}

// Stop 停止消费者
func (c *StreamConsumer) Stop() {
	log.Println("Stopping stream consumers...")

	// 取消上下文
	c.cancel()

	// 等待所有goroutine结束
	c.wg.Wait()

	// TODO: 关闭Stream管理器
	// if c.manager != nil {
	// 	c.manager.Shutdown()
	// }

	log.Println("All stream consumers stopped")
}

// GetManager 获取Stream管理器
func (c *StreamConsumer) GetManager() interface{} {
	return c.manager
}

// PublishMessage 发布消息到指定Stream
func (c *StreamConsumer) PublishMessage(streamName string, message map[string]interface{}) (string, error) {
	// TODO: 实现发布消息功能
	return "", fmt.Errorf("stream manager not initialized")
}

// GetStreamInfo 获取Stream信息
func (c *StreamConsumer) GetStreamInfo(streamName string) (interface{}, error) {
	// TODO: 实现获取Stream信息功能
	return nil, fmt.Errorf("stream manager not initialized")
}

// ListStreams 列出所有Stream
func (c *StreamConsumer) ListStreams() []string {
	// TODO: 实现列出Stream功能
	return []string{}
}

// GetStreamCount 获取Stream数量
func (c *StreamConsumer) GetStreamCount() int {
	// TODO: 实现获取Stream数量功能
	return 0
}

// GetConfigManager 获取配置管理器
func (c *StreamConsumer) GetConfigManager() *ConfigManager {
	return c.configManager
}

// GetRegistry 获取处理器注册中心
func (c *StreamConsumer) GetRegistry() *ProcessorRegistry {
	return c.registry
}

// ensureStreamAndGroup 确保Stream和消费者组存在
func (c *StreamConsumer) ensureStreamAndGroup(streamName, groupName string) error {
	ctx := context.Background()

	// 检查Stream是否存在，如果不存在则创建
	exists, err := c.client.GetClient().Exists(ctx, streamName).Result()
	if err != nil {
		return fmt.Errorf("failed to check stream existence: %w", err)
	}

	if exists == 0 {
		// Stream不存在，创建一个空的消息来初始化Stream
		log.Printf("Creating stream: %s", streamName)
		_, err = c.client.GetClient().XAdd(ctx, &redisClient.XAddArgs{
			Stream: streamName,
			Values: map[string]interface{}{
				"init": "stream_created",
			},
		}).Result()
		if err != nil {
			return fmt.Errorf("failed to create stream: %w", err)
		}
		log.Printf("Stream created: %s", streamName)
	}

	// 检查消费者组是否存在，如果不存在则创建
	err = c.client.GetClient().XGroupCreate(ctx, streamName, groupName, "0").Err()
	if err != nil {
		// 如果错误不是"BUSYGROUP Consumer Group name already exists"，则返回错误
		if err.Error() != "BUSYGROUP Consumer Group name already exists" {
			return fmt.Errorf("failed to create consumer group: %w", err)
		}
		log.Printf("Consumer group already exists: %s", groupName)
	} else {
		log.Printf("Consumer group created: %s", groupName)
	}

	return nil
}
