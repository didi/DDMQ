package com.xiaojukeji.chronos.ha;

import com.xiaojukeji.chronos.enums.ServerState;
import com.xiaojukeji.chronos.services.MetaService;
import com.xiaojukeji.chronos.utils.LogUtils;
import com.xiaojukeji.chronos.utils.ZkUtils;
import com.xiaojukeji.chronos.utils.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class MasterElection {
    private static final Logger SWITCH_LOGGER = LogUtils.SWITCH_LOGGER;

    private static volatile ServerState state = ServerState.BACKUPING;

    public static void election(final CountDownLatch cdl) {
        final CuratorFramework client = ZkUtils.getCuratorClient();
        final LeaderSelector selector = new LeaderSelector(client, Constants.MASTER_PATH, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                SWITCH_LOGGER.info("take master leadership");

                long seekTimestamp = MetaService.getSeekTimestamp();
                long zkSeekTimestamp = MetaService.getZkSeekTimestamp();
                final long sleepMs = 200;
                long sleepCount = 0;
                // 如果zk上的数据丢失了, 则zkSeekTimestamp为0, 此时chronos则被block住
                while (seekTimestamp < zkSeekTimestamp && zkSeekTimestamp > 0) {
                    SWITCH_LOGGER.info("sleep {}ms to wait seekTimestamp:{} to catch up with zkSeekTimestamp:{}",
                            sleepMs, seekTimestamp, zkSeekTimestamp);
                    TimeUnit.MILLISECONDS.sleep(sleepMs);
                    seekTimestamp = MetaService.getSeekTimestamp();
                    zkSeekTimestamp = MetaService.getZkSeekTimestamp();
                    sleepCount++;
                }

                state = ServerState.MASTERING;
                SWITCH_LOGGER.info("change server state to {}, totalSleepMs:{}ms", state, sleepCount * sleepMs);
                cdl.await();
                state = ServerState.BACKUPING;
                SWITCH_LOGGER.info("release master leadership");
            }
        });
        selector.autoRequeue();
        selector.start();
    }

    public static boolean isMaster() {
        return state == ServerState.MASTERING;
    }

    public static boolean isBackup() {
        return state == ServerState.BACKUPING;
    }

    public static void standAlone() {
        state = ServerState.MASTERING;
    }

    public static ServerState getState() {
        return state;
    }
}