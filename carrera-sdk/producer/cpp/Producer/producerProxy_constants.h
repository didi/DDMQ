/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef producerProxy_CONSTANTS_H
#define producerProxy_CONSTANTS_H

#include "producerProxy_types.h"

namespace CarreraProducer {

class producerProxyConstants {
 public:
  producerProxyConstants();

  std::string PRESSURE_TRAFFIC_KEY;
  std::string PRESSURE_TRAFFIC_ENABLE;
  std::string PRESSURE_TRAFFIC_DISABLE;
  std::string TRACE_ID;
  std::string SPAN_ID;
  std::string CARRERA_HEADERS;
  std::string DIDI_HEADER_RID;
  std::string DIDI_HEADER_SPANID;
};

extern const producerProxyConstants g_producerProxy_constants;

} // namespace

#endif
