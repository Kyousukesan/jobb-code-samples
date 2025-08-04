package main

import (
	"context"
	"drama-go/config"
	"drama-go/internal/dao"
	"drama-go/internal/handler"
	"drama-go/internal/middleware"
	"drama-go/internal/router"
	"drama-go/internal/service"
	"drama-go/internal/stream_consumer"
	"drama-go/internal/stream_consumer/processors"
	"drama-go/pkg/db"
	"drama-go/pkg/jwt"
	"drama-go/pkg/redis"
	"log"
	"os"
	"os/signal"
	"syscall"

	"github.com/labstack/echo/v4"
)

func main() {
	e := echo.New()

	// 初始化配置
	config.InitConfig()

	// 初始化数据库
	db.InitDB()

	// 初始化Redis客户端
	redisClient, err := redis.NewClient(config.Global)
	if err != nil {
		log.Fatalf("Failed to initialize Redis client: %v", err)
	}
	defer redisClient.Close()

	// 初始化DAO
	dao.SetDefault(db.Global)

	// 初始化JWT管理器
	jwtConfig := &jwt.Config{
		SecretKey: config.Global.JWT.SecretKey,
		Expire:    config.Global.JWT.Expire,
	}
	jwtMgr := jwt.NewJWTManager(jwtConfig)

	// 初始化用户服务
	userService := service.NewUserService(dao.User.WithContext(context.Background()), jwtMgr)

	// 初始化用户处理器
	userHandler := handler.NewUserHandler(userService)

	// 配置JWT中间件
	jwtMiddlewareConfig := middleware.DefaultJWTConfig(jwtMgr)
	e.Use(middleware.JWT(jwtMiddlewareConfig))

	// 创建并启动Stream消费者
	streamConsumer := stream_consumer.NewStreamConsumer(redisClient)

	// 注册所有处理器
	processors.RegisterAllProcessors(streamConsumer)

	// 启动Stream消费者
	if err := streamConsumer.Start(); err != nil {
		log.Fatalf("Failed to start stream consumer: %v", err)
	}

	// 注册路由
	router.RegisterRouter(e, streamConsumer)

	// 注册用户路由
	userApi := e.Group("api/user")
	userApi.POST("/login", userHandler.VistLogin)
	userApi.GET("/info", userHandler.GetUserInfo)
	userApi.POST("/refresh", userHandler.RefreshToken)

	// 启动HTTP服务器
	port := config.Global.App.Port
	if port == "" {
		port = "8080"
	}

	// 在goroutine中启动服务器
	go func() {
		log.Printf("Starting HTTP server on port %s", port)
		if err := e.Start(":" + port); err != nil {
			log.Printf("HTTP server error: %v", err)
		}
	}()

	// 等待中断信号
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down server...")

	// 优雅关闭
	ctx, cancel := context.WithTimeout(context.Background(), 30)
	defer cancel()

	// 停止Stream消费者
	streamConsumer.Stop()

	// 关闭HTTP服务器
	if err := e.Shutdown(ctx); err != nil {
		log.Printf("Error during server shutdown: %v", err)
	}

	log.Println("Server stopped")
}
