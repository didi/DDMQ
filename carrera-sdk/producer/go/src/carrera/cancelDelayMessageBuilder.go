package carrera

import (
	"carrera/CarreraProducer"
)

type CancelDelayMessageBuilder struct {
	producer       sender
	topic          string
	uniqDelayMsgId string
	tags           string
}

func NewCancelDelayMessageBuilder(producer sender) *CancelDelayMessageBuilder {
	this := &CancelDelayMessageBuilder{}
	this.initMsg(producer)
	return this
}

func (this *CancelDelayMessageBuilder) initMsg(producer sender) {
	this.producer = producer
}

func (this *CancelDelayMessageBuilder) SetTopic(topic string) *CancelDelayMessageBuilder {
	this.topic = topic
	return this
}

func (this *CancelDelayMessageBuilder) SetUniqDelayMsgId(uniqDelayMsgId string) *CancelDelayMessageBuilder {
	this.uniqDelayMsgId = uniqDelayMsgId
	return this
}

func (this *CancelDelayMessageBuilder) SetTags(tag string) *CancelDelayMessageBuilder {
	this.tags = tag
	return this
}

func (this *CancelDelayMessageBuilder) Send() *CarreraProducer.DelayResult {
	return this.producer.cancelDelay(this.topic, this.uniqDelayMsgId, this.tags)
}
