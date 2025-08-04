package router

import (
	"drama-go/internal/handler"
	"drama-go/internal/stream_consumer"

	"github.com/labstack/echo/v4"
)

func RegisterRouter(echo *echo.Echo, streamConsumer *stream_consumer.StreamConsumer) {
	info(echo)
	user(echo)
	book(echo)
	stream(echo, streamConsumer)
}

func info(echo *echo.Echo) {

	// infoApi := echo.Group("info")

	// infoApi.GET("/info", )
}

func user(echo *echo.Echo) {
	userApi := echo.Group("api/user")
	uh := &handler.UserHandler{}
	userApi.POST("/login", uh.VistLogin)
}

func book(echo *echo.Echo) {
	bookApi := echo.Group("book")
	bh := &handler.BookHandler{}
	bookApi.GET("/list", bh.List)
}

func stream(echo *echo.Echo, streamConsumer *stream_consumer.StreamConsumer) {
	// TODO: 实现stream相关路由
	// 暂时注释掉，因为stream_handler包不存在
	/*
		// 获取Redis客户端
		redisClient := streamConsumer.GetManager().GetClient()

		// 为每个配置的stream创建handler
		configs := streamConsumer.GetConfigManager().GetAllConfigs()
		for streamName, config := range configs {
			// 创建Stream处理器
			streamHandler := stream_handler.NewStreamHandler(redisClient, streamName, config.GroupName, config.ConsumerID)

			// 创建HTTP处理器
			httpHandler := stream_handler.NewStreamHTTPHandler(streamHandler)

			// 注册路由，使用stream名称作为路径
			streamGroup := echo.Group("/api/" + streamName)

			// 手动注册路由，因为RegisterRoutes期望的是echo.Echo而不是echo.Group
			streamGroup.POST("/publish", httpHandler.PublishMessage)
			streamGroup.GET("/info", httpHandler.GetStreamInfo)
			streamGroup.GET("/group/info", httpHandler.GetGroupInfo)
			streamGroup.GET("/pending", httpHandler.GetPendingMessages)
			streamGroup.POST("/consumer/start", httpHandler.StartConsumer)
			streamGroup.POST("/consumer/stop", httpHandler.StopConsumer)
		}
	*/
}
