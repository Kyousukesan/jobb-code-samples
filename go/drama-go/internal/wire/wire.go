//go:build wireinject
// +build wireinject

package wire

import (
	"context"
	"drama-go/config"
	"drama-go/internal/dao"
	"drama-go/internal/handler"
	"drama-go/internal/middleware"
	"drama-go/internal/service"
	"drama-go/pkg/db"
	"drama-go/pkg/jwt"
	"drama-go/pkg/redis"

	"github.com/google/wire"
	"github.com/labstack/echo/v4"
	"gorm.io/gorm"
)

// InitializeApp 初始化应用
func InitializeApp() (*echo.Echo, error) {
	wire.Build(
		// 配置
		provideConfig,

		// 数据库
		provideDB,

		// Redis
		redis.NewClient,

		// DAO
		provideUserDAO,

		// JWT
		provideJWTManager,

		// Service
		service.NewUserService,

		// Handler
		handler.NewUserHandler,

		// Middleware
		middleware.DefaultJWTConfig,

		// Echo
		echo.New,

		// Router
		provideRouter,
	)
	return &echo.Echo{}, nil
}

// provideConfig 提供配置
func provideConfig() *config.Config {
	return config.Global
}

// provideDB 提供数据库
func provideDB() *gorm.DB {
	return db.GetDB()
}

// provideUserDAO 提供用户DAO
func provideUserDAO(db *gorm.DB) dao.IUserDo {
	dao.SetDefault(db)
	return dao.User.WithContext(context.Background())
}

// provideJWTManager 提供JWT管理器
func provideJWTManager(cfg *config.Config) *jwt.JWTManager {
	jwtConfig := &jwt.Config{
		SecretKey: cfg.JWT.SecretKey,
		Expire:    cfg.JWT.Expire,
	}
	return jwt.NewJWTManager(jwtConfig)
}

// provideRouter 提供路由
func provideRouter(e *echo.Echo, userHandler *handler.UserHandler, jwtConfig middleware.JWTConfig) error {
	// 添加JWT中间件
	e.Use(middleware.JWT(jwtConfig))

	// 注册路由
	userApi := e.Group("api/user")
	userApi.POST("/login", userHandler.VistLogin)
	userApi.GET("/info", userHandler.GetUserInfo)
	userApi.POST("/refresh", userHandler.RefreshToken)

	return nil
}
