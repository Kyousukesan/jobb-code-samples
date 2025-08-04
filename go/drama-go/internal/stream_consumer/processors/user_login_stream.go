package processors

import (
	"context"
	"fmt"
	"time"

	"drama-go/internal/stream_consumer"
	"drama-go/pkg/redis"
)

// UserLoginStreamProcessor 用户登录流处理器
type UserLoginStreamProcessor struct {
	*stream_consumer.BaseProcessor
}

// NewUserLoginStreamProcessor 创建用户登录流处理器
func NewUserLoginStreamProcessor(consumer *stream_consumer.StreamConsumer, config stream_consumer.QueueConfig) *UserLoginStreamProcessor {
	processor := &UserLoginStreamProcessor{
		BaseProcessor: stream_consumer.NewBaseProcessor(consumer, config),
	}

	// 设置消息处理函数，让 BaseProcessor.Process 能调用到子类的处理逻辑
	processor.SetMessageHandler(processor.handleMessage)

	return processor
}

// handleMessage 实现具体的消息处理逻辑
func (p *UserLoginStreamProcessor) handleMessage(ctx context.Context, message redis.StreamMessage) error {
	// 解析消息数据
	userID, ok := message.Values["user_id"].(string)
	if !ok {
		return fmt.Errorf("missing user_id in user login stream message")
	}

	action, ok := message.Values["action"].(string)
	if !ok {
		action = "unknown"
	}

	p.LogInfo("开始处理用户登录流消息", map[string]interface{}{
		"user_id":    userID,
		"action":     action,
		"message_id": message.ID,
	})

	// 根据不同的用户事件类型进行处理
	switch action {
	case "login":
		return p.handleUserLogin(ctx, userID, message)
	case "logout":
		return p.handleUserLogout(ctx, userID, message)
	case "register":
		return p.handleUserRegister(ctx, userID, message)
	case "update_profile":
		return p.handleUserUpdateProfile(ctx, userID, message)
	default:
		p.LogInfo("未知的用户操作", map[string]interface{}{
			"action": action,
		})
		return p.handleDefaultUserEvent(ctx, userID, message)
	}
}

// handleUserLogin 处理用户登录事件
func (p *UserLoginStreamProcessor) handleUserLogin(ctx context.Context, userID string, message redis.StreamMessage) error {
	p.LogInfo("处理用户登录事件", map[string]interface{}{
		"user_id": userID,
	})

	// 模拟处理登录事件
	time.Sleep(50 * time.Millisecond)

	// 这里可以添加具体的业务逻辑
	// 例如：更新用户最后登录时间、记录登录日志、发送欢迎消息等

	p.LogInfo("用户登录事件处理完成", map[string]interface{}{
		"user_id": userID,
	})
	return nil
}

// handleUserLogout 处理用户登出事件
func (p *UserLoginStreamProcessor) handleUserLogout(ctx context.Context, userID string, message redis.StreamMessage) error {
	p.LogInfo("处理用户登出事件", map[string]interface{}{
		"user_id": userID,
	})

	// 模拟处理登出事件
	time.Sleep(30 * time.Millisecond)

	// 这里可以添加具体的业务逻辑
	// 例如：清理用户会话、记录登出日志等

	p.LogInfo("用户登出事件处理完成", map[string]interface{}{
		"user_id": userID,
	})
	return nil
}

// handleUserRegister 处理用户注册事件
func (p *UserLoginStreamProcessor) handleUserRegister(ctx context.Context, userID string, message redis.StreamMessage) error {
	p.LogInfo("处理用户注册事件", map[string]interface{}{
		"user_id": userID,
	})

	// 模拟处理注册事件
	time.Sleep(100 * time.Millisecond)

	// 这里可以添加具体的业务逻辑
	// 例如：发送欢迎邮件、初始化用户数据、分配默认权限等

	p.LogInfo("用户注册事件处理完成", map[string]interface{}{
		"user_id": userID,
	})
	return nil
}

// handleUserUpdateProfile 处理用户更新资料事件
func (p *UserLoginStreamProcessor) handleUserUpdateProfile(ctx context.Context, userID string, message redis.StreamMessage) error {
	p.LogInfo("处理用户更新资料事件", map[string]interface{}{
		"user_id": userID,
	})

	// 模拟处理更新资料事件
	time.Sleep(40 * time.Millisecond)

	// 这里可以添加具体的业务逻辑
	// 例如：更新用户资料、记录变更日志等

	p.LogInfo("用户更新资料事件处理完成", map[string]interface{}{
		"user_id": userID,
	})
	return nil
}

// handleDefaultUserEvent 处理默认用户事件
func (p *UserLoginStreamProcessor) handleDefaultUserEvent(ctx context.Context, userID string, message redis.StreamMessage) error {
	p.LogInfo("处理默认用户事件", map[string]interface{}{
		"user_id": userID,
	})

	// 模拟处理默认事件
	time.Sleep(20 * time.Millisecond)

	p.LogInfo("默认用户事件处理完成", map[string]interface{}{
		"user_id": userID,
	})
	return nil
}

// UserLoginStreamProcessorFactory 用户登录流处理器工厂函数
func UserLoginStreamProcessorFactory(consumer *stream_consumer.StreamConsumer) redis.StreamProcessor {
	config, _ := consumer.GetConfigManager().GetConfig("abcstream:app_queue_redis_UserLoginStream")
	return NewUserLoginStreamProcessor(consumer, config)
}
