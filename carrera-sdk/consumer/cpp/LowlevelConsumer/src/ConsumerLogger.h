#ifndef CARRERA_CONSUMER_LOGGER_H
#define CARRERA_CONSUMER_LOGGER_H

#include <string>
#include <log4cpp/Category.hh>
#include <log4cpp/RollingFileAppender.hh>
#include <log4cpp/PatternLayout.hh>
#include <log4cpp/PropertyConfigurator.hh>
#include <pthread.h>

namespace CarreraConsumer {

class ConsumerLogger {
 public:
    static log4cpp::Category* GetLoggerCategory() {
        pthread_once(&once_, &ConsumerLogger::InitLogger);
        return logger_->root_;
    }

    static void ConfigLogger(const std::string &conf_file) {
        conf_file_ = conf_file;
        pthread_once(&once_, &ConsumerLogger::InitLogger);
    }

 private:

    ConsumerLogger(const ConsumerLogger &rhs);
    ConsumerLogger& operator=(const ConsumerLogger &rhs);

    ConsumerLogger() {
        if (conf_file_.empty()) {
            root_ = &log4cpp::Category::getRoot();
            ConfigLoggerCommon(root_, "consumer.log", 400000, "WARN");
        } else {
            log4cpp::PropertyConfigurator::configure(conf_file_);
            root_ = &log4cpp::Category::getRoot();
        }
    }   

    static void InitLogger() {
        logger_ = new ConsumerLogger(); 
    }

    static void ConfigLoggerCommon(log4cpp::Category *r, const std::string &file_name, 
                                   int max_file_size, const std::string &level) {
        r->removeAllAppenders();
        log4cpp::Appender *appender = new log4cpp::RollingFileAppender(
                                      "consumer_rolling_logger", file_name, 
                                      max_file_size, 3, true, 00644);
        log4cpp::PatternLayout *layout = new log4cpp::PatternLayout();
        layout->setConversionPattern("%d{%Y-%m-%d %H:%M:%S.%l} - [%p] : %m%n");
        appender->setLayout(layout);
        r->addAppender(appender);
        r->setPriority(log4cpp::Priority::getPriorityValue(level));
    }

    ~ConsumerLogger() {
        log4cpp::Category::shutdown();
    }

    class Helpler {
      public:
        ~Helpler() {
            if (logger_) delete logger_;
        }
    };

    log4cpp::Category *root_;
    static ConsumerLogger *logger_;
    static Helpler helpler_;
    static pthread_once_t once_;
    static std::string conf_file_; 
};

} // namespace CarreraConsumer

#define LoggerCategory CarreraConsumer::ConsumerLogger::GetLoggerCategory()
#define LOG_DEBUG(...) do { LoggerCategory->debug(__VA_ARGS__); } while (0)
#define LOG_INFO(...) do { LoggerCategory->info(__VA_ARGS__); } while (0)
#define LOG_WARN(...) do { LoggerCategory->warn(__VA_ARGS__); } while (0)
#define LOG_ERROR(...) do { LoggerCategory->error(__VA_ARGS__); } while (0)
#define LOG_FATAL(...) do { LoggerCategory->fatal(__VA_ARGS__); } while (0)

#endif // CARRERA_CONSUMER_LOGGER_H
