package com.xiaojukeji.carrera.nodemgr.connection;

import com.xiaojukeji.carrera.nodemgr.Node;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.*;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;


public class CarreraConnection {

    private ProducerService.Client thriftClient;
    private Node node;
    private int clientTimeout;
    private TSocket socket;

    private static final boolean OPEN_SSL = Boolean.valueOf(System.getProperty("carrera.ssl.open", "false"));
    private static final String TRUST_STORE = System.getProperty("carrera.ssl.trustStore", "");
    private static final String TRUST_PASS = System.getProperty("carrera.ssl.trustPass", "");


    public CarreraConnection(Node node, int clientTimeout) {
        this.node = node;
        this.clientTimeout = clientTimeout;
    }

    private ProducerService.Client createThriftClient() throws TTransportException {
        TSocket socket;
        if (OPEN_SSL) {
            TSSLTransportFactory.TSSLTransportParameters parameters = new TSSLTransportFactory.TSSLTransportParameters();
            parameters.setTrustStore(TRUST_STORE, TRUST_PASS);
            socket = TSSLTransportFactory.getClientSocket(node.getIp(), node.getPort(), clientTimeout, parameters);
        } else {
            socket = new TSocket(node.getIp(), node.getPort(), clientTimeout);
            socket.open();
        }

        TTransport transport = new TFramedTransport(socket);
        TProtocol protocol = new TCompactProtocol(transport);
        return new ProducerService.Client(protocol);
    }

    public synchronized Result send(Message msg, long proxyTimeout) {
        Result ret = new Result();
        try {
            if (this.thriftClient == null) {
                this.thriftClient = createThriftClient();
            }
            ret = thriftClient.sendSync(msg, proxyTimeout);

        } catch (TTransportException | TProtocolException te) {
            ret.setCode(CarreraReturnCode.THRIFT_NETWORK_EXCEPTION);
            ret.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        } catch (Exception te) {
            ret.setCode(CarreraReturnCode.THRIFT_EXCEPTION);
            ret.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        }
        return ret;
    }

    public synchronized DelayResult sendDelay(DelayMessage delayMessage, long proxyTimeout) {
        DelayResult result = new DelayResult();
        try {
            if (this.thriftClient == null) {
                this.thriftClient = createThriftClient();
            }
            result = thriftClient.sendDelaySync(delayMessage, proxyTimeout);

        } catch (TTransportException | TProtocolException te) {
            result.setCode(CarreraReturnCode.THRIFT_NETWORK_EXCEPTION);
            result.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        } catch (Exception te) {
            result.setCode(CarreraReturnCode.THRIFT_EXCEPTION);
            result.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        }
        return result;
    }

    public synchronized Result sendBatchSync(List<Message> messages) {
        Result ret = new Result();
        try {
            if (this.thriftClient == null) {
                this.thriftClient = createThriftClient();
            }
            ret = thriftClient.sendBatchSync(messages);

        } catch (TTransportException | TProtocolException te) {
            ret.setCode(CarreraReturnCode.THRIFT_NETWORK_EXCEPTION);
            ret.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        } catch (Exception te) {
            ret.setCode(CarreraReturnCode.THRIFT_EXCEPTION);
            ret.setMsg(te.getClass().getSimpleName() + " - " + te.getMessage());
            close();
        }
        return ret;
    }

    public synchronized void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        this.thriftClient = null;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public synchronized boolean validate() {
        return socket == null || !socket.isOpen() || this.thriftClient == null;
    }

}