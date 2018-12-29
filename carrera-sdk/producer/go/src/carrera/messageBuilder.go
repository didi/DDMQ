package carrera

import (
	"carrera/common"
	"carrera/CarreraProducer"
	"carrera/common/util"
)

type MessageBuilder struct {
	producer sender
	msg      *CarreraProducer.Message
}

func NewMessageBuilder(producer sender) *MessageBuilder {
	this := &MessageBuilder{}
	this.initMsg(producer)

	return this
}

func (this *MessageBuilder) initMsg(producer sender) {
	this.msg = CarreraProducer.NewMessage()
	this.producer = producer
	this.msg.Properties = make(map[string]string)
	curSdkVersion := common.CUR_SDK_VERSION
	this.msg.Version = &curSdkVersion
	this.msg.PartitionId = PARTITION_RAND
}

func (this *MessageBuilder) SetTopic(topic string) *MessageBuilder {
	this.msg.Topic = topic
	return this
}

func (this *MessageBuilder) SetPartitionId(partitionId int32) *MessageBuilder {
	this.msg.PartitionId = partitionId
	return this
}

func (this *MessageBuilder) SetRandomPartition() *MessageBuilder {
	this.msg.PartitionId = PARTITION_RAND
	return this
}

func (this *MessageBuilder) SetHashId(hashId int64) *MessageBuilder {
	this.msg.PartitionId = PARTITION_HASH
	this.msg.HashId = hashId
	return this
}

func (this *MessageBuilder) SetBody(body interface{}) *MessageBuilder {
	if body != nil {
		switch b := body.(type) {
		case []byte:
			this.msg.Body = b
		case string:
			this.msg.Body = []byte(b)
		}
	}
	return this
}

func (this *MessageBuilder) SetKey(key string) *MessageBuilder {
	if key != "" {
		this.msg.Key = key
	}
	return this
}

func (this *MessageBuilder) SetTags(tag string) *MessageBuilder {
	if tag != "" {
		this.msg.Tags = tag
	}

	return this
}

func (this *MessageBuilder) SetTraceId(traceId string) *MessageBuilder {
	this.msg.Properties[CarreraProducer.TRACE_ID] = traceId
	return this
}

func (this *MessageBuilder) SetSpanId(spanId string) *MessageBuilder {
	this.msg.Properties[CarreraProducer.SPAN_ID] = spanId
	return this
}

func (this *MessageBuilder) SetPressureTraffic(isOpen bool) *MessageBuilder {
	if isOpen {
		this.msg.Properties[CarreraProducer.PRESSURE_TRAFFIC_KEY] = CarreraProducer.PRESSURE_TRAFFIC_ENABLE
	} else {
		this.msg.Properties[CarreraProducer.PRESSURE_TRAFFIC_KEY] = CarreraProducer.PRESSURE_TRAFFIC_DISABLE
	}
	return this
}

func (this *MessageBuilder) AddProperty(key, value string) *MessageBuilder {
	if key != "" && key != CarreraProducer.PRESSURE_TRAFFIC_KEY {
		this.msg.Properties[key] = value
	}
	return this
}

func (this *MessageBuilder) Send() *CarreraProducer.Result {
	if this.msg.Key == "" {
		this.SetKey(util.GenRandKey())
	}
	return this.producer.sendMessage(this.msg)
}
