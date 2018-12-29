PACKAGE DOCUMENTATION

package dlog
    import "."


VARIABLES

var SyslogPriorityMap = map[string]syslog.Priority{
    "local0": syslog.LOG_LOCAL0,
    "local1": syslog.LOG_LOCAL1,
    "local2": syslog.LOG_LOCAL2,
    "local3": syslog.LOG_LOCAL3,
    "local4": syslog.LOG_LOCAL4,
    "local5": syslog.LOG_LOCAL5,
    "local6": syslog.LOG_LOCAL6,
    "local7": syslog.LOG_LOCAL7,
}

FUNCTIONS

func Close()

func Debug(args ...interface{})

func Debugf(format string, args ...interface{})

func DialSyslogBackend(network, raddr string, priority syslog.Priority, tag string) (*syslogBackend, error)

func Error(args ...interface{})

func Errorf(format string, args ...interface{})

func Fall()

func Fatal(args ...interface{})

func Fatalf(format string, args ...interface{})

func Info(args ...interface{})

func Infof(format string, args ...interface{})

func Init(config LogConfig) error

func LogToStderr()

func NewMultiBackend(bes ...Backend) (*multiBackend, error)

func NewSyslogBackend(priority syslog.Priority, tag string) (*syslogBackend, error)

func Rotate(rotateNum1 int, maxSize1 uint64)

func SetFlushDuration(t time.Duration)

func SetLogging(level interface{}, backend Backend)

func SetSeverity(level interface{})

func Warning(args ...interface{})

func Warningf(format string, args ...interface{})

TYPES

type Backend interface {
    Log(s Severity, msg []byte)
    // contains filtered or unexported methods
}

type FileBackend struct {
    // contains filtered or unexported fields
}

func NewFileBackend(dir string) (*FileBackend, error)

func (self *FileBackend) Flush()

func (self *FileBackend) Log(s Severity, msg []byte)

type LogConfig struct {
    Type              string // syslog/stderr/std/file
    Level             string // DEBUG/INFO/WARNING/ERROR/FATAL
    SyslogPriority    string // local0-7
    SyslogSeverity    string
    FileName          string
    FileRotateCount   int
    FileRotateSize    uint64
    FileFlushDuration time.Duration
}

type Logger struct {
    // contains filtered or unexported fields
}

func NewLogger(level interface{}, backend Backend) *Logger

func (l *Logger) Close()

func (l *Logger) Debug(args ...interface{})

func (l *Logger) Debugf(format string, args ...interface{})

func (l *Logger) Error(args ...interface{})

func (l *Logger) Errorf(format string, args ...interface{})

func (l *Logger) Fatal(args ...interface{})

func (l *Logger) Fatalf(format string, args ...interface{})

func (l *Logger) Info(args ...interface{})

func (l *Logger) Infof(format string, args ...interface{})

func (l *Logger) LogToStderr()

func (l *Logger) SetSeverity(level interface{})

func (l *Logger) Warning(args ...interface{})

func (l *Logger) Warningf(format string, args ...interface{})

type Severity int

const (
    FATAL Severity = iota
    ERROR
    WARNING
    INFO
    DEBUG
)# dlog
--
    import "go.intra.xiaojukeji.com/golang/dlog"


## Usage

```go
var SyslogPriorityMap = map[string]syslog.Priority{
	"local0": syslog.LOG_LOCAL0,
	"local1": syslog.LOG_LOCAL1,
	"local2": syslog.LOG_LOCAL2,
	"local3": syslog.LOG_LOCAL3,
	"local4": syslog.LOG_LOCAL4,
	"local5": syslog.LOG_LOCAL5,
	"local6": syslog.LOG_LOCAL6,
	"local7": syslog.LOG_LOCAL7,
}
```

#### func  Close

```go
func Close()
```

#### func  Debug

```go
func Debug(args ...interface{})
```

#### func  Debugf

```go
func Debugf(format string, args ...interface{})
```

#### func  DialSyslogBackend

```go
func DialSyslogBackend(network, raddr string, priority syslog.Priority, tag string) (*syslogBackend, error)
```

#### func  Error

```go
func Error(args ...interface{})
```

#### func  Errorf

```go
func Errorf(format string, args ...interface{})
```

#### func  Fall

```go
func Fall()
```

#### func  Fatal

```go
func Fatal(args ...interface{})
```

#### func  Fatalf

```go
func Fatalf(format string, args ...interface{})
```

#### func  Info

```go
func Info(args ...interface{})
```

#### func  Infof

```go
func Infof(format string, args ...interface{})
```

#### func  Init

```go
func Init(config LogConfig) error
```

#### func  LogToStderr

```go
func LogToStderr()
```

#### func  NewMultiBackend

```go
func NewMultiBackend(bes ...Backend) (*multiBackend, error)
```

#### func  NewSyslogBackend

```go
func NewSyslogBackend(priority syslog.Priority, tag string) (*syslogBackend, error)
```

#### func  Rotate

```go
func Rotate(rotateNum1 int, maxSize1 uint64)
```

#### func  SetFlushDuration

```go
func SetFlushDuration(t time.Duration)
```

#### func  SetLogging

```go
func SetLogging(level interface{}, backend Backend)
```

#### func  SetSeverity

```go
func SetSeverity(level interface{})
```

#### func  Warning

```go
func Warning(args ...interface{})
```

#### func  Warningf

```go
func Warningf(format string, args ...interface{})
```

#### type Backend

```go
type Backend interface {
	Log(s Severity, msg []byte)
	// contains filtered or unexported methods
}
```


#### type FileBackend

```go
type FileBackend struct {
}
```


#### func  NewFileBackend

```go
func NewFileBackend(dir string) (*FileBackend, error)
```

#### func (*FileBackend) Flush

```go
func (self *FileBackend) Flush()
```

#### func (*FileBackend) Log

```go
func (self *FileBackend) Log(s Severity, msg []byte)
```

#### type LogConfig

```go
type LogConfig struct {
	Type              string // syslog/stderr/std/file
	Level             string // DEBUG/INFO/WARNING/ERROR/FATAL
	SyslogPriority    string // local0-7
	SyslogSeverity    string
	FileName          string
	FileRotateCount   int
	FileRotateSize    uint64
	FileFlushDuration time.Duration
}
```


#### type Logger

```go
type Logger struct {
}
```


#### func  NewLogger

```go
func NewLogger(level interface{}, backend Backend) *Logger
```

#### func (*Logger) Close

```go
func (l *Logger) Close()
```

#### func (*Logger) Debug

```go
func (l *Logger) Debug(args ...interface{})
```

#### func (*Logger) Debugf

```go
func (l *Logger) Debugf(format string, args ...interface{})
```

#### func (*Logger) Error

```go
func (l *Logger) Error(args ...interface{})
```

#### func (*Logger) Errorf

```go
func (l *Logger) Errorf(format string, args ...interface{})
```

#### func (*Logger) Fatal

```go
func (l *Logger) Fatal(args ...interface{})
```

#### func (*Logger) Fatalf

```go
func (l *Logger) Fatalf(format string, args ...interface{})
```

#### func (*Logger) Info

```go
func (l *Logger) Info(args ...interface{})
```

#### func (*Logger) Infof

```go
func (l *Logger) Infof(format string, args ...interface{})
```

#### func (*Logger) LogToStderr

```go
func (l *Logger) LogToStderr()
```

#### func (*Logger) SetSeverity

```go
func (l *Logger) SetSeverity(level interface{})
```

#### func (*Logger) Warning

```go
func (l *Logger) Warning(args ...interface{})
```

#### func (*Logger) Warningf

```go
func (l *Logger) Warningf(format string, args ...interface{})
```

#### type Severity

```go
type Severity int
```


```go
const (
	FATAL Severity = iota
	ERROR
	WARNING
	INFO
	DEBUG
)
```
