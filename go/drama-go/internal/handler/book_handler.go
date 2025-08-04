package handler

import (
	"drama-go/internal/dao"
	"drama-go/pkg/db"
	"drama-go/pkg/response"
	"strconv"

	"github.com/labstack/echo/v4"
)

type BookHandler struct {
}

func (book *BookHandler) List(ctx echo.Context) error {
	// 入参
	pageStr := ctx.QueryParam("page")
	sizeStr := ctx.QueryParam("size")
	bookNo := ctx.QueryParam("book_no")
	bookName := ctx.QueryParam("book_name")

	page, _ := strconv.Atoi(pageStr)
	if page < 1 {
		page = 1
	}
	size, _ := strconv.Atoi(sizeStr)
	if size < 1 {
		size = 10
	}
	offset := (page - 1) * size

	db.Global.DB()
	dao.Book.UseDB(db.Global)
	list, err := dao.Book.WithContext(ctx.Request().Context()).Where(
		dao.Book.BookNo.Like("%" + bookNo + "%")).Where(
		dao.Book.BookName.Like("%" + bookName + "%")).Limit(size).Offset(offset).Find()
	if err != nil {
		return response.Error(ctx, 500, err.Error())
	}
	return response.Success(ctx, list)
}
