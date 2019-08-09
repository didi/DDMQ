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
package org.apache.rocketmq.tools.command.namesrv;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

public class EnableBrokerRoleSwitchCommand implements SubCommand {
    @Override
    public String commandName() {
        return "enableBrokerRoleSwitch";
    }

    @Override
    public String commandDesc() {
        return "enable broker role switch.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("c", "clusterName", true, "cluster name");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("b", "brokerName", true, "broker name");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    public void execute(final CommandLine commandLine, final Options options,
        final RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            // cluster name
            String clusterName = commandLine.getOptionValue('c').trim();
            // broker name
            String brokerName = commandLine.getOptionValue('b').trim();

            // servers
            String servers = commandLine.getOptionValue('n');
            List<String> serverList = null;
            if (servers != null && servers.length() > 0) {
                String[] serverArray = servers.trim().split(";");

                if (serverArray.length > 0) {
                    serverList = Arrays.asList(serverArray);
                }
            }

            defaultMQAdminExt.start();

            defaultMQAdminExt.enableBrokerRoleSwitch(clusterName, brokerName, serverList);

            System.out.printf("enable broker role switch success!%s\n%s : %s\n",
                serverList == null ? "" : serverList, clusterName, brokerName);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
