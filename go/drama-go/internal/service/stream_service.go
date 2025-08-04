package service

import (
	"context"
	"log"
	"time"

	"drama-go/pkg/redis"
)

// StreamService Stream服务
type StreamService struct {
	producer *redis.StreamProducer
	manager  *redis.StreamManager
}

// NewStreamService 创建Stream服务
func NewStreamService(producer *redis.StreamProducer, manager *redis.StreamManager) *StreamService {
	return &StreamService{
		producer: producer,
		manager:  manager,
	}
}

// PublishMessage 发布消息
func (s *StreamService) PublishMessage(ctx context.Context, stream string, message map[string]interface{}) (string, error) {
	return s.producer.Publish(ctx, stream, message)
}

// CreateStream 创建Stream
func (s *StreamService) CreateStream(ctx context.Context, stream string, maxLen int64) error {
	return s.manager.CreateStream(ctx, stream, maxLen)
}

// GetStreamInfo 获取Stream信息
func (s *StreamService) GetStreamInfo(ctx context.Context, stream string) (interface{}, error) {
	return s.manager.GetStreamInfo(ctx, stream)
}

// ExampleMessageProcessor 示例消息处理器
type ExampleMessageProcessor struct {
	service *StreamService
}

// NewExampleMessageProcessor 创建示例消息处理器
func NewExampleMessageProcessor(service *StreamService) *ExampleMessageProcessor {
	return &ExampleMessageProcessor{service: service}
}

// Process 处理消息
func (p *ExampleMessageProcessor) Process(ctx context.Context, message redis.StreamMessage) error {
	log.Printf("Processing message ID: %s", message.ID)

	// 示例：打印消息内容
	for key, value := range message.Values {
		log.Printf("  %s: %v", key, value)
	}

	// 示例：模拟业务处理
	time.Sleep(100 * time.Millisecond)

	log.Printf("Message %s processed successfully", message.ID)
	return nil
}

// UserEventProcessor 用户事件处理器示例
// 注意：需要先定义UserService才能使用
/*
type UserEventProcessor struct {
	userService *UserService
}

// NewUserEventProcessor 创建用户事件处理器
func NewUserEventProcessor(userService *UserService) *UserEventProcessor {
	return &UserEventProcessor{userService: userService}
}

// Process 处理用户事件
func (p *UserEventProcessor) Process(ctx context.Context, message redis.StreamMessage) error {
	log.Printf("Processing user event: %s", message.ID)

	// 根据消息类型处理不同的用户事件
	eventType, ok := message.Values["event_type"].(string)
	if !ok {
		return fmt.Errorf("missing event_type in message")
	}

	switch eventType {
	case "user_created":
		return p.handleUserCreated(ctx, message)
	case "user_updated":
		return p.handleUserUpdated(ctx, message)
	case "user_deleted":
		return p.handleUserDeleted(ctx, message)
	default:
		log.Printf("Unknown event type: %s", eventType)
		return nil
	}
}

// handleUserCreated 处理用户创建事件
func (p *UserEventProcessor) handleUserCreated(ctx context.Context, message redis.StreamMessage) error {
	userID, ok := message.Values["user_id"].(string)
	if !ok {
		return fmt.Errorf("missing user_id in user_created event")
	}

	log.Printf("Handling user created event for user: %s", userID)
	// 这里可以添加具体的业务逻辑，比如发送欢迎邮件、初始化用户数据等

	return nil
}

// handleUserUpdated 处理用户更新事件
func (p *UserEventProcessor) handleUserUpdated(ctx context.Context, message redis.StreamMessage) error {
	userID, ok := message.Values["user_id"].(string)
	if !ok {
		return fmt.Errorf("missing user_id in user_updated event")
	}

	log.Printf("Handling user updated event for user: %s", userID)
	// 这里可以添加具体的业务逻辑，比如更新缓存、同步数据等

	return nil
}

// handleUserDeleted 处理用户删除事件
func (p *UserEventProcessor) handleUserDeleted(ctx context.Context, message redis.StreamMessage) error {
	userID, ok := message.Values["user_id"].(string)
	if !ok {
		return fmt.Errorf("missing user_id in user_deleted event")
	}

	log.Printf("Handling user deleted event for user: %s", userID)
	// 这里可以添加具体的业务逻辑，比如清理相关数据、发送通知等

	return nil
}
*/
