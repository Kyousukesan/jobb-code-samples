package main

import (
	"drama-go/config"
	"drama-go/pkg/db"

	"gorm.io/gen"
)

func main() {
	config.InitConfig()
	db.InitDB()

	g := gen.NewGenerator(gen.Config{
		OutPath: "./internal/dao",
		Mode:    gen.WithoutContext | gen.WithDefaultQuery | gen.WithQueryInterface,
	})

	g.UseDB(db.Global)

	// 从数据库表生成 struct
	book := g.GenerateModelAs("book", "Book")
	user := g.GenerateModelAs("user_info_1", "User")

	g.ApplyBasic(book, user)
	g.Execute()
}
