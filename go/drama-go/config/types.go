package config

import "time"

type Config struct {
	App struct {
		Name string `mapstructure:"name"`
		Port string `mapstructure:"port"`
		Env  string `mapstructure:"env"`
	} `mapstructure:"app"`
	Db struct {
		Dsn string `mapstructure:"dsn"`
	} `mapstructure:"database"`
	Redis struct {
		Addr         string `mapstructure:"addr"`
		Password     string `mapstructure:"password"`
		DB           int    `mapstructure:"db"`
		PoolSize     int    `mapstructure:"pool_size"`
		MinIdleConns int    `mapstructure:"min_idle_conns"`
	} `mapstructure:"redis"`
	JWT struct {
		SecretKey string        `mapstructure:"secret_key"`
		Expire    time.Duration `mapstructure:"expire"`
	} `mapstructure:"jwt"`
}
