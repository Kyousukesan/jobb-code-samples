package processors

import (
	"context"
	"time"

	"drama-go/internal/stream_consumer"
	"drama-go/pkg/redis"
)

// TemplateProcessor 模板处理器（示例）
type TemplateProcessor struct {
	*stream_consumer.BaseProcessor
}

// NewTemplateProcessor 创建模板处理器
func NewTemplateProcessor(consumer *stream_consumer.StreamConsumer, config stream_consumer.QueueConfig) *TemplateProcessor {
	processor := &TemplateProcessor{
		BaseProcessor: stream_consumer.NewBaseProcessor(consumer, config),
	}

	// 设置消息处理函数，让 BaseProcessor.Process 能调用到子类的处理逻辑
	processor.SetMessageHandler(processor.handleMessage)

	return processor
}

// handleMessage 实现具体的消息处理逻辑
func (p *TemplateProcessor) handleMessage(ctx context.Context, message redis.StreamMessage) error {
	// 1. 解析消息数据
	// exampleID, ok := message.Values["example_id"].(string)
	// if !ok {
	//     return fmt.Errorf("missing example_id in message")
	// }

	p.LogInfo("开始处理消息", map[string]interface{}{
		"message_id": message.ID,
		"data":       message.Values,
	})

	// 2. 执行业务逻辑
	if err := p.processBusinessLogic(ctx, message); err != nil {
		p.LogError("业务处理失败", map[string]interface{}{
			"message_id": message.ID,
			"error":      err.Error(),
		})
		return err
	}

	// 3. 处理成功
	p.LogInfo("消息处理完成", map[string]interface{}{
		"message_id": message.ID,
	})

	return nil
}

// processBusinessLogic 处理业务逻辑
func (p *TemplateProcessor) processBusinessLogic(ctx context.Context, message redis.StreamMessage) error {
	// 模拟业务处理
	time.Sleep(100 * time.Millisecond)

	// 这里添加具体的业务逻辑
	// 例如：
	// 1. 调用外部API
	// 2. 更新数据库
	// 3. 发送通知
	// 4. 发布到其他队列

	return nil
}

// TemplateProcessorFactory 模板处理器工厂函数
func TemplateProcessorFactory(consumer *stream_consumer.StreamConsumer) redis.StreamProcessor {
	config, _ := consumer.GetConfigManager().GetConfig("template_queue")
	return NewTemplateProcessor(consumer, config)
}

/*
使用说明：

1. 复制这个文件并重命名，例如：user_events.go
2. 修改结构体名称：TemplateProcessor -> UserEventsProcessor
3. 修改工厂函数名称：TemplateProcessorFactory -> UserEventsProcessorFactory
4. 实现 handleMessage 方法中的具体业务逻辑
5. 在 register.go 中注册新的处理器
6. 在 config/queues.json 中添加队列配置

示例：
```go
// 在 register.go 中添加：
registry.RegisterProcessor("user_events", UserEventsProcessorFactory)

// 在 config/queues.json 中添加：
{
  "stream_name": "user_events",
  "group_name": "user_group",
  "consumer_id": "consumer_1",
  "auto_start": true,
  "enabled": true,
  "max_retries": 3,
  "batch_size": 10,
  "timeout": 30
}
```
*/
