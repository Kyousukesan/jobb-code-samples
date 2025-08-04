package processors

import (
	"context"
	"fmt"
	"time"

	"drama-go/internal/stream_consumer"
	"drama-go/pkg/redis"
)

// BookSimilarityProcessor 书籍相似度处理器
type BookSimilarityProcessor struct {
	*stream_consumer.BaseProcessor
}

// NewBookSimilarityProcessor 创建书籍相似度处理器
func NewBookSimilarityProcessor(consumer *stream_consumer.StreamConsumer, config stream_consumer.QueueConfig) *BookSimilarityProcessor {
	processor := &BookSimilarityProcessor{
		BaseProcessor: stream_consumer.NewBaseProcessor(consumer, config),
	}

	// 设置消息处理函数，让 BaseProcessor.Process 能调用到子类的处理逻辑
	processor.SetMessageHandler(processor.handleMessage)

	return processor
}

// handleMessage 实现具体的消息处理逻辑
func (p *BookSimilarityProcessor) handleMessage(ctx context.Context, message redis.StreamMessage) error {
	// 解析消息数据
	bookID, ok := message.Values["book_id"].(string)
	if !ok {
		return fmt.Errorf("missing book_id in book similarity message")
	}

	p.LogInfo("开始计算相似度", map[string]interface{}{
		"book_id":    bookID,
		"message_id": message.ID,
	})

	// 调用相似度计算服务
	if err := p.calculateBookSimilarity(ctx, bookID); err != nil {
		p.LogError("相似度计算失败", map[string]interface{}{
			"book_id": bookID,
			"error":   err.Error(),
		})
		return err
	}

	p.LogInfo("相似度计算完成", map[string]interface{}{
		"book_id": bookID,
	})

	return nil
}

// calculateBookSimilarity 计算书籍相似度
func (p *BookSimilarityProcessor) calculateBookSimilarity(ctx context.Context, bookID string) error {
	// 模拟计算相似度的过程
	time.Sleep(200 * time.Millisecond)

	// 这里可以添加具体的相似度计算逻辑
	// 例如：
	// 1. 获取书籍特征
	// 2. 查找相似书籍
	// 3. 计算相似度分数
	// 4. 更新数据库

	// 模拟可能的错误
	if bookID == "error_book" {
		return fmt.Errorf("simulated error for testing")
	}

	return nil
}

// BookSimilarityProcessorFactory 书籍相似度处理器工厂函数
func BookSimilarityProcessorFactory(consumer *stream_consumer.StreamConsumer) redis.StreamProcessor {
	config, _ := consumer.GetConfigManager().GetConfig("book_similarity")
	return NewBookSimilarityProcessor(consumer, config)
}
