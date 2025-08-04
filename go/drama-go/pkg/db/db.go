package db

import (
	"drama-go/config"
	"fmt"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

var Global *gorm.DB

func InitDB() {
	fmt.Println("config.Global:", config.Global) // 先看是否为 nil
	fmt.Print("dsn:" + config.Global.Db.Dsn)
	db, err := gorm.Open(mysql.Open(config.Global.Db.Dsn), &gorm.Config{})
	if err != nil {
		panic("数据库连接失败: " + err.Error())
	}
	Global = db
}

// GetDB 获取数据库实例
func GetDB() *gorm.DB {
	return Global
}
