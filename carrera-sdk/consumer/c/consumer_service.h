/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
#ifndef CONSUMER_SERVICE_H
#define CONSUMER_SERVICE_H

#include <thrift/c_glib/processor/thrift_dispatch_processor.h>

#include "consumer_proxy_types.h"

/* ConsumerService service interface */
typedef struct _ConsumerServiceIf ConsumerServiceIf;  /* dummy object */

struct _ConsumerServiceIfInterface
{
  GTypeInterface parent;

  gboolean (*pull) (ConsumerServiceIf *iface, PullResponse ** _return, const PullRequest * request, PullException ** error, GError **error);
  gboolean (*submit) (ConsumerServiceIf *iface, gboolean* _return, const ConsumeResult * result, PullException ** error, GError **error);
  gboolean (*get_consume_stats) (ConsumerServiceIf *iface, GPtrArray ** _return, const ConsumeStatsRequest * request, PullException ** error, GError **error);
  gboolean (*fetch) (ConsumerServiceIf *iface, FetchResponse ** _return, const FetchRequest * request, GError **error);
  gboolean (*ack) (ConsumerServiceIf *iface, gboolean* _return, const AckResult * result, GError **error);
};
typedef struct _ConsumerServiceIfInterface ConsumerServiceIfInterface;

GType consumer_service_if_get_type (void);
#define TYPE_CONSUMER_SERVICE_IF (consumer_service_if_get_type())
#define CONSUMER_SERVICE_IF(obj) (G_TYPE_CHECK_INSTANCE_CAST ((obj), TYPE_CONSUMER_SERVICE_IF, ConsumerServiceIf))
#define IS_CONSUMER_SERVICE_IF(obj) (G_TYPE_CHECK_INSTANCE_TYPE ((obj), TYPE_CONSUMER_SERVICE_IF))
#define CONSUMER_SERVICE_IF_GET_INTERFACE(inst) (G_TYPE_INSTANCE_GET_INTERFACE ((inst), TYPE_CONSUMER_SERVICE_IF, ConsumerServiceIfInterface))

gboolean consumer_service_if_pull (ConsumerServiceIf *iface, PullResponse ** _return, const PullRequest * request, PullException ** error, GError **error);
gboolean consumer_service_if_submit (ConsumerServiceIf *iface, gboolean* _return, const ConsumeResult * result, PullException ** error, GError **error);
gboolean consumer_service_if_get_consume_stats (ConsumerServiceIf *iface, GPtrArray ** _return, const ConsumeStatsRequest * request, PullException ** error, GError **error);
gboolean consumer_service_if_fetch (ConsumerServiceIf *iface, FetchResponse ** _return, const FetchRequest * request, GError **error);
gboolean consumer_service_if_ack (ConsumerServiceIf *iface, gboolean* _return, const AckResult * result, GError **error);

/* ConsumerService service client */
struct _ConsumerServiceClient
{
  GObject parent;

  ThriftProtocol *input_protocol;
  ThriftProtocol *output_protocol;
};
typedef struct _ConsumerServiceClient ConsumerServiceClient;

struct _ConsumerServiceClientClass
{
  GObjectClass parent;
};
typedef struct _ConsumerServiceClientClass ConsumerServiceClientClass;

GType consumer_service_client_get_type (void);
#define TYPE_CONSUMER_SERVICE_CLIENT (consumer_service_client_get_type())
#define CONSUMER_SERVICE_CLIENT(obj) (G_TYPE_CHECK_INSTANCE_CAST ((obj), TYPE_CONSUMER_SERVICE_CLIENT, ConsumerServiceClient))
#define CONSUMER_SERVICE_CLIENT_CLASS(c) (G_TYPE_CHECK_CLASS_CAST ((c), TYPE_CONSUMER_SERVICE_CLIENT, ConsumerServiceClientClass))
#define CONSUMER_SERVICE_IS_CLIENT(obj) (G_TYPE_CHECK_INSTANCE_TYPE ((obj), TYPE_CONSUMER_SERVICE_CLIENT))
#define CONSUMER_SERVICE_IS_CLIENT_CLASS(c) (G_TYPE_CHECK_CLASS_TYPE ((c), TYPE_CONSUMER_SERVICE_CLIENT))
#define CONSUMER_SERVICE_CLIENT_GET_CLASS(obj) (G_TYPE_INSTANCE_GET_CLASS ((obj), TYPE_CONSUMER_SERVICE_CLIENT, ConsumerServiceClientClass))

gboolean consumer_service_client_pull (ConsumerServiceIf * iface, PullResponse ** _return, const PullRequest * request, PullException ** error, GError ** error);
gboolean consumer_service_client_send_pull (ConsumerServiceIf * iface, const PullRequest * request, GError ** error);
gboolean consumer_service_client_recv_pull (ConsumerServiceIf * iface, PullResponse ** _return, PullException ** error, GError ** error);
gboolean consumer_service_client_submit (ConsumerServiceIf * iface, gboolean* _return, const ConsumeResult * result, PullException ** error, GError ** error);
gboolean consumer_service_client_send_submit (ConsumerServiceIf * iface, const ConsumeResult * result, GError ** error);
gboolean consumer_service_client_recv_submit (ConsumerServiceIf * iface, gboolean* _return, PullException ** error, GError ** error);
gboolean consumer_service_client_get_consume_stats (ConsumerServiceIf * iface, GPtrArray ** _return, const ConsumeStatsRequest * request, PullException ** error, GError ** error);
gboolean consumer_service_client_send_get_consume_stats (ConsumerServiceIf * iface, const ConsumeStatsRequest * request, GError ** error);
gboolean consumer_service_client_recv_get_consume_stats (ConsumerServiceIf * iface, GPtrArray ** _return, PullException ** error, GError ** error);
gboolean consumer_service_client_fetch (ConsumerServiceIf * iface, FetchResponse ** _return, const FetchRequest * request, GError ** error);
gboolean consumer_service_client_send_fetch (ConsumerServiceIf * iface, const FetchRequest * request, GError ** error);
gboolean consumer_service_client_recv_fetch (ConsumerServiceIf * iface, FetchResponse ** _return, GError ** error);
gboolean consumer_service_client_ack (ConsumerServiceIf * iface, gboolean* _return, const AckResult * result, GError ** error);
gboolean consumer_service_client_send_ack (ConsumerServiceIf * iface, const AckResult * result, GError ** error);
gboolean consumer_service_client_recv_ack (ConsumerServiceIf * iface, gboolean* _return, GError ** error);
void consumer_service_client_set_property (GObject *object, guint property_id, const GValue *value, GParamSpec *pspec);
void consumer_service_client_get_property (GObject *object, guint property_id, GValue *value, GParamSpec *pspec);

/* ConsumerService handler (abstract base class) */
struct _ConsumerServiceHandler
{
  GObject parent;
};
typedef struct _ConsumerServiceHandler ConsumerServiceHandler;

struct _ConsumerServiceHandlerClass
{
  GObjectClass parent;

  gboolean (*pull) (ConsumerServiceIf *iface, PullResponse ** _return, const PullRequest * request, PullException ** error, GError **error);
  gboolean (*submit) (ConsumerServiceIf *iface, gboolean* _return, const ConsumeResult * result, PullException ** error, GError **error);
  gboolean (*get_consume_stats) (ConsumerServiceIf *iface, GPtrArray ** _return, const ConsumeStatsRequest * request, PullException ** error, GError **error);
  gboolean (*fetch) (ConsumerServiceIf *iface, FetchResponse ** _return, const FetchRequest * request, GError **error);
  gboolean (*ack) (ConsumerServiceIf *iface, gboolean* _return, const AckResult * result, GError **error);
};
typedef struct _ConsumerServiceHandlerClass ConsumerServiceHandlerClass;

GType consumer_service_handler_get_type (void);
#define TYPE_CONSUMER_SERVICE_HANDLER (consumer_service_handler_get_type())
#define CONSUMER_SERVICE_HANDLER(obj) (G_TYPE_CHECK_INSTANCE_CAST ((obj), TYPE_CONSUMER_SERVICE_HANDLER, ConsumerServiceHandler))
#define IS_CONSUMER_SERVICE_HANDLER(obj) (G_TYPE_CHECK_INSTANCE_TYPE ((obj), TYPE_CONSUMER_SERVICE_HANDLER))
#define CONSUMER_SERVICE_HANDLER_CLASS(c) (G_TYPE_CHECK_CLASS_CAST ((c), TYPE_CONSUMER_SERVICE_HANDLER, ConsumerServiceHandlerClass))
#define IS_CONSUMER_SERVICE_HANDLER_CLASS(c) (G_TYPE_CHECK_CLASS_TYPE ((c), TYPE_CONSUMER_SERVICE_HANDLER))
#define CONSUMER_SERVICE_HANDLER_GET_CLASS(obj) (G_TYPE_INSTANCE_GET_CLASS ((obj), TYPE_CONSUMER_SERVICE_HANDLER, ConsumerServiceHandlerClass))

gboolean consumer_service_handler_pull (ConsumerServiceIf *iface, PullResponse ** _return, const PullRequest * request, PullException ** error, GError **error);
gboolean consumer_service_handler_submit (ConsumerServiceIf *iface, gboolean* _return, const ConsumeResult * result, PullException ** error, GError **error);
gboolean consumer_service_handler_get_consume_stats (ConsumerServiceIf *iface, GPtrArray ** _return, const ConsumeStatsRequest * request, PullException ** error, GError **error);
gboolean consumer_service_handler_fetch (ConsumerServiceIf *iface, FetchResponse ** _return, const FetchRequest * request, GError **error);
gboolean consumer_service_handler_ack (ConsumerServiceIf *iface, gboolean* _return, const AckResult * result, GError **error);

/* ConsumerService processor */
struct _ConsumerServiceProcessor
{
  ThriftDispatchProcessor parent;

  /* protected */
  ConsumerServiceHandler *handler;
  GHashTable *process_map;
};
typedef struct _ConsumerServiceProcessor ConsumerServiceProcessor;

struct _ConsumerServiceProcessorClass
{
  ThriftDispatchProcessorClass parent;

  /* protected */
  gboolean (*dispatch_call) (ThriftDispatchProcessor *processor,
                             ThriftProtocol *in,
                             ThriftProtocol *out,
                             gchar *fname,
                             gint32 seqid,
                             GError **error);
};
typedef struct _ConsumerServiceProcessorClass ConsumerServiceProcessorClass;

GType consumer_service_processor_get_type (void);
#define TYPE_CONSUMER_SERVICE_PROCESSOR (consumer_service_processor_get_type())
#define CONSUMER_SERVICE_PROCESSOR(obj) (G_TYPE_CHECK_INSTANCE_CAST ((obj), TYPE_CONSUMER_SERVICE_PROCESSOR, ConsumerServiceProcessor))
#define IS_CONSUMER_SERVICE_PROCESSOR(obj) (G_TYPE_CHECK_INSTANCE_TYPE ((obj), TYPE_CONSUMER_SERVICE_PROCESSOR))
#define CONSUMER_SERVICE_PROCESSOR_CLASS(c) (G_TYPE_CHECK_CLASS_CAST ((c), TYPE_CONSUMER_SERVICE_PROCESSOR, ConsumerServiceProcessorClass))
#define IS_CONSUMER_SERVICE_PROCESSOR_CLASS(c) (G_TYPE_CHECK_CLASS_TYPE ((c), TYPE_CONSUMER_SERVICE_PROCESSOR))
#define CONSUMER_SERVICE_PROCESSOR_GET_CLASS(obj) (G_TYPE_INSTANCE_GET_CLASS ((obj), TYPE_CONSUMER_SERVICE_PROCESSOR, ConsumerServiceProcessorClass))

#endif /* CONSUMER_SERVICE_H */
