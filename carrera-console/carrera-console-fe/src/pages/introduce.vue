<style lang="less">
@backgroundColor: #F2F9FF;

.introduce {
  padding: 30px 40px 50px 40px;

  &-header {
    padding: 10px 0px;
    width: 920px;

    &-text {
      margin-top: 10px;
      padding: 30px 15px;
      background-color: @backgroundColor;
      font-size: 14px;
    }
  }

  &-body {
    margin-top: 20px;
    &-title {
      font-family: 'Arial-Black';
      font-size: 18px ;
      color: #5A5A5A ;
      margin-bottom: 20px;
    }
    &-text{
      display: inline-block;
      padding: 30px 15px;
      background-color: @backgroundColor;
      width: 96%;
      margin-bottom: 20px;
      height: 90px;
      font-size: 14px;
    }

    &-text:hover {
      color: #24529C;
      background-color: #fff;
      box-shadow: #24439c63 0 0  10px;
    }
  }
  .bcui-row{
    width: 940px;
  }

}
</style>

<template>
  <div class="introduce">
    <div class="introduce-header">
      <img src="logo-with-text.png" width="128" height="38">
      <div class="introduce-header-text">
        DDMQ is a distributed messaging product built by DiDi Infrastructure Team based on <a href="https://rocketmq.apache.org/" target="_blank">Apache RocketMQ</a>. As a distributed messaging middleware, DDMQ provides low latency, high throughput and high availability messaging service to many important systems inside <a href="https://www.didiglobal.com/" target="_blank">DiDi</a>. DDMQ provides realtime messaging, delay-time messaging and transactional messaging to satisfy different scenarios. Through an easy-to-use Web Console and simple SDK Client, developers can experience producing and consuming messages in the most simple and stable way.
      </div>
    </div>
    <div class="introduce-body">
      <div class="introduce-body-title">
        Features
      </div>
      <bc-row>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li> Messaging model: support both P2P and Pub/Sub messaging model</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li> Massive message storage, support RocketMQ and Kafka as storage engine</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>Low latency & High throughput</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>Delay message, use RocksDB as storage engine</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>Transactional message: provide transaction similar to X/Open XA</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>Multiple language client SDK: provide client SDK in PHP, Java, Go, C/C++, Python</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>Message transition and filter with user-defined Groovy script</li>
          </div>
        </bc-col>
        <bc-col span="12">
          <div class="introduce-body-text">
            <li>An easy-to-use Web Console</li>
          </div>
        </bc-col>
      </bc-row>
    </div>
  </div>
</template>
