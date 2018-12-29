package carrera

import "carrera/CarreraProducer"

type TxBusinessMessageBuilder struct {
	messageBuilder *MessageBuilder
}

func NewTxBusinessMessageBuilder(messageBuilder *MessageBuilder) *TxBusinessMessageBuilder {
	return &TxBusinessMessageBuilder{messageBuilder:messageBuilder}
}

func (txBusinessMessageBuilder *TxBusinessMessageBuilder) Send() *CarreraProducer.Result {
	return txBusinessMessageBuilder.messageBuilder.Send()
}
