/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.xiaojukeji.carrera.thrift.consumer;

import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2018-10-31")
public class AckResult implements org.apache.thrift.TBase<AckResult, AckResult._Fields>, java.io.Serializable, Cloneable, Comparable<AckResult> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AckResult");

  private static final org.apache.thrift.protocol.TField CONSUMER_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("consumerId", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField GROUP_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("groupId", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField CLUSTER_FIELD_DESC = new org.apache.thrift.protocol.TField("cluster", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField OFFSETS_FIELD_DESC = new org.apache.thrift.protocol.TField("offsets", org.apache.thrift.protocol.TType.MAP, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new AckResultStandardSchemeFactory());
    schemes.put(TupleScheme.class, new AckResultTupleSchemeFactory());
  }

  public String consumerId; // required
  public String groupId; // required
  public String cluster; // required
  public Map<String,Map<String,Long>> offsets; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CONSUMER_ID((short)1, "consumerId"),
    GROUP_ID((short)2, "groupId"),
    CLUSTER((short)3, "cluster"),
    OFFSETS((short)4, "offsets");

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
        case 1: // CONSUMER_ID
          return CONSUMER_ID;
        case 2: // GROUP_ID
          return GROUP_ID;
        case 3: // CLUSTER
          return CLUSTER;
        case 4: // OFFSETS
          return OFFSETS;
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
    tmpMap.put(_Fields.CONSUMER_ID, new org.apache.thrift.meta_data.FieldMetaData("consumerId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.GROUP_ID, new org.apache.thrift.meta_data.FieldMetaData("groupId", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CLUSTER, new org.apache.thrift.meta_data.FieldMetaData("cluster", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.OFFSETS, new org.apache.thrift.meta_data.FieldMetaData("offsets", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AckResult.class, metaDataMap);
  }

  public AckResult() {
  }

  public AckResult(
    String consumerId,
    String groupId,
    String cluster,
    Map<String,Map<String,Long>> offsets)
  {
    this();
    this.consumerId = consumerId;
    this.groupId = groupId;
    this.cluster = cluster;
    this.offsets = offsets;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public AckResult(AckResult other) {
    if (other.isSetConsumerId()) {
      this.consumerId = other.consumerId;
    }
    if (other.isSetGroupId()) {
      this.groupId = other.groupId;
    }
    if (other.isSetCluster()) {
      this.cluster = other.cluster;
    }
    if (other.isSetOffsets()) {
      Map<String,Map<String,Long>> __this__offsets = new HashMap<String,Map<String,Long>>(other.offsets.size());
      for (Map.Entry<String, Map<String,Long>> other_element : other.offsets.entrySet()) {

        String other_element_key = other_element.getKey();
        Map<String,Long> other_element_value = other_element.getValue();

        String __this__offsets_copy_key = other_element_key;

        Map<String,Long> __this__offsets_copy_value = new HashMap<String,Long>(other_element_value);

        __this__offsets.put(__this__offsets_copy_key, __this__offsets_copy_value);
      }
      this.offsets = __this__offsets;
    }
  }

  public AckResult deepCopy() {
    return new AckResult(this);
  }

  @Override
  public void clear() {
    this.consumerId = null;
    this.groupId = null;
    this.cluster = null;
    this.offsets = null;
  }

  public String getConsumerId() {
    return this.consumerId;
  }

  public AckResult setConsumerId(String consumerId) {
    this.consumerId = consumerId;
    return this;
  }

  public void unsetConsumerId() {
    this.consumerId = null;
  }

  /** Returns true if field consumerId is set (has been assigned a value) and false otherwise */
  public boolean isSetConsumerId() {
    return this.consumerId != null;
  }

  public void setConsumerIdIsSet(boolean value) {
    if (!value) {
      this.consumerId = null;
    }
  }

  public String getGroupId() {
    return this.groupId;
  }

  public AckResult setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public void unsetGroupId() {
    this.groupId = null;
  }

  /** Returns true if field groupId is set (has been assigned a value) and false otherwise */
  public boolean isSetGroupId() {
    return this.groupId != null;
  }

  public void setGroupIdIsSet(boolean value) {
    if (!value) {
      this.groupId = null;
    }
  }

  public String getCluster() {
    return this.cluster;
  }

  public AckResult setCluster(String cluster) {
    this.cluster = cluster;
    return this;
  }

  public void unsetCluster() {
    this.cluster = null;
  }

  /** Returns true if field cluster is set (has been assigned a value) and false otherwise */
  public boolean isSetCluster() {
    return this.cluster != null;
  }

  public void setClusterIsSet(boolean value) {
    if (!value) {
      this.cluster = null;
    }
  }

  public int getOffsetsSize() {
    return (this.offsets == null) ? 0 : this.offsets.size();
  }

  public void putToOffsets(String key, Map<String,Long> val) {
    if (this.offsets == null) {
      this.offsets = new HashMap<String,Map<String,Long>>();
    }
    this.offsets.put(key, val);
  }

  public Map<String,Map<String,Long>> getOffsets() {
    return this.offsets;
  }

  public AckResult setOffsets(Map<String,Map<String,Long>> offsets) {
    this.offsets = offsets;
    return this;
  }

  public void unsetOffsets() {
    this.offsets = null;
  }

  /** Returns true if field offsets is set (has been assigned a value) and false otherwise */
  public boolean isSetOffsets() {
    return this.offsets != null;
  }

  public void setOffsetsIsSet(boolean value) {
    if (!value) {
      this.offsets = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CONSUMER_ID:
      if (value == null) {
        unsetConsumerId();
      } else {
        setConsumerId((String)value);
      }
      break;

    case GROUP_ID:
      if (value == null) {
        unsetGroupId();
      } else {
        setGroupId((String)value);
      }
      break;

    case CLUSTER:
      if (value == null) {
        unsetCluster();
      } else {
        setCluster((String)value);
      }
      break;

    case OFFSETS:
      if (value == null) {
        unsetOffsets();
      } else {
        setOffsets((Map<String,Map<String,Long>>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CONSUMER_ID:
      return getConsumerId();

    case GROUP_ID:
      return getGroupId();

    case CLUSTER:
      return getCluster();

    case OFFSETS:
      return getOffsets();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CONSUMER_ID:
      return isSetConsumerId();
    case GROUP_ID:
      return isSetGroupId();
    case CLUSTER:
      return isSetCluster();
    case OFFSETS:
      return isSetOffsets();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof AckResult)
      return this.equals((AckResult)that);
    return false;
  }

  public boolean equals(AckResult that) {
    if (that == null)
      return false;

    boolean this_present_consumerId = true && this.isSetConsumerId();
    boolean that_present_consumerId = true && that.isSetConsumerId();
    if (this_present_consumerId || that_present_consumerId) {
      if (!(this_present_consumerId && that_present_consumerId))
        return false;
      if (!this.consumerId.equals(that.consumerId))
        return false;
    }

    boolean this_present_groupId = true && this.isSetGroupId();
    boolean that_present_groupId = true && that.isSetGroupId();
    if (this_present_groupId || that_present_groupId) {
      if (!(this_present_groupId && that_present_groupId))
        return false;
      if (!this.groupId.equals(that.groupId))
        return false;
    }

    boolean this_present_cluster = true && this.isSetCluster();
    boolean that_present_cluster = true && that.isSetCluster();
    if (this_present_cluster || that_present_cluster) {
      if (!(this_present_cluster && that_present_cluster))
        return false;
      if (!this.cluster.equals(that.cluster))
        return false;
    }

    boolean this_present_offsets = true && this.isSetOffsets();
    boolean that_present_offsets = true && that.isSetOffsets();
    if (this_present_offsets || that_present_offsets) {
      if (!(this_present_offsets && that_present_offsets))
        return false;
      if (!this.offsets.equals(that.offsets))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_consumerId = true && (isSetConsumerId());
    list.add(present_consumerId);
    if (present_consumerId)
      list.add(consumerId);

    boolean present_groupId = true && (isSetGroupId());
    list.add(present_groupId);
    if (present_groupId)
      list.add(groupId);

    boolean present_cluster = true && (isSetCluster());
    list.add(present_cluster);
    if (present_cluster)
      list.add(cluster);

    boolean present_offsets = true && (isSetOffsets());
    list.add(present_offsets);
    if (present_offsets)
      list.add(offsets);

    return list.hashCode();
  }

  @Override
  public int compareTo(AckResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetConsumerId()).compareTo(other.isSetConsumerId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConsumerId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.consumerId, other.consumerId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetGroupId()).compareTo(other.isSetGroupId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetGroupId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.groupId, other.groupId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCluster()).compareTo(other.isSetCluster());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCluster()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cluster, other.cluster);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOffsets()).compareTo(other.isSetOffsets());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOffsets()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.offsets, other.offsets);
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
    StringBuilder sb = new StringBuilder("AckResult(");
    boolean first = true;

    sb.append("consumerId:");
    if (this.consumerId == null) {
      sb.append("null");
    } else {
      sb.append(this.consumerId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("groupId:");
    if (this.groupId == null) {
      sb.append("null");
    } else {
      sb.append(this.groupId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("cluster:");
    if (this.cluster == null) {
      sb.append("null");
    } else {
      sb.append(this.cluster);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("offsets:");
    if (this.offsets == null) {
      sb.append("null");
    } else {
      sb.append(this.offsets);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (consumerId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'consumerId' was not present! Struct: " + toString());
    }
    if (groupId == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'groupId' was not present! Struct: " + toString());
    }
    if (cluster == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'cluster' was not present! Struct: " + toString());
    }
    if (offsets == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'offsets' was not present! Struct: " + toString());
    }
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

  private static class AckResultStandardSchemeFactory implements SchemeFactory {
    public AckResultStandardScheme getScheme() {
      return new AckResultStandardScheme();
    }
  }

  private static class AckResultStandardScheme extends StandardScheme<AckResult> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, AckResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CONSUMER_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.consumerId = iprot.readString();
              struct.setConsumerIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // GROUP_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.groupId = iprot.readString();
              struct.setGroupIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CLUSTER
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.cluster = iprot.readString();
              struct.setClusterIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // OFFSETS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map70 = iprot.readMapBegin();
                struct.offsets = new HashMap<String,Map<String,Long>>(2*_map70.size);
                String _key71;
                Map<String,Long> _val72;
                for (int _i73 = 0; _i73 < _map70.size; ++_i73)
                {
                  _key71 = iprot.readString();
                  {
                    org.apache.thrift.protocol.TMap _map74 = iprot.readMapBegin();
                    _val72 = new HashMap<String,Long>(2*_map74.size);
                    String _key75;
                    long _val76;
                    for (int _i77 = 0; _i77 < _map74.size; ++_i77)
                    {
                      _key75 = iprot.readString();
                      _val76 = iprot.readI64();
                      _val72.put(_key75, _val76);
                    }
                    iprot.readMapEnd();
                  }
                  struct.offsets.put(_key71, _val72);
                }
                iprot.readMapEnd();
              }
              struct.setOffsetsIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, AckResult struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.consumerId != null) {
        oprot.writeFieldBegin(CONSUMER_ID_FIELD_DESC);
        oprot.writeString(struct.consumerId);
        oprot.writeFieldEnd();
      }
      if (struct.groupId != null) {
        oprot.writeFieldBegin(GROUP_ID_FIELD_DESC);
        oprot.writeString(struct.groupId);
        oprot.writeFieldEnd();
      }
      if (struct.cluster != null) {
        oprot.writeFieldBegin(CLUSTER_FIELD_DESC);
        oprot.writeString(struct.cluster);
        oprot.writeFieldEnd();
      }
      if (struct.offsets != null) {
        oprot.writeFieldBegin(OFFSETS_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.offsets.size()));
          for (Map.Entry<String, Map<String,Long>> _iter78 : struct.offsets.entrySet())
          {
            oprot.writeString(_iter78.getKey());
            {
              oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, _iter78.getValue().size()));
              for (Map.Entry<String, Long> _iter79 : _iter78.getValue().entrySet())
              {
                oprot.writeString(_iter79.getKey());
                oprot.writeI64(_iter79.getValue());
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class AckResultTupleSchemeFactory implements SchemeFactory {
    public AckResultTupleScheme getScheme() {
      return new AckResultTupleScheme();
    }
  }

  private static class AckResultTupleScheme extends TupleScheme<AckResult> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, AckResult struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeString(struct.consumerId);
      oprot.writeString(struct.groupId);
      oprot.writeString(struct.cluster);
      {
        oprot.writeI32(struct.offsets.size());
        for (Map.Entry<String, Map<String,Long>> _iter80 : struct.offsets.entrySet())
        {
          oprot.writeString(_iter80.getKey());
          {
            oprot.writeI32(_iter80.getValue().size());
            for (Map.Entry<String, Long> _iter81 : _iter80.getValue().entrySet())
            {
              oprot.writeString(_iter81.getKey());
              oprot.writeI64(_iter81.getValue());
            }
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, AckResult struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.consumerId = iprot.readString();
      struct.setConsumerIdIsSet(true);
      struct.groupId = iprot.readString();
      struct.setGroupIdIsSet(true);
      struct.cluster = iprot.readString();
      struct.setClusterIsSet(true);
      {
        org.apache.thrift.protocol.TMap _map82 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
        struct.offsets = new HashMap<String,Map<String,Long>>(2*_map82.size);
        String _key83;
        Map<String,Long> _val84;
        for (int _i85 = 0; _i85 < _map82.size; ++_i85)
        {
          _key83 = iprot.readString();
          {
            org.apache.thrift.protocol.TMap _map86 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, iprot.readI32());
            _val84 = new HashMap<String,Long>(2*_map86.size);
            String _key87;
            long _val88;
            for (int _i89 = 0; _i89 < _map86.size; ++_i89)
            {
              _key87 = iprot.readString();
              _val88 = iprot.readI64();
              _val84.put(_key87, _val88);
            }
          }
          struct.offsets.put(_key83, _val84);
        }
      }
      struct.setOffsetsIsSet(true);
    }
  }

}

