package util

import (
	"testing"
)

func TestBinaryInsert(t *testing.T) {
	var s []uint64
	s = BinaryInsert(s, 0, len(s)-1, 8)
	sExp := []uint64{8}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}

	s = []uint64{3}
	s = BinaryInsert(s, 0, len(s)-1, 8)
	sExp = []uint64{3, 8}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}

	s = []uint64{3}
	s = BinaryInsert(s, 0, len(s)-1, 2)
	sExp = []uint64{2, 3}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}

	s = []uint64{3, 7, 11, 15, 19, 23}
	s = BinaryInsert(s, 0, len(s)-1, 8)
	sExp = []uint64{3, 7, 8, 11, 15, 19, 23}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}

	s = []uint64{3, 7, 11, 15, 19, 23}
	s = BinaryInsert(s, 0, len(s)-1, 2)
	sExp = []uint64{2, 3, 7, 11, 15, 19, 23}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}

	s = []uint64{3, 7, 11, 15, 19, 23}
	s = BinaryInsert(s, 0, len(s)-1, 24)
	sExp = []uint64{3, 7, 11, 15, 19, 23, 24}
	if !sliceEqual(s, sExp) {
		t.Errorf("Unpected s: %v, expected: %v", s, sExp)
	}
}

func BenchmarkBinaryInsert(b *testing.B) {
	//
	waterMark := 20000
	s := make([]uint64, waterMark)
	step := uint64(4)
	init := uint64(0)
	for i := 0; i < waterMark; i++ {
		s[i] = init + uint64(i)*step
	}
	init = 1
	var target uint64
	for i := 0; i < b.N; i++ {
		target = init + uint64(i)*step
		s = BinaryInsert(s, 0, len(s)-1, target)
		s = s[1:]
	}
}

func sliceEqual(s1 []uint64, s2 []uint64) bool {
	if s1 == nil || s2 == nil {
		return false
	}
	if len(s1) != len(s2) {
		return false
	}
	for i := range s1 {
		if s1[i] != s2[i] {
			return false
		}
	}
	return true
}
