package stream_consumer

import (
	"fmt"
	"log"
	"sync"

	"drama-go/pkg/redis"
)

// ProcessorRegistry 处理器注册中心
type ProcessorRegistry struct {
	processors map[string]ProcessorFactory
	mu         sync.RWMutex
}

// ProcessorFactory 处理器工厂函数
type ProcessorFactory func(consumer *StreamConsumer) redis.StreamProcessor

// NewProcessorRegistry 创建处理器注册中心
func NewProcessorRegistry() *ProcessorRegistry {
	return &ProcessorRegistry{
		processors: make(map[string]ProcessorFactory),
	}
}

// RegisterProcessor 注册处理器
func (r *ProcessorRegistry) RegisterProcessor(streamName string, factory ProcessorFactory) {
	r.mu.Lock()
	defer r.mu.Unlock()

	r.processors[streamName] = factory
	log.Printf("Registered processor for stream: %s", streamName)
}

// GetProcessor 获取处理器
func (r *ProcessorRegistry) GetProcessor(streamName string, consumer *StreamConsumer) (redis.StreamProcessor, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()

	factory, exists := r.processors[streamName]
	if !exists {
		return nil, fmt.Errorf("no processor registered for stream: %s", streamName)
	}

	return factory(consumer), nil
}

// ListRegisteredStreams 列出已注册的Stream
func (r *ProcessorRegistry) ListRegisteredStreams() []string {
	r.mu.RLock()
	defer r.mu.RUnlock()

	streams := make([]string, 0, len(r.processors))
	for streamName := range r.processors {
		streams = append(streams, streamName)
	}

	return streams
}

// HasProcessor 检查是否有处理器
func (r *ProcessorRegistry) HasProcessor(streamName string) bool {
	r.mu.RLock()
	defer r.mu.RUnlock()

	_, exists := r.processors[streamName]
	return exists
}

// UnregisterProcessor 注销处理器
func (r *ProcessorRegistry) UnregisterProcessor(streamName string) {
	r.mu.Lock()
	defer r.mu.Unlock()

	delete(r.processors, streamName)
	log.Printf("Unregistered processor for stream: %s", streamName)
}
