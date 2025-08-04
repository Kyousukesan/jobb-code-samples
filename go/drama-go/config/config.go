package config

import (
	"fmt"

	"github.com/spf13/viper"
)

var Global *Config

func InitConfig() error {
	viper.AddConfigPath("config/")
	viper.SetConfigName("app")
	viper.SetConfigType("yaml")
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		return fmt.Errorf("read config error: %w", err)
	}

	var cfg Config

	if err := viper.Unmarshal(&cfg); err != nil {
		return fmt.Errorf("unmarshal config error: %w", err)
	}

	Global = &cfg
	return nil
}
