package handler

import (
	"drama-go/internal/model/dto"
	"drama-go/internal/service"
	"drama-go/pkg/response"
	"net/http"

	"github.com/labstack/echo/v4"
)

type UserHandler struct {
	userService *service.UserService
}

func NewUserHandler(userService *service.UserService) *UserHandler {
	return &UserHandler{
		userService: userService,
	}
}

func (user *UserHandler) Index(ctx echo.Context) error {
	return response.Success(ctx, "ok")
}

// VistLogin 用户登录
func (user *UserHandler) VistLogin(ctx echo.Context) error {
	var req dto.LoginRequest
	if err := ctx.Bind(&req); err != nil {
		return response.Error(ctx, http.StatusBadRequest, "请求参数错误")
	}

	// 验证请求参数
	if req.Phone == "" || req.Password == "" {
		return response.Error(ctx, http.StatusBadRequest, "手机号和密码不能为空")
	}

	// 调用服务层登录
	resp, err := user.userService.Login(&req)
	if err != nil {
		return response.Error(ctx, http.StatusUnauthorized, err.Error())
	}

	return response.Success(ctx, resp)
}

// GetUserInfo 获取用户信息
func (user *UserHandler) GetUserInfo(ctx echo.Context) error {
	// 从JWT中获取用户ID
	userID := ctx.Get("user_id").(int64)

	userInfo, err := user.userService.GetUserInfo(userID)
	if err != nil {
		return response.Error(ctx, http.StatusNotFound, "用户不存在")
	}

	return response.Success(ctx, userInfo)
}

// RefreshToken 刷新token
func (user *UserHandler) RefreshToken(ctx echo.Context) error {
	var req dto.RefreshTokenRequest
	if err := ctx.Bind(&req); err != nil {
		return response.Error(ctx, http.StatusBadRequest, "请求参数错误")
	}

	resp, err := user.userService.RefreshToken(req.RefreshToken)
	if err != nil {
		return response.Error(ctx, http.StatusUnauthorized, err.Error())
	}

	return response.Success(ctx, resp)
}
