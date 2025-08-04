package jwt

import (
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// JWT配置
type Config struct {
	SecretKey string        `json:"secret_key"`
	Expire    time.Duration `json:"expire"`
}

// Claims JWT声明
type Claims struct {
	UserID   int64  `json:"user_id"`
	Nickname string `json:"nickname"`
	Phone    string `json:"phone"`
	jwt.RegisteredClaims
}

// JWTManager JWT管理器
type JWTManager struct {
	config *Config
}

// NewJWTManager 创建JWT管理器
func NewJWTManager(config *Config) *JWTManager {
	return &JWTManager{
		config: config,
	}
}

// GenerateToken 生成JWT token
func (j *JWTManager) GenerateToken(userID int64, nickname, phone string) (string, error) {
	claims := &Claims{
		UserID:   userID,
		Nickname: nickname,
		Phone:    phone,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(j.config.Expire)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			NotBefore: jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(j.config.SecretKey))
}

// ValidateToken 验证JWT token
func (j *JWTManager) ValidateToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(j.config.SecretKey), nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}

	return nil, errors.New("invalid token")
}

// RefreshToken 刷新token
func (j *JWTManager) RefreshToken(tokenString string) (string, error) {
	claims, err := j.ValidateToken(tokenString)
	if err != nil {
		return "", err
	}

	// 更新过期时间
	claims.ExpiresAt = jwt.NewNumericDate(time.Now().Add(j.config.Expire))
	claims.IssuedAt = jwt.NewNumericDate(time.Now())

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(j.config.SecretKey))
}
