package redis

import (
	"context"
	"log"
	"time"

	"github.com/redis/go-redis/v9"
)

// StreamMessage Stream消息结构
type StreamMessage struct {
	ID     string
	Values map[string]interface{}
}

// StreamProducer Stream生产者
type StreamProducer struct {
	client *Client
}

// NewStreamProducer 创建Stream生产者
func NewStreamProducer(client *Client) *StreamProducer {
	return &StreamProducer{client: client}
}

// Publish 发布消息到Stream
func (p *StreamProducer) Publish(ctx context.Context, stream string, values map[string]interface{}) (string, error) {
	args := make([]interface{}, 0, len(values)*2)
	for key, value := range values {
		args = append(args, key, value)
	}

	result := p.client.GetClient().XAdd(ctx, &redis.XAddArgs{
		Stream: stream,
		Values: args,
	})

	return result.Result()
}

// StreamConsumer Stream消费者
type StreamConsumer struct {
	client     *Client
	groupName  string
	consumerID string
}

// NewStreamConsumer 创建Stream消费者
func NewStreamConsumer(client *Client, groupName, consumerID string) *StreamConsumer {
	return &StreamConsumer{
		client:     client,
		groupName:  groupName,
		consumerID: consumerID,
	}
}

// CreateGroup 创建消费者组
func (c *StreamConsumer) CreateGroup(ctx context.Context, stream string, start string) error {
	return c.client.GetClient().XGroupCreate(ctx, stream, c.groupName, start).Err()
}

// ReadGroup 从消费者组读取消息
func (c *StreamConsumer) ReadGroup(ctx context.Context, stream string, count int64, block time.Duration) ([]StreamMessage, error) {
	result := c.client.GetClient().XReadGroup(ctx, &redis.XReadGroupArgs{
		Group:    c.groupName,
		Consumer: c.consumerID,
		Streams:  []string{stream, ">"},
		Count:    count,
		Block:    block,
	})

	streams, err := result.Result()
	if err != nil {
		return nil, err
	}

	var messages []StreamMessage
	for _, stream := range streams {
		for _, message := range stream.Messages {
			msg := StreamMessage{
				ID:     message.ID,
				Values: message.Values,
			}
			messages = append(messages, msg)
		}
	}

	return messages, nil
}

// Acknowledge 确认消息已处理
func (c *StreamConsumer) Acknowledge(ctx context.Context, stream string, messageIDs ...string) error {
	return c.client.GetClient().XAck(ctx, stream, c.groupName, messageIDs...).Err()
}

// Pending 获取待处理消息
func (c *StreamConsumer) Pending(ctx context.Context, stream string) (*redis.XPending, error) {
	return c.client.GetClient().XPending(ctx, stream, c.groupName).Result()
}

// ClaimPending 认领待处理消息
func (c *StreamConsumer) ClaimPending(ctx context.Context, stream string, minIdleTime time.Duration, messageIDs []string) ([]StreamMessage, error) {
	result := c.client.GetClient().XClaim(ctx, &redis.XClaimArgs{
		Stream:   stream,
		Group:    c.groupName,
		Consumer: c.consumerID,
		MinIdle:  minIdleTime,
		Messages: messageIDs,
	})

	messages, err := result.Result()
	if err != nil {
		return nil, err
	}

	var streamMessages []StreamMessage
	for _, message := range messages {
		msg := StreamMessage{
			ID:     message.ID,
			Values: message.Values,
		}
		streamMessages = append(streamMessages, msg)
	}

	return streamMessages, nil
}

// StreamManager Stream管理器
type StreamManager struct {
	client *Client
}

// NewStreamManager 创建Stream管理器
func NewStreamManager(client *Client) *StreamManager {
	return &StreamManager{client: client}
}

// CreateStream 创建Stream
func (m *StreamManager) CreateStream(ctx context.Context, stream string, maxLen int64) error {
	// 使用XADD创建Stream，如果不存在会自动创建
	_, err := m.client.GetClient().XAdd(ctx, &redis.XAddArgs{
		Stream: stream,
		MaxLen: maxLen,
		Values: map[string]interface{}{"init": "true"},
	}).Result()

	return err
}

// DeleteStream 删除Stream
func (m *StreamManager) DeleteStream(ctx context.Context, stream string) error {
	return m.client.GetClient().Del(ctx, stream).Err()
}

// GetStreamInfo 获取Stream信息
func (m *StreamManager) GetStreamInfo(ctx context.Context, stream string) (*redis.XInfoStream, error) {
	return m.client.GetClient().XInfoStream(ctx, stream).Result()
}

// GetGroupInfo 获取消费者组信息
func (m *StreamManager) GetGroupInfo(ctx context.Context, stream string) ([]redis.XInfoGroup, error) {
	return m.client.GetClient().XInfoGroups(ctx, stream).Result()
}

// TrimStream 裁剪Stream
func (m *StreamManager) TrimStream(ctx context.Context, stream string, maxLen int64) error {
	return m.client.GetClient().XTrimMaxLen(ctx, stream, maxLen).Err()
}

// StreamProcessor Stream处理器接口
type StreamProcessor interface {
	Process(ctx context.Context, message StreamMessage) error
}

// StreamWorker Stream工作器
type StreamWorker struct {
	consumer  *StreamConsumer
	processor StreamProcessor
	stream    string
	stopChan  chan struct{}
}

// NewStreamWorker 创建Stream工作器
func NewStreamWorker(consumer *StreamConsumer, processor StreamProcessor, stream string) *StreamWorker {
	return &StreamWorker{
		consumer:  consumer,
		processor: processor,
		stream:    stream,
		stopChan:  make(chan struct{}),
	}
}

// Start 启动工作器
func (w *StreamWorker) Start(ctx context.Context) error {
	log.Printf("Starting stream worker for stream: %s, group: %s, consumer: %s",
		w.stream, w.consumer.groupName, w.consumer.consumerID)

	for {
		select {
		case <-ctx.Done():
			log.Printf("Context cancelled, stopping worker")
			return ctx.Err()
		case <-w.stopChan:
			log.Printf("Stop signal received, stopping worker")
			return nil
		default:
			// 读取消息
			messages, err := w.consumer.ReadGroup(ctx, w.stream, 10, 5*time.Second)
			if err != nil {
				if err == redis.Nil {
					// 没有消息，继续等待
					continue
				}
				log.Printf("Error reading from stream: %v", err)
				time.Sleep(time.Second)
				continue
			}

			// 处理消息
			for _, message := range messages {
				if err := w.processor.Process(ctx, message); err != nil {
					log.Printf("Error processing message %s: %v", message.ID, err)
					// 可以选择是否确认消息，这里选择不确认，让消息重新投递
					continue
				}

				// 确认消息已处理
				if err := w.consumer.Acknowledge(ctx, w.stream, message.ID); err != nil {
					log.Printf("Error acknowledging message %s: %v", message.ID, err)
				}
			}
		}
	}
}

// Stop 停止工作器
func (w *StreamWorker) Stop() {
	close(w.stopChan)
}
