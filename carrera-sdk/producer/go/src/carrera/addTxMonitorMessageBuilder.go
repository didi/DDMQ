package carrera

import "carrera/CarreraProducer"

type AddTxMonitorMessageBuilder struct {
	addDelayMessageBuilder *AddDelayMessageBuilder
}

func NewAddTxMonitorMessageBuilder(addDelayMessageBuilder *AddDelayMessageBuilder) *AddTxMonitorMessageBuilder {
	return &AddTxMonitorMessageBuilder{addDelayMessageBuilder:addDelayMessageBuilder}
}

func (addTxMonitorMessageBuilder *AddTxMonitorMessageBuilder) Send() *CarreraProducer.DelayResult {
	return addTxMonitorMessageBuilder.addDelayMessageBuilder.Send()
}
