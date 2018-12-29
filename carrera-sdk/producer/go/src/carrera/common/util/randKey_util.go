package util

import (
	"go.intra.xiaojukeji.com/golang/go.uuid"
)

func GenRandKey() string {
	return uuid.NewV1().String()
}
