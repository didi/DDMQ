#include "ConsumerLogger.h"

#include <log4cpp/Category.hh>
#include <log4cpp/PropertyConfigurator.hh>

namespace CarreraConsumer {

// Logger initiation

ConsumerLogger* ConsumerLogger::logger_ = NULL;
ConsumerLogger::Helpler ConsumerLogger::helpler_;
pthread_once_t ConsumerLogger::once_ = PTHREAD_ONCE_INIT;
std::string ConsumerLogger::conf_file_;

} // namespace CarreraConsumer
