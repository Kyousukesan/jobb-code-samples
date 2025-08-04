package response

import (
	"net/http"

	"github.com/labstack/echo/v4"
)

type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// 成功返回
func Success(c echo.Context, data interface{}) error {
	return c.JSON(http.StatusOK, Response{
		Code:    0,
		Message: "success",
		Data:    data,
	})
}

// 带自定义消息的成功返回
func SuccessMsg(c echo.Context, msg string, data interface{}) error {
	return c.JSON(http.StatusOK, Response{
		Code:    0,
		Message: msg,
		Data:    data,
	})
}

// 失败返回（默认 400）
func Error(c echo.Context, code int, msg string) error {
	return c.JSON(http.StatusBadRequest, Response{
		Code:    code,
		Message: msg,
	})
}

// 通用错误（内部错误）
func InternalError(c echo.Context, err error) error {
	return c.JSON(http.StatusInternalServerError, Response{
		Code:    500,
		Message: "internal error: " + err.Error(),
	})
}
