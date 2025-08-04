package dto

// LoginRequest 登录请求
type LoginRequest struct {
	Phone    string `json:"phone" validate:"required"`    // 手机号
	Password string `json:"password" validate:"required"` // 密码
}

// LoginResponse 登录响应
type LoginResponse struct {
	Token        string `json:"token"`         // JWT token
	RefreshToken string `json:"refresh_token"` // 刷新token
	UserID       int64  `json:"user_id"`       // 用户ID
	Nickname     string `json:"nickname"`      // 昵称
	Phone        string `json:"phone"`         // 手机号
	Avatar       string `json:"avatar"`        // 头像
}

// RefreshTokenRequest 刷新token请求
type RefreshTokenRequest struct {
	RefreshToken string `json:"refresh_token" validate:"required"`
}

// RefreshTokenResponse 刷新token响应
type RefreshTokenResponse struct {
	Token        string `json:"token"`
	RefreshToken string `json:"refresh_token"`
}

// UserInfo 用户信息
type UserInfo struct {
	UserID   int64  `json:"user_id"`
	Nickname string `json:"nickname"`
	Phone    string `json:"phone"`
	Avatar   string `json:"avatar"`
	Email    string `json:"email"`
	Sex      int32  `json:"sex"`
	Status   int32  `json:"status"`
}
