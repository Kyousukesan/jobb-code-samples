package middleware

import (
	"drama-go/pkg/jwt"
	"drama-go/pkg/response"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
)

// JWTConfig JWT中间件配置
type JWTConfig struct {
	JWTManager *jwt.JWTManager
	Skipper    func(c echo.Context) bool
}

// DefaultJWTConfig 默认JWT配置
func DefaultJWTConfig(jwtMgr *jwt.JWTManager) JWTConfig {
	return JWTConfig{
		JWTManager: jwtMgr,
		Skipper: func(c echo.Context) bool {
			// 跳过登录接口和刷新token接口
			path := c.Path()
			return path == "/api/user/login" || path == "/api/user/refresh"
		},
	}
}

// JWT JWT中间件
func JWT(config JWTConfig) echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			// 跳过不需要验证的接口
			if config.Skipper != nil && config.Skipper(c) {
				return next(c)
			}

			// 获取Authorization header
			auth := c.Request().Header.Get("Authorization")
			if auth == "" {
				return response.Error(c, http.StatusUnauthorized, "缺少Authorization header")
			}

			// 检查Bearer前缀
			if !strings.HasPrefix(auth, "Bearer ") {
				return response.Error(c, http.StatusUnauthorized, "Authorization header格式错误")
			}

			// 提取token
			token := strings.TrimPrefix(auth, "Bearer ")

			// 验证token
			claims, err := config.JWTManager.ValidateToken(token)
			if err != nil {
				return response.Error(c, http.StatusUnauthorized, "无效的token")
			}

			// 将用户信息存储到context中
			c.Set("user_id", claims.UserID)
			c.Set("nickname", claims.Nickname)
			c.Set("phone", claims.Phone)

			return next(c)
		}
	}
}
