package dlog

import (
	"fmt"
	"io"
	"log/syslog"
	"os"
	"path"
	"path/filepath"
	"runtime"

	"go.intra.xiaojukeji.com/golang/logrus"
	logrus_syslog "go.intra.xiaojukeji.com/golang/logrus/hooks/syslog"
	"go.intra.xiaojukeji.com/golang/lumberjack"
)

const (
	// Fractional seconds can be printed by adding a run of 0s or 9s after
	// a decimal point in the seconds value in the layout string.
	// If the layout digits are 0s, the fractional second is of the specified
	// width. Note that the output has a trailing zero.
	// If the fraction in the layout is 9s, trailing zeros are dropped.
	TimestampFormat = "2006-01-02T15:04:05.000"
)

var Logger *DLogger

func init() {
	lumberjack.BackupTimeFormat = "20060102150405"
	CreateLocalLog("./all.log", "debug", 100, 10, "text")
}

type DLogger struct {
	*logrus.Logger
	output      io.Writer
	hookWriters []io.Writer
}

type ContextHook struct {
}

func (hook ContextHook) Levels() []logrus.Level {
	return logrus.AllLevels
}

func (hook ContextHook) Fire(entry *logrus.Entry) error {
	if pc, file, line, ok := runtime.Caller(8); ok {
		funcName := runtime.FuncForPC(pc).Name()

		entry.Data["file"] = path.Base(file)
		entry.Data["func"] = path.Base(funcName)
		entry.Data["line"] = line
	}

	return nil
}

func (lw *DLogger) Close() {
	if c, ok := lw.output.(io.Closer); ok {
		c.Close()
	}
	for _, hw := range lw.hookWriters {
		if c, ok := hw.(io.Closer); ok {
			c.Close()
		}
	}
}

func (lw *DLogger) SetKeySeparator(separator string) {
	switch f := lw.Formatter.(type) {
	case *logrus.TextFormatter:
		f.KeySeparator = separator
	default:
	}
}

func (lw *DLogger) SetDisableAutoAddKey(disable bool) {
	switch f := lw.Formatter.(type) {
	case *logrus.TextFormatter:
		f.DisableAutoAddedKey = disable
	default:
	}
}

func (lw *DLogger) SetDisableQuoting(disable bool) {
	switch f := lw.Formatter.(type) {
	case *logrus.TextFormatter:
		f.DisableQuoting = disable
	default:
	}
}

func (lw *DLogger) ShowFileLine(enable bool) {
	if enable {
		lw.Hooks.Add(ContextHook{})
	}
}

/**
* formatter: 'json' or 'text', default is 'text'
 */
func CreateSyslog(level string, formatter string, debug bool) *DLogger {
	var output io.Writer
	if !debug {
		output = newSyslogOutput()
	} else {
		output = os.Stdout
	}
	l := newLogrus(level, formatter, output)
	Logger = &DLogger{l, output, nil}
	return Logger
}

/**
* formatter: 'json' or 'text', default is 'text'
 */
func CreateLocalLog(logFilePath string, level string, maxSizeMB int, maxBackups int, formatter string) *DLogger {
	output := newLumberjack(logFilePath, maxSizeMB, maxBackups)
	l := newLogrus(level, formatter, output)
	logDir := filepath.Dir(logFilePath)
	errorLogFilePath := filepath.Join(logDir, "error.log")
	hookWriter := newLumberjack(errorLogFilePath, maxSizeMB, maxBackups)
	errorHook := &ErrorHook{Writer: hookWriter}
	l.Hooks.Add(errorHook)
	Logger = &DLogger{l, output, []io.Writer{errorHook.Writer}}
	return Logger
}

func newSyslogOutput() io.WriteCloser {
	output, err := logrus_syslog.NewSyslogHook("", "", syslog.LOG_DEBUG, "")
	//output, err := logrus_syslog.NewSyslogHook("tcp", "localhost:514", syslog.LOG_DEBUG, "")
	if err != nil {
		fmt.Printf("Error while new syslog: %v", err)
		panic("Error while new syslog")
	}
	return output.Writer
}

func newLumberjack(filename string, maxSizeMB int, maxBackups int) io.WriteCloser {
	return &lumberjack.Logger{
		Filename:   filename,
		MaxSize:    maxSizeMB, // megabytes
		MaxBackups: maxBackups,
		//MaxAge:     28, //days
		LocalTime: true,
	}
}

/**
* formatter: 'json' or 'text', default is 'text'
 */
func newLogrus(level string, formatter string, output io.Writer) *logrus.Logger {
	l, err := logrus.ParseLevel(level)
	if err != nil {
		fmt.Printf("Bad level: %v, set it to 'debug'", level)
		l = logrus.DebugLevel
	}
	logger := &logrus.Logger{
		Out:   output,
		Hooks: make(logrus.LevelHooks),
		Level: l,
	}
	switch formatter {
	case "json":
		logger.Formatter = &logrus.JSONFormatter{TimestampFormat: TimestampFormat}
	case "text":
		fallthrough
	default:
		logger.Formatter = &logrus.TextFormatter{DisableColors: true,
			DisableSorting: false, TimestampFormat: TimestampFormat}
	}
	return logger
}

type ErrorHook struct {
	Writer io.Writer
}

func (hook *ErrorHook) Fire(entry *logrus.Entry) error {
	serialized, err := entry.Logger.Formatter.Format(entry)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Unable to read entry, %v", err)
		return err
	}
	_, err = hook.Writer.Write(serialized)
	return err
}

func (hook *ErrorHook) Levels() []logrus.Level {
	return []logrus.Level{logrus.PanicLevel, logrus.FatalLevel, logrus.ErrorLevel}

}

// Adds a field to the log entry, note that it doesn't log until you call
// Debug, Print, Info, Warn, Fatal or Panic. It only creates a log entry.
// If you want multiple fields, use `WithFields`.
func WithField(key string, value interface{}) *logrus.Entry {
	return Logger.WithField(key, value)
}

// Adds a struct of fields to the log entry. All it does is call `WithField` for
// each `Field`.
func WithFields(fields logrus.Fields) *logrus.Entry {
	return Logger.WithFields(fields)
}

// Add an error as single field to the log entry.  All it does is call
// `WithError` for the given `error`.
func WithError(err error) *logrus.Entry {
	return Logger.WithError(err)
}

func Debugf(format string, args ...interface{}) {
	Logger.Debugf(format, args...)
}

func Infof(format string, args ...interface{}) {
	Logger.Infof(format, args...)
}

func Printf(format string, args ...interface{}) {
	Logger.Printf(format, args...)
}

func Warnf(format string, args ...interface{}) {
	Logger.Warnf(format, args...)
}

func Warningf(format string, args ...interface{}) {
	Logger.Warningf(format, args...)
}

func Errorf(format string, args ...interface{}) {
	Logger.Errorf(format, args...)
}

func Fatalf(format string, args ...interface{}) {
	Logger.Fatalf(format, args...)
}

func Panicf(format string, args ...interface{}) {
	Logger.Panicf(format, args...)
}

func Debug(args ...interface{}) {
	Logger.Debug(args...)
}

func Info(args ...interface{}) {
	Logger.Info(args...)
}

func Print(args ...interface{}) {
	Logger.Print(args...)
}

func Warn(args ...interface{}) {
	Logger.Warn(args...)
}

func Warning(args ...interface{}) {
	Logger.Warning(args...)
}

func Error(args ...interface{}) {
	Logger.Error(args...)
}

func Fatal(args ...interface{}) {
	Logger.Fatal(args...)
}

func Panic(args ...interface{}) {
	Logger.Panic(args...)
}

func Debugln(args ...interface{}) {
	Logger.Debugln(args...)
}

func Infoln(args ...interface{}) {
	Logger.Infoln(args...)
}

func Println(args ...interface{}) {
	Logger.Println(args...)
}

func Warnln(args ...interface{}) {
	Logger.Warnln(args...)
}

func Warningln(args ...interface{}) {
	Logger.Warningln(args...)
}

func Errorln(args ...interface{}) {
	Logger.Errorln(args...)
}

func Fatalln(args ...interface{}) {
	Logger.Fatalln(args...)
}

func Panicln(args ...interface{}) {
	Logger.Panicln(args...)
}

func Close() {
	Logger.Close()
}
