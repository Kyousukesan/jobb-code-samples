package handler

import (
	"drama-go/pkg/response"

	"github.com/labstack/echo/v4"
)

type IndexHandler struct {
}

func (h *IndexHandler) index(ctx echo.Context) error {

	return response.Success(ctx, nil)
}
