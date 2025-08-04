package stream_consumer

import (
	"context"
	"fmt"
	"log"
	"time"

	"drama-go/pkg/redis"
)

// MessageHandler 消息处理函数类型
type MessageHandler func(ctx context.Context, message redis.StreamMessage) error

// BaseProcessor 处理器基类
type BaseProcessor struct {
	consumer          *StreamConsumer
	config            QueueConfig
	handleMessageFunc MessageHandler
}

// NewBaseProcessor 创建基础处理器
func NewBaseProcessor(consumer *StreamConsumer, config QueueConfig) *BaseProcessor {
	return &BaseProcessor{
		consumer: consumer,
		config:   config,
	}
}

// SetMessageHandler 设置消息处理函数
func (bp *BaseProcessor) SetMessageHandler(handler MessageHandler) {
	bp.handleMessageFunc = handler
}

// Process 基础处理逻辑
func (bp *BaseProcessor) Process(ctx context.Context, message redis.StreamMessage) error {
	// 记录开始处理
	log.Printf("[%s] Processing message: %s", bp.config.StreamName, message.ID)

	// 设置超时上下文
	timeoutCtx, cancel := context.WithTimeout(ctx, time.Duration(bp.config.Timeout)*time.Second)
	defer cancel()

	// 重试机制
	var lastErr error
	for attempt := 0; attempt <= bp.config.MaxRetries; attempt++ {
		if attempt > 0 {
			log.Printf("[%s] Retry attempt %d for message: %s", bp.config.StreamName, attempt, message.ID)
			time.Sleep(time.Duration(attempt) * time.Second) // 指数退避
		}

		// 调用具体的处理逻辑
		var err error
		if bp.handleMessageFunc != nil {
			err = bp.handleMessageFunc(timeoutCtx, message)
		} else {
			err = bp.handleMessage(timeoutCtx, message)
		}

		if err != nil {
			lastErr = err
			log.Printf("[%s] Error processing message %s (attempt %d): %v",
				bp.config.StreamName, message.ID, attempt+1, err)
			continue
		}

		// 处理成功
		log.Printf("[%s] Successfully processed message: %s", bp.config.StreamName, message.ID)
		return nil
	}

	// 所有重试都失败了
	log.Printf("[%s] Failed to process message %s after %d attempts: %v",
		bp.config.StreamName, message.ID, bp.config.MaxRetries+1, lastErr)

	// 记录失败日志
	bp.LogError("Message processing failed", map[string]interface{}{
		"stream_name": bp.config.StreamName,
		"message_id":  message.ID,
		"attempts":    bp.config.MaxRetries + 1,
		"error":       lastErr.Error(),
		"data":        message.Values,
	})

	return fmt.Errorf("failed to process message after %d attempts: %w", bp.config.MaxRetries+1, lastErr)
}

// handleMessage 具体的消息处理逻辑，子类需要实现
func (bp *BaseProcessor) handleMessage(ctx context.Context, message redis.StreamMessage) error {
	// 默认实现，子类应该重写这个方法
	return fmt.Errorf("handleMessage not implemented")
}

// LogError 记录错误日志
func (bp *BaseProcessor) LogError(message string, data map[string]interface{}) {
	log.Printf("ERROR [%s]: %s - %+v", bp.config.StreamName, message, data)

	// 这里可以添加具体的日志记录逻辑
	// 例如：写入数据库、发送到日志服务等
}

// LogInfo 记录信息日志
func (bp *BaseProcessor) LogInfo(message string, data map[string]interface{}) {
	log.Printf("INFO [%s]: %s - %+v", bp.config.StreamName, message, data)
}

// LogDebug 记录调试日志
func (bp *BaseProcessor) LogDebug(message string, data map[string]interface{}) {
	log.Printf("DEBUG [%s]: %s - %+v", bp.config.StreamName, message, data)
}

// PublishMessage 发布消息到其他队列
func (bp *BaseProcessor) PublishMessage(ctx context.Context, streamName string, message map[string]interface{}) (string, error) {
	return bp.consumer.PublishMessage(streamName, message)
}

// GetConfig 获取配置
func (bp *BaseProcessor) GetConfig() QueueConfig {
	return bp.config
}

// GetConsumer 获取消费者
func (bp *BaseProcessor) GetConsumer() *StreamConsumer {
	return bp.consumer
}
