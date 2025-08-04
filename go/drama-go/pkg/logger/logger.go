package logger

import "go.uber.org/zap"

var Global *zap.Logger

func Init() error {
	log, err := zap.NewProduction()
	if err != nil {
		return err
	}
	Global = log
	return nil
}
