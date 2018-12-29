package dhttp

import (
	"encoding/json"
	"net/http"
	"testing"
	"time"
)

func TestHttpGet(t *testing.T) {
	logid := time.Now().UnixNano()
	iceHolder, err := NewHTTPClientHolder("test", "ice", []string{"http://127.0.0.1:8008"},
		200*time.Millisecond, 200*time.Millisecond, 30*time.Second, 2, 1, 0)
	if err != nil {
		t.Fatalf("%d, error while create ice holder: %v", logid, err)
	}
	headers := make(map[string][]string)
	headers["Authorization"] = []string{"51000001"}
	if iceResp, err := iceHolder.Get(logid, "/idgen", "", headers); err != nil {
		t.Fatalf("%d, error while do get request: %d", logid, iceResp.StatusCode)
	} else if iceResp.StatusCode != http.StatusOK {
		t.Fatalf("%d, bad http response status code: %d", logid, iceResp.StatusCode)
	} else {
		if iceMap, err := iceResp.DecodeUseNumber(logid); err != nil {
			t.Fatalf("%d, id in response is not number: %v", logid, iceResp)
		} else if errno, ok := iceMap["errno"].(json.Number); !ok {
			t.Fatalf("%d, bad type, not json.Number: %v, from ice response ", logid, iceMap["errno"])
		} else if errno, err := errno.Int64(); err != nil {
			t.Fatalf("%d, bad errno: %v, from ice response ", logid, errno)
		} else {
			t.Logf("%d, get id succ: %v", logid, iceMap["id"])
		}
	}
}
