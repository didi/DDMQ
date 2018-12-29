package util

import (
	"testing"
	"time"
)

type TestStruct struct {
	A int
	B string
	C byte
}

func TestJsonCompress(t *testing.T) {
	o := &TestStruct{A: 1234, B: "test xxx with yyy", C: 127}
	logid := time.Now().UnixNano()
	result, err := JsonCompress(logid, o)
	if err != nil {
		t.Errorf("%d, json compress failed: %v", logid, err)
	} else {
		t.Logf("%d, json compress succ: %v", logid, string(result))
	}

	var decomp *TestStruct
	//var decomp interface{}
	if err = JsonDecompress(logid, result, &decomp); err != nil {
		t.Errorf("%d, json decompress failed: %v", logid, err)
	} else {
		t.Logf("%d, json decompress succ: %v", logid, decomp)
	}
	decomp = &TestStruct{}
	// decompress again, var decomp interface{}
	if err = JsonDecompress(logid, result, &decomp); err != nil {
		t.Errorf("%d, json decompress failed: %v", logid, err)
	} else {
		t.Logf("%d, json decompress succ: %v", logid, decomp)
	}
}
