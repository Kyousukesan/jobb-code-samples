package main

import (
	"drama-go/config"
	"drama-go/pkg/jwt"
	"fmt"
	"log"
)

func main() {
	// 初始化配置
	config.InitConfig()

	// 创建JWT管理器
	jwtConfig := &jwt.Config{
		SecretKey: config.Global.JWT.SecretKey,
		Expire:    config.Global.JWT.Expire,
	}
	jwtMgr := jwt.NewJWTManager(jwtConfig)

	// 生成token
	userID := int64(12345)
	nickname := "测试用户"
	phone := "13800138000"

	token, err := jwtMgr.GenerateToken(userID, nickname, phone)
	if err != nil {
		log.Fatalf("生成token失败: %v", err)
	}

	fmt.Printf("生成的token: %s\n", token)

	// 验证token
	claims, err := jwtMgr.ValidateToken(token)
	if err != nil {
		log.Fatalf("验证token失败: %v", err)
	}

	fmt.Printf("验证成功，用户信息: UserID=%d, Nickname=%s, Phone=%s\n",
		claims.UserID, claims.Nickname, claims.Phone)

	// 刷新token
	refreshToken, err := jwtMgr.RefreshToken(token)
	if err != nil {
		log.Fatalf("刷新token失败: %v", err)
	}

	fmt.Printf("刷新后的token: %s\n", refreshToken)

	// 验证刷新后的token
	refreshClaims, err := jwtMgr.ValidateToken(refreshToken)
	if err != nil {
		log.Fatalf("验证刷新token失败: %v", err)
	}

	fmt.Printf("刷新token验证成功，用户信息: UserID=%d, Nickname=%s, Phone=%s\n",
		refreshClaims.UserID, refreshClaims.Nickname, refreshClaims.Phone)

	fmt.Println("JWT测试完成！")
}
