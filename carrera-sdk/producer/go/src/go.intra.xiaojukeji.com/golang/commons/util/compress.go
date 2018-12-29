package util

import (
	"bytes"
	"compress/gzip"
	"encoding/json"
	"errors"
	"sync"

	log "go.intra.xiaojukeji.com/golang/commons/dlog"
)

const (
	CompressLevel = gzip.DefaultCompression
)

var gzipWriterPool = sync.Pool{
	New: func() interface{} {
		w, _ := gzip.NewWriterLevel(nil, CompressLevel)
		return w
	},
}

var gzipReaderPool sync.Pool

var gzipBufPool = sync.Pool{
	New: func() interface{} { return new(bytes.Buffer) },
}

/**
 * Encode the obj to a json string and then compress it to byte array
 */
func JsonCompress(logid int64, obj interface{}) ([]byte, error) {
	buf := gzipBufPool.Get().(*bytes.Buffer)
	defer gzipBufPool.Put(buf)
	buf.Reset()
	gzipWriter := gzipWriterPool.Get().(*gzip.Writer)
	defer gzipWriterPool.Put(gzipWriter)
	gzipWriter.Reset(buf)
	if err := json.NewEncoder(gzipWriter).Encode(obj); err != nil {
		log.Errorf("%d, err while json compress obj: %v, err: %v", logid, obj, err)
		//return nil, err
	}
	if err := gzipWriter.Close(); err != nil {
		log.Errorf("%d, err while close gzip writer, err: %v", logid, err)
		return nil, err
	}
	return buf.Bytes(), nil
}

/**
 * decompress the 'compressed', and then json decode it to the obj
 */
func JsonDecompress(logid int64, compressed interface{}, obj interface{}) error {
	var b []byte
	switch t := compressed.(type) {
	case string:
		b = []byte(t)
	case []byte:
		b = t
	case nil:
		return nil
	default:
		log.Errorf("%d, Unsupported compressed's type: %v", logid, t)
		return errors.New("Not supported compressed's type")
	}
	buf := bytes.NewBuffer(b)
	var gzipReader *gzip.Reader
	defer func() {
		if gzipReader != nil {
			gzipReaderPool.Put(gzipReader)
		}
	}()
	var err error
	if r := gzipReaderPool.Get(); r != nil {
		gzipReader = r.(*gzip.Reader)
		if err = gzipReader.Reset(buf); err != nil {
			log.Errorf("%d, err while gzip reader reset, err: %v", logid, err)
			return err
		}
	} else {
		if gzipReader, err = gzip.NewReader(buf); err != nil {
			log.Errorf("%d, err while new reader, err: %v", logid, err)
			return err
		}
	}

	decoder := json.NewDecoder(gzipReader)
	decoder.UseNumber()
	if err := decoder.Decode(obj); err != nil {
		log.Errorf("%d, err while json decompress, err: %v", logid, err)
		//return err
	}
	if err := gzipReader.Close(); err != nil {
		log.Errorf("%d, err while close gzip reader, err: %v", logid, err)
		return err
	}
	return nil
}
