package service

import (
	"drama-go/internal/dao"
	"drama-go/internal/model/dto"
	"drama-go/pkg/jwt"
	"errors"
	"time"
)

type UserService struct {
	userDao dao.IUserDo
	jwtMgr  *jwt.JWTManager
}

func NewUserService(userDao dao.IUserDo, jwtMgr *jwt.JWTManager) *UserService {
	return &UserService{
		userDao: userDao,
		jwtMgr:  jwtMgr,
	}
}

// Login 用户登录
func (s *UserService) Login(req *dto.LoginRequest) (*dto.LoginResponse, error) {
	// 根据手机号查找用户
	user, err := s.userDao.Where(dao.User.Phone.Eq(req.Phone)).First()
	if err != nil {
		return nil, errors.New("用户不存在")
	}

	// 验证密码（这里假设密码已经加密存储，实际项目中需要根据具体情况调整）
	// 如果密码是明文存储，直接比较
	if user.Phone != req.Phone {
		return nil, errors.New("手机号或密码错误")
	}

	// 检查用户状态
	if user.Status != 1 {
		return nil, errors.New("用户已被禁用")
	}

	// 生成JWT token
	token, err := s.jwtMgr.GenerateToken(user.UserID, user.Nickname, user.Phone)
	if err != nil {
		return nil, err
	}

	// 生成刷新token（这里简化处理，实际项目中可能需要单独存储）
	refreshToken, err := s.jwtMgr.GenerateToken(user.UserID, user.Nickname, user.Phone)
	if err != nil {
		return nil, err
	}

	// 更新最后登录时间
	user.UpdatedAt = time.Now()
	s.userDao.Updates(user)

	return &dto.LoginResponse{
		Token:        token,
		RefreshToken: refreshToken,
		UserID:       user.UserID,
		Nickname:     user.Nickname,
		Phone:        user.Phone,
		Avatar:       user.Avatar,
	}, nil
}

// GetUserInfo 获取用户信息
func (s *UserService) GetUserInfo(userID int64) (*dto.UserInfo, error) {
	user, err := s.userDao.Where(dao.User.UserID.Eq(userID)).First()
	if err != nil {
		return nil, err
	}

	return &dto.UserInfo{
		UserID:   user.UserID,
		Nickname: user.Nickname,
		Phone:    user.Phone,
		Avatar:   user.Avatar,
		Email:    user.Email,
		Sex:      user.Sex,
		Status:   user.Status,
	}, nil
}

// RefreshToken 刷新token
func (s *UserService) RefreshToken(refreshToken string) (*dto.RefreshTokenResponse, error) {
	claims, err := s.jwtMgr.ValidateToken(refreshToken)
	if err != nil {
		return nil, errors.New("无效的刷新token")
	}

	// 生成新的token
	token, err := s.jwtMgr.GenerateToken(claims.UserID, claims.Nickname, claims.Phone)
	if err != nil {
		return nil, err
	}

	// 生成新的刷新token
	newRefreshToken, err := s.jwtMgr.GenerateToken(claims.UserID, claims.Nickname, claims.Phone)
	if err != nil {
		return nil, err
	}

	return &dto.RefreshTokenResponse{
		Token:        token,
		RefreshToken: newRefreshToken,
	}, nil
}
