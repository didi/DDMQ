package carrera

import (
	"encoding/json"
	"carrera/CarreraProducer"
	"strings"
)

type AddDelayMessageBuilder struct {
	producer   sender
	delayMeta  *CarreraProducer.DelayMeta
	properties map[string]string
	headers    map[string]string
	topic      string
	body       []byte
	tags       string
}

func NewAddDelayMessageBuilder(producer sender) *AddDelayMessageBuilder {
	this := &AddDelayMessageBuilder{}
	this.initMsg(producer)
	return this
}

func (this *AddDelayMessageBuilder) initMsg(producer sender) {
	this.producer = producer
	this.properties = make(map[string]string)
	this.headers = make(map[string]string)
}

func (this *AddDelayMessageBuilder) SetTopic(topic string) *AddDelayMessageBuilder {
	this.topic = topic
	return this
}

func (this *AddDelayMessageBuilder) SetBody(body interface{}) *AddDelayMessageBuilder {
	if body != nil {
		switch b := body.(type) {
		case []byte:
			this.body = b
		case string:
			this.body = []byte(b)
		}
	}
	return this
}

func (this *AddDelayMessageBuilder) SetDelayMeta(delayMeta *CarreraProducer.DelayMeta) *AddDelayMessageBuilder {
	this.delayMeta = delayMeta
	return this
}

func (this *AddDelayMessageBuilder) SetTags(tag string) *AddDelayMessageBuilder {
	if tag != "" {
		this.tags = tag
	}

	return this
}

func (this *AddDelayMessageBuilder) AddProperty(key, value string) *AddDelayMessageBuilder {
	if key != "" && key != CarreraProducer.PRESSURE_TRAFFIC_KEY {
		this.properties[key] = value
	}
	return this
}

func (this *AddDelayMessageBuilder) AddHeader(key, value string) *AddDelayMessageBuilder {
	if key != "" {
		if strings.EqualFold(key, CarreraProducer.DIDI_HEADER_RID) {
			this.SetTraceId(value)
		} else if strings.EqualFold(key, CarreraProducer.DIDI_HEADER_SPANID) {
			this.SetSpanId(value)
		} else {
			if !strings.EqualFold(key, CarreraProducer.CARRERA_HEADERS) {
				this.headers[key] = value
			}
		}
	}

	return this
}

func (this *AddDelayMessageBuilder) SetTraceId(traceId string) *AddDelayMessageBuilder {
	this.properties[CarreraProducer.TRACE_ID] = traceId
	return this
}

func (this *AddDelayMessageBuilder) SetSpanId(spanId string) *AddDelayMessageBuilder {
	this.properties[CarreraProducer.SPAN_ID] = spanId
	return this
}

func (this *AddDelayMessageBuilder) SetPressureTraffic(isOpen bool) *AddDelayMessageBuilder {
	if isOpen {
		this.properties[CarreraProducer.PRESSURE_TRAFFIC_KEY] = CarreraProducer.PRESSURE_TRAFFIC_ENABLE
	} else {
		this.properties[CarreraProducer.PRESSURE_TRAFFIC_KEY] = CarreraProducer.PRESSURE_TRAFFIC_DISABLE
	}
	return this
}

func (this *AddDelayMessageBuilder) Send() *CarreraProducer.DelayResult {
	if this.delayMeta != nil {
		if len(this.headers) > 0 {
			value, err := json.Marshal(this.headers)
			if err == nil {
				this.AddProperty(CarreraProducer.CARRERA_HEADERS, string(value))
			}
		}

		if len(this.delayMeta.Properties) <= 0 {
			this.delayMeta.Properties = this.properties
		} else {
			for k, v := range this.properties {
				this.delayMeta.Properties[k] = v
			}
		}
	}

	return this.producer.sendDelay(this.topic, this.body, this.delayMeta, this.tags)
}
