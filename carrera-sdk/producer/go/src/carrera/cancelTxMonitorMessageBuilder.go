package carrera

import "carrera/CarreraProducer"

type CancelTxMonitorMessageBuilder struct {
	cancelDelayMessageBuilder *CancelDelayMessageBuilder
}

func NewCancelTxMonitorMessageBuilder(cancelDelayMessageBuilder *CancelDelayMessageBuilder) *CancelTxMonitorMessageBuilder {
	return &CancelTxMonitorMessageBuilder{cancelDelayMessageBuilder:cancelDelayMessageBuilder}
}

func (cancelTxMonitorMessageBuilder *CancelTxMonitorMessageBuilder) Send() *CarreraProducer.DelayResult {
	return cancelTxMonitorMessageBuilder.cancelDelayMessageBuilder.Send()
}
