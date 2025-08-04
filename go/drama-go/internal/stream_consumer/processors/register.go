package processors

import (
	"drama-go/internal/stream_consumer"
)

// RegisterAllProcessors 注册所有处理器
func RegisterAllProcessors(consumer *stream_consumer.StreamConsumer) {
	registry := consumer.GetRegistry()

	// 注册书籍相似度处理器
	registry.RegisterProcessor("book_similarity", BookSimilarityProcessorFactory)

	// 注册用户登录流处理器
	registry.RegisterProcessor("abcstream:app_queue_redis_UserLoginStream", UserLoginStreamProcessorFactory)

	// 这里可以继续注册其他处理器
	// registry.RegisterProcessor("user_events", UserEventsProcessorFactory)
	// registry.RegisterProcessor("system_events", SystemEventsProcessorFactory)
	// registry.RegisterProcessor("notifications", NotificationProcessorFactory)
	// registry.RegisterProcessor("orders", OrderProcessorFactory)
	// registry.RegisterProcessor("payments", PaymentProcessorFactory)
}
