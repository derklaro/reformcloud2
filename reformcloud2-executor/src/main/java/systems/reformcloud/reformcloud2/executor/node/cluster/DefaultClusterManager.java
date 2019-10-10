package systems.reformcloud.reformcloud2.executor.node.cluster;

import systems.reformcloud.reformcloud2.executor.api.common.node.NodeInformation;
import systems.reformcloud.reformcloud2.executor.api.common.utility.list.Links;
import systems.reformcloud.reformcloud2.executor.api.node.cluster.ClusterManager;
import systems.reformcloud.reformcloud2.executor.api.node.cluster.InternalNetworkCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

public class DefaultClusterManager implements ClusterManager {

    private final Collection<NodeInformation> nodeInformation = new ArrayList<>();

    private NodeInformation head;

    @Override
    public void handleNodeDisconnect(InternalNetworkCluster cluster, String name) {
        Links.allOf(nodeInformation, e -> e.getName().equals(name)).forEach(e -> {
            this.nodeInformation.remove(e);
            cluster.getConnectedNodes().remove(e);
        });
        recalculateHead();
    }

    @Override
    public void handleConnect(InternalNetworkCluster cluster, NodeInformation nodeInformation, BiConsumer<Boolean, String> result) {
        if (this.nodeInformation.stream().anyMatch(e -> e.getName().equals(nodeInformation.getName()))) {
            result.accept(false, "A node with this name is already connected");
            return;
        }

        this.nodeInformation.add(nodeInformation);
        cluster.getConnectedNodes().add(nodeInformation);
        recalculateHead();
        result.accept(true, null);
    }

    @Override
    public int getOnlineAndWaiting(String groupName) {
        return this.nodeInformation
                .stream()
                .filter(e -> e.getStartedProcesses().stream().anyMatch(s -> s.getGroup().equals(groupName))
                        || e.getQueuedProcesses().entrySet().stream().anyMatch(s -> s.getKey().equals(groupName)))
                .mapToInt(e -> Links.keyFilter(e.getQueuedProcesses(), g -> g.equals(groupName)).size()
                        + Links.allOf(e.getStartedProcesses(), s -> s.getGroup().equals(groupName)).size())
                .sum();
    }

    @Override
    public NodeInformation getHeadNode() {
        if (head == null) {
            recalculateHead();
        }

        return head;
    }

    private void recalculateHead() {
        for (NodeInformation information : nodeInformation) {
            if (head == null) {
                head = information;
                continue;
            }

            if (information.getStartupTime() < head.getStartupTime()) {
                head = information;
            }
        }
    }
}
