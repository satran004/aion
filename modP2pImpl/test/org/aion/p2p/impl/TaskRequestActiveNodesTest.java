package org.aion.p2p.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aion.p2p.INode;
import org.aion.p2p.IP2pMgr;
import org.aion.p2p.impl.zero.msg.ReqActiveNodes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class TaskRequestActiveNodesTest {

    @Mock private IP2pMgr mgr;

    @Mock private INode node;

    @Mock private Logger p2pLOG;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(timeout = 10_000)
    public void TestRun() throws InterruptedException {
        when(mgr.getRandom()).thenReturn(node);
        when(node.getIdShort()).thenReturn("inode");

        TaskRequestActiveNodes tran = new TaskRequestActiveNodes(mgr, p2pLOG);
        assertNotNull(tran);
        tran.run();
        Thread.sleep(10);
        verify(mgr).send(anyInt(), anyString(), any(ReqActiveNodes.class));
    }
}
