package com.xiaojukeji.carrera.nodemgr.connection;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.nodemgr.Node;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;


public class CarreraConnectionFactory extends BaseKeyedPooledObjectFactory<Node, CarreraConnection> {

    private CarreraConfig carreraConfig;

    public CarreraConnectionFactory(CarreraConfig carreraConfig) {
        super();
        this.carreraConfig = carreraConfig;
    }

    @Override
    public CarreraConnection create(Node key) throws Exception {
        return new CarreraConnection(key, this.carreraConfig.getCarreraClientTimeout());
    }

    @Override
    public PooledObject<CarreraConnection> wrap(CarreraConnection value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public PooledObject<CarreraConnection> makeObject(Node key) throws Exception {
        return super.makeObject(key);
    }

    @Override
    public void destroyObject(Node key, PooledObject<CarreraConnection> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public boolean validateObject(Node key, PooledObject<CarreraConnection> p) {
        return p.getObject().validate();

    }

    @Override
    public void activateObject(Node key, PooledObject<CarreraConnection> p) throws Exception {
        super.activateObject(key, p);
    }

    @Override
    public void passivateObject(Node key, PooledObject<CarreraConnection> p) throws Exception {
        super.passivateObject(key, p);
    }

}