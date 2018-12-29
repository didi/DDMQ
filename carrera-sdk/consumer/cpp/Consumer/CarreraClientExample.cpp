/**
 * carrera consumer thrift sdk - example
 */
#include "CarreraConsumer.h"
#include <iostream>
#include <string>
#include <signal.h>
#include <map>
#include <unistd.h>

using std::string;
using std::map;

class MyClass : public CarreraConsumer::ProcessorBase {
public:
    bool Process(const CarreraConsumer::Message &message, const CarreraConsumer::Context &context) {
        std::cout << "msg: {" << message << "}, context: {" << context << "}\n";
        return true;
    }
};

int main() {
    std::cout << "init..." << std::endl;
    CarreraConsumer::CarreraConfig config;
    std::string consumeGroupName  = "cg_test_bd"; // 消费组名
    std::string idc = "test"; // client IDC
    std::string sd_server = "127.0.0.1:9513";
    config.SetGroupName(consumeGroupName);
    config.SetClientNumPerServer(2);
    config.SetRetryInterval(1000);
    MyClass testClass;

    CarreraConsumer::CarreraSdCconsumer sdClient(consumeGroupName,idc,sd_server);
    sdClient.SetCarreraConfig(config);
    sdClient.SetProcessor(&testClass);

    std::cout << "start consume..." << std::endl;
    sdClient.StartConsume();

    std::this_thread::sleep_for(std::chrono::seconds(100));
    std::cout << "stopping" << std::endl;
    sdClient.Stop();
    std::cout << "stopped" << std::endl;
    return 0;
}
