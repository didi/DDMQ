/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.tools.command.message;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

public class QueryMsgByKeySubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "queryMsgByKey";
    }

    @Override
    public String commandDesc() {
        return "Query Message by Key";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "topic name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("k", "msgKey", true, "Message Key");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("s", "slaveFirst", true, "Slave First");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            final String topic = commandLine.getOptionValue('t').trim();
            final String key = commandLine.getOptionValue('k').trim();
            String isSlaveFirstStr = commandLine.getOptionValue('s').trim();
            boolean isSlaveFirst = StringUtils.isEmpty(isSlaveFirstStr) ? true : Boolean.valueOf(isSlaveFirstStr);

            this.queryByKey(defaultMQAdminExt, topic, key, isSlaveFirst);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }

    private void queryByKey(final DefaultMQAdminExt admin, final String topic, final String key, boolean isSlaveFirst)
        throws MQClientException, InterruptedException, IOException {
        admin.start();

        QueryResult queryResult = admin.queryMessage(topic, key, 64, 0, Long.MAX_VALUE, isSlaveFirst);

        for (MessageExt msg : queryResult.getMessageList()) {
            System.out.printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~%n");
            //System.out.printf("%-50s %4d %40d%n", msg.getMsgId(), msg.getQueueId(), msg.getQueueOffset());
            printMsg(msg);
            System.out.printf("%n");
        }
    }

    public static void printMsg(final MessageExt msg) throws IOException {
        if (msg == null) {
            System.out.printf("%nMessage not found!");
            return;
        }

        String bodyTmpFilePath = createBodyFile(msg);
        String msgId = msg.getMsgId();
        if (msg instanceof MessageClientExt) {
            msgId = ((MessageClientExt) msg).getOffsetMsgId();
        }

        System.out.printf("%-20s %s%n",
            "OffsetID:",
            msgId
        );

        System.out.printf("%-20s %s%n",
            "Topic:",
            msg.getTopic()
        );

        System.out.printf("%-20s %s%n",
            "Tags:",
            "[" + msg.getTags() + "]"
        );

        System.out.printf("%-20s %s%n",
            "Keys:",
            "[" + msg.getKeys() + "]"
        );

        System.out.printf("%-20s %d%n",
            "Queue ID:",
            msg.getQueueId()
        );

        System.out.printf("%-20s %d%n",
            "Queue Offset:",
            msg.getQueueOffset()
        );

        System.out.printf("%-20s %d%n",
            "CommitLog Offset:",
            msg.getCommitLogOffset()
        );

        System.out.printf("%-20s %d%n",
            "Reconsume Times:",
            msg.getReconsumeTimes()
        );

        System.out.printf("%-20s %s%n",
            "Born Timestamp:",
            UtilAll.timeMillisToHumanString2(msg.getBornTimestamp())
        );

        System.out.printf("%-20s %s%n",
            "Store Timestamp:",
            UtilAll.timeMillisToHumanString2(msg.getStoreTimestamp())
        );

        System.out.printf("%-20s %s%n",
            "Born Host:",
            RemotingHelper.parseSocketAddressAddr(msg.getBornHost())
        );

        System.out.printf("%-20s %s%n",
            "Store Host:",
            RemotingHelper.parseSocketAddressAddr(msg.getStoreHost())
        );

        System.out.printf("%-20s %d%n",
            "System Flag:",
            msg.getSysFlag()
        );

        System.out.printf("%-20s %s%n",
            "Properties:",
            msg.getProperties() != null ? msg.getProperties().toString() : ""
        );

        System.out.printf("%-20s %s%n",
            "Message Body Path:",
            bodyTmpFilePath
        );
    }

    private static String createBodyFile(MessageExt msg) throws IOException {
        DataOutputStream dos = null;
        try {
            String bodyTmpFilePath = "/tmp/rocketmq/msgbodys";
            File file = new File(bodyTmpFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            bodyTmpFilePath = bodyTmpFilePath + "/" + msg.getMsgId();
            dos = new DataOutputStream(new FileOutputStream(bodyTmpFilePath));
            dos.write(msg.getBody());
            return bodyTmpFilePath;
        } finally {
            if (dos != null)
                dos.close();
        }
    }
}
