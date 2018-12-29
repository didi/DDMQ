/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.xiaojukeji.carrera.consumer.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2018-2-23")
public class ConsumeStats implements org.apache.thrift.TBase<ConsumeStats, ConsumeStats._Fields>, java.io.Serializable, Cloneable, Comparable<ConsumeStats> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConsumeStats");

  private static final org.apache.thrift.protocol.TField GROUP_FIELD_DESC = new org.apache.thrift.protocol.TField("group", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField TOPIC_FIELD_DESC = new org.apache.thrift.protocol.TField("topic", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField CONSUME_OFFSETS_FIELD_DESC = new org.apache.thrift.protocol.TField("consumeOffsets", org.apache.thrift.protocol.TType.MAP, (short)3);
  private static final org.apache.thrift.protocol.TField PRODUCE_OFFSETS_FIELD_DESC = new org.apache.thrift.protocol.TField("produceOffsets", org.apache.thrift.protocol.TType.MAP, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ConsumeStatsStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ConsumeStatsTupleSchemeFactory());
  }

  public String group; // required
  public String topic; // required
  public Map<String,Long> consumeOffsets; // required
  public Map<String,Long> produceOffsets; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    GROUP((short)1, "group"),
    TOPIC((short)2, "topic"),
    CONSUME_OFFSETS((short)3, "consumeOffsets"),
    PRODUCE_OFFSETS((short)4, "produceOffsets");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // GROUP
          return GROUP;
        case 2: // TOPIC
          return TOPIC;
        case 3: // CONSUME_OFFSETS
          return CONSUME_OFFSETS;
        case 4: // PRODUCE_OFFSETS
          return PRODUCE_OFFSETS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.GROUP, new org.apache.thrift.meta_data.FieldMetaData("group", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.TOPIC, new org.apache.thrift.meta_data.FieldMetaData("topic", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONSUME_OFFSETS, new org.apache.thrift.meta_data.FieldMetaData("consumeOffsets", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64))));
    tmpMap.put(_Fields.PRODUCE_OFFSETS, new org.apache.thrift.meta_data.FieldMetaData("produceOffsets", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConsumeStats.class, metaDataMap);
  }

  public ConsumeStats() {
  }

  public ConsumeStats(
    String group,
    String topic,
    Map<String,Long> consumeOffsets,
    Map<String,Long> produceOffsets)
  {
    this();
    this.group = group;
    this.topic = topic;
    this.consumeOffsets = consumeOffsets;
    this.produceOffsets = produceOffsets;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ConsumeStats(ConsumeStats other) {
    if (other.isSetGroup()) {
      this.group = other.group;
    }
    if (other.isSetTopic()) {
      this.topic = other.topic;
    }
    if (other.isSetConsumeOffsets()) {
      Map<String,Long> __this__consumeOffsets = new HashMap<String,Long>(other.consumeOffsets);
      this.consumeOffsets = __this__consumeOffsets;
    }
    if (other.isSetProduceOffsets()) {
      Map<String,Long> __this__produceOffsets = new HashMap<String,Long>(other.produceOffsets);
      this.produceOffsets = __this__produceOffsets;
    }
  }

  public ConsumeStats deepCopy() {
    return new ConsumeStats(this);
  }

  @Override
  public void clear() {
    this.group = null;
    this.topic = null;
    this.consumeOffsets = null;
    this.produceOffsets = null;
  }

  public String getGroup() {
    return this.group;
  }

  public ConsumeStats setGroup(String group) {
    this.group = group;
    return this;
  }

  public void unsetGroup() {
    this.group = null;
  }

  /** Returns true if field group is set (has been assigned a value) and false otherwise */
  public boolean isSetGroup() {
    return this.group != null;
  }

  public void setGroupIsSet(boolean value) {
    if (!value) {
      this.group = null;
    }
  }

  public String getTopic() {
    return this.topic;
  }

  public ConsumeStats setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public void unsetTopic() {
    this.topic = null;
  }

  /** Returns true if field topic is set (has been assigned a value) and false otherwise */
  public boolean isSetTopic() {
    return this.topic != null;
  }

  public void setTopicIsSet(boolean value) {
    if (!value) {
      this.topic = null;
    }
  }

  public int getConsumeOffsetsSize() {
    return (this.consumeOffsets == null) ? 0 : this.consumeOffsets.size();
  }

  public void putToConsumeOffsets(String key, long val) {
    if (this.consumeOffsets == null) {
      this.consumeOffsets = new HashMap<String,Long>();
    }
    this.consumeOffsets.put(key, val);
  }

  public Map<String,Long> getConsumeOffsets() {
    return this.consumeOffsets;
  }

  public ConsumeStats setConsumeOffsets(Map<String,Long> consumeOffsets) {
    this.consumeOffsets = consumeOffsets;
    return this;
  }

  public void unsetConsumeOffsets() {
    this.consumeOffsets = null;
  }

  /** Returns true if field consumeOffsets is set (has been assigned a value) and false otherwise */
  public boolean isSetConsumeOffsets() {
    return this.consumeOffsets != null;
  }

  public void setConsumeOffsetsIsSet(boolean value) {
    if (!value) {
      this.consumeOffsets = null;
    }
  }

  public int getProduceOffsetsSize() {
    return (this.produceOffsets == null) ? 0 : this.produceOffsets.size();
  }

  public void putToProduceOffsets(String key, long val) {
    if (this.produceOffsets == null) {
      this.produceOffsets = new HashMap<String,Long>();
    }
    this.produceOffsets.put(key, val);
  }

  public Map<String,Long> getProduceOffsets() {
    return this.produceOffsets;
  }

  public ConsumeStats setProduceOffsets(Map<String,Long> produceOffsets) {
    this.produceOffsets = produceOffsets;
    return this;
  }

  public void unsetProduceOffsets() {
    this.produceOffsets = null;
  }

  /** Returns true if field produceOffsets is set (has been assigned a value) and false otherwise */
  public boolean isSetProduceOffsets() {
    return this.produceOffsets != null;
  }

  public void setProduceOffsetsIsSet(boolean value) {
    if (!value) {
      this.produceOffsets = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case GROUP:
      if (value == null) {
        unsetGroup();
      } else {
        setGroup((String)value);
      }
      break;

    case TOPIC:
      if (value == null) {
        unsetTopic();
      } else {
        setTopic((String)value);
      }
      break;

    case CONSUME_OFFSETS:
      if (value == null) {
        unsetConsumeOffsets();
      } else {
        setConsumeOffsets((Map<String,Long>)value);
      }
      break;

    case PRODUCE_OFFSETS:
      if (value == null) {
        unsetProduceOffsets();
      } else {
        setProduceOffsets((Map<String,Long>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case GROUP:
      return getGroup();

    case TOPIC:
      return getTopic();

    case CONSUME_OFFSETS:
      return getConsumeOffsets();

    case PRODUCE_OFFSETS:
      return getProduceOffsets();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case GROUP:
      return isSetGroup();
    case TOPIC:
      return isSetTopic();
    case CONSUME_OFFSETS:
      return isSetConsumeOffsets();
    case PRODUCE_OFFSETS:
      return isSetProduceOffsets();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ConsumeStats)
      return this.equals((ConsumeStats)that);
    return false;
  }

  public boolean equals(ConsumeStats that) {
    if (that == null)
      return false;

    boolean this_present_group = true && this.isSetGroup();
    boolean that_present_group = true && that.isSetGroup();
    if (this_present_group || that_present_group) {
      if (!(this_present_group && that_present_group))
        return false;
      if (!this.group.equals(that.group))
        return false;
    }

    boolean this_present_topic = true && this.isSetTopic();
    boolean that_present_topic = true && that.isSetTopic();
    if (this_present_topic || that_present_topic) {
      if (!(this_present_topic && that_present_topic))
        return false;
      if (!this.topic.equals(that.topic))
        return false;
    }

    boolean this_present_consumeOffsets = true && this.isSetConsumeOffsets();
    boolean that_present_consumeOffsets = true && that.isSetConsumeOffsets();
    if (this_present_consumeOffsets || that_present_consumeOffsets) {
      if (!(this_present_consumeOffsets && that_present_consumeOffsets))
        return false;
      if (!this.consumeOffsets.equals(that.consumeOffsets))
        return false;
    }

    boolean this_present_produceOffsets = true && this.isSetProduceOffsets();
    boolean that_present_produceOffsets = true && that.isSetProduceOffsets();
    if (this_present_produceOffsets || that_present_produceOffsets) {
      if (!(this_present_produceOffsets && that_present_produceOffsets))
        return false;
      if (!this.produceOffsets.equals(that.produceOffsets))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_group = true && (isSetGroup());
    list.add(present_group);
    if (present_group)
      list.add(group);

    boolean present_topic = true && (isSetTopic());
    list.add(present_topic);
    if (present_topic)
      list.add(topic);

    boolean present_consumeOffsets = true && (isSetConsumeOffsets());
    list.add(present_consumeOffsets);
    if (present_consumeOffsets)
      list.add(consumeOffsets);

    boolean present_produceOffsets = true && (isSetProduceOffsets());
    list.add(present_produceOffsets);
    if (present_produceOffsets)
      list.add(produceOffsets);

    return list.hashCode();
  }

  @Override
  public int compareTo(ConsumeStats other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetGroup()).compareTo(other.isSetGroup());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGroup()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.group, other.group);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTopic()).compareTo(other.isSetTopic());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTopic()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.topic, other.topic);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetConsumeOffsets()).compareTo(other.isSetConsumeOffsets());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConsumeOffsets()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.consumeOffsets, other.consumeOffsets);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetProduceOffsets()).compareTo(other.isSetProduceOffsets());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetProduceOffsets()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.produceOffsets, other.produceOffsets);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ConsumeStats(");
    boolean first = true;

    sb.append("group:");
    if (this.group == null) {
      sb.append("null");
    } else {
      sb.append(this.group);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("topic:");
    if (this.topic == null) {
      sb.append("null");
    } else {
      sb.append(this.topic);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("consumeOffsets:");
    if (this.consumeOffsets == null) {
      sb.append("null");
    } else {
      sb.append(this.consumeOffsets);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("produceOffsets:");
    if (this.produceOffsets == null) {
      sb.append("null");
    } else {
      sb.append(this.produceOffsets);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ConsumeStatsStandardSchemeFactory implements SchemeFactory {
    public ConsumeStatsStandardScheme getScheme() {
      return new ConsumeStatsStandardScheme();
    }
  }

  private static class ConsumeStatsStandardScheme extends StandardScheme<ConsumeStats> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ConsumeStats struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // GROUP
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.group = iprot.readString();
              struct.setGroupIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // TOPIC
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.topic = iprot.readString();
              struct.setTopicIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CONSUME_OFFSETS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map90 = iprot.readMapBegin();
                struct.consumeOffsets = new HashMap<String,Long>(2*_map90.size);
                String _key91;
                long _val92;
                for (int _i93 = 0; _i93 < _map90.size; ++_i93)
                {
                  _key91 = iprot.readString();
                  _val92 = iprot.readI64();
                  struct.consumeOffsets.put(_key91, _val92);
                }
                iprot.readMapEnd();
              }
              struct.setConsumeOffsetsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // PRODUCE_OFFSETS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map94 = iprot.readMapBegin();
                struct.produceOffsets = new HashMap<String,Long>(2*_map94.size);
                String _key95;
                long _val96;
                for (int _i97 = 0; _i97 < _map94.size; ++_i97)
                {
                  _key95 = iprot.readString();
                  _val96 = iprot.readI64();
                  struct.produceOffsets.put(_key95, _val96);
                }
                iprot.readMapEnd();
              }
              struct.setProduceOffsetsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ConsumeStats struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.group != null) {
        oprot.writeFieldBegin(GROUP_FIELD_DESC);
        oprot.writeString(struct.group);
        oprot.writeFieldEnd();
      }
      if (struct.topic != null) {
        oprot.writeFieldBegin(TOPIC_FIELD_DESC);
        oprot.writeString(struct.topic);
        oprot.writeFieldEnd();
      }
      if (struct.consumeOffsets != null) {
        oprot.writeFieldBegin(CONSUME_OFFSETS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, struct.consumeOffsets.size()));
          for (Map.Entry<String, Long> _iter98 : struct.consumeOffsets.entrySet())
          {
            oprot.writeString(_iter98.getKey());
            oprot.writeI64(_iter98.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.produceOffsets != null) {
        oprot.writeFieldBegin(PRODUCE_OFFSETS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, struct.produceOffsets.size()));
          for (Map.Entry<String, Long> _iter99 : struct.produceOffsets.entrySet())
          {
            oprot.writeString(_iter99.getKey());
            oprot.writeI64(_iter99.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ConsumeStatsTupleSchemeFactory implements SchemeFactory {
    public ConsumeStatsTupleScheme getScheme() {
      return new ConsumeStatsTupleScheme();
    }
  }

  private static class ConsumeStatsTupleScheme extends TupleScheme<ConsumeStats> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ConsumeStats struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetGroup()) {
        optionals.set(0);
      }
      if (struct.isSetTopic()) {
        optionals.set(1);
      }
      if (struct.isSetConsumeOffsets()) {
        optionals.set(2);
      }
      if (struct.isSetProduceOffsets()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetGroup()) {
        oprot.writeString(struct.group);
      }
      if (struct.isSetTopic()) {
        oprot.writeString(struct.topic);
      }
      if (struct.isSetConsumeOffsets()) {
        {
          oprot.writeI32(struct.consumeOffsets.size());
          for (Map.Entry<String, Long> _iter100 : struct.consumeOffsets.entrySet())
          {
            oprot.writeString(_iter100.getKey());
            oprot.writeI64(_iter100.getValue());
          }
        }
      }
      if (struct.isSetProduceOffsets()) {
        {
          oprot.writeI32(struct.produceOffsets.size());
          for (Map.Entry<String, Long> _iter101 : struct.produceOffsets.entrySet())
          {
            oprot.writeString(_iter101.getKey());
            oprot.writeI64(_iter101.getValue());
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ConsumeStats struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.group = iprot.readString();
        struct.setGroupIsSet(true);
      }
      if (incoming.get(1)) {
        struct.topic = iprot.readString();
        struct.setTopicIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TMap _map102 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, iprot.readI32());
          struct.consumeOffsets = new HashMap<String,Long>(2*_map102.size);
          String _key103;
          long _val104;
          for (int _i105 = 0; _i105 < _map102.size; ++_i105)
          {
            _key103 = iprot.readString();
            _val104 = iprot.readI64();
            struct.consumeOffsets.put(_key103, _val104);
          }
        }
        struct.setConsumeOffsetsIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TMap _map106 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, iprot.readI32());
          struct.produceOffsets = new HashMap<String,Long>(2*_map106.size);
          String _key107;
          long _val108;
          for (int _i109 = 0; _i109 < _map106.size; ++_i109)
          {
            _key107 = iprot.readString();
            _val108 = iprot.readI64();
            struct.produceOffsets.put(_key107, _val108);
          }
        }
        struct.setProduceOffsetsIsSet(true);
      }
    }
  }

}

