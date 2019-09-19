package de.klaro.reformcloud2.executor.api.bungee.event;

import de.klaro.reformcloud2.executor.api.bungee.BungeeExecutor;
import de.klaro.reformcloud2.executor.api.common.CommonHelper;
import de.klaro.reformcloud2.executor.api.common.ExecutorAPI;
import de.klaro.reformcloud2.executor.api.common.groups.utils.PlayerAccessConfiguration;
import de.klaro.reformcloud2.executor.api.common.groups.utils.Version;
import de.klaro.reformcloud2.executor.api.common.network.channel.PacketSender;
import de.klaro.reformcloud2.executor.api.common.network.channel.manager.DefaultChannelManager;
import de.klaro.reformcloud2.executor.api.common.network.packet.Packet;
import de.klaro.reformcloud2.executor.api.common.process.ProcessInformation;
import de.klaro.reformcloud2.executor.api.common.process.ProcessState;
import de.klaro.reformcloud2.executor.api.packets.out.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public final class PlayerListenerHandler implements Listener {

    //Note: Cannot send always the same version like on velocity because it can support MCPE or not
    private final Version version = BungeeExecutor.getInstance().getThisProcessInformation().getTemplate().getVersion();

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(final ServerConnectEvent event) {
        final ProxiedPlayer proxiedPlayer = event.getPlayer();
        proxiedPlayer.setReconnectServer(null);
        if (proxiedPlayer.getServer() == null) {
            DefaultChannelManager.INSTANCE.get("Controller").ifPresent(packetSender -> {
                Packet result = BungeeExecutor.getInstance().packetHandler().getQueryHandler().sendQueryAsync(packetSender,
                        new APIPacketOutGetBestLobbyForPlayer(proxiedPlayer.getPermissions(), version)
                ).getTask().getUninterruptedly(TimeUnit.SECONDS, 3);
                if (result != null) {
                    ProcessInformation info = result.content().get("result", ProcessInformation.TYPE);
                    if (info != null && ProxyServer.getInstance().getServers().containsKey(info.getName())) {
                        event.setTarget(ProxyServer.getInstance().getServerInfo(info.getName()));
                        return;
                    }
                }

                proxiedPlayer.disconnect(TextComponent.fromLegacyText("There is currently no lobby server available"));
                event.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void handle(final ServerConnectedEvent event) {
        DefaultChannelManager.INSTANCE.get("Controller").ifPresent(sender -> sender.sendPacket(new APIBungeePacketOutPlayerServerSwitch(
                event.getPlayer().getUniqueId(),
                event.getServer().getInfo().getName()
        )));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(final LoginEvent event) {
        PacketSender sender = DefaultChannelManager.INSTANCE.get("Controller").orElse(null);
        if (sender == null) {
            event.setCancelReason(TextComponent.fromLegacyText("§4§lThe current proxy is not connected to the controller"));
            event.setCancelled(true);
            return;
        }

        if (ExecutorAPI.getInstance().getThisProcessInformation().getProcessGroup().getPlayerAccessConfiguration().isOnlyProxyJoin()) {
            PendingConnection connection = event.getConnection();
            sender.sendPacket(new APIPacketOutCreateLoginRequest(
                    connection.getUniqueId(),
                    connection.getName()
            ));
        }
    }

    @EventHandler
    public void handle(final PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final ProcessInformation current = ExecutorAPI.getInstance().getThisProcessInformation();
        final PlayerAccessConfiguration configuration = current.getProcessGroup().getPlayerAccessConfiguration();

        if (configuration.isUseCloudPlayerLimit()
                && configuration.getMaxPlayers() < current.getOnlineCount() + 1
                && !player.hasPermission("reformcloud.join.full")) {
            player.disconnect(TextComponent.fromLegacyText("§4§lThe proxy is full"));
            return;
        }

        if (configuration.isJoinOnlyPerPermission()
                && configuration.getJoinPermission() != null
                && !player.hasPermission(configuration.getJoinPermission())) {
            player.disconnect(TextComponent.fromLegacyText("§4§lYou do not have permission to enter this proxy"));
            return;
        }

        if (configuration.isMaintenance()
                && configuration.getMaintenanceJoinPermission() != null
                && !player.hasPermission(configuration.getMaintenanceJoinPermission())) {
            player.disconnect(TextComponent.fromLegacyText("§4§lThis proxy is currently in maintenance"));
            return;
        }

        if (current.getProcessState().equals(ProcessState.FULL) && !player.hasPermission("reformcloud.join.full")) {
            player.disconnect(TextComponent.fromLegacyText("§4§lYou are not allowed to join this server in the current state"));
            return;
        }

        if (ProxyServer.getInstance().getOnlineCount() >= current.getMaxPlayers()
                && !current.getProcessState().equals(ProcessState.FULL)
                && !current.getProcessState().equals(ProcessState.INVISIBLE)) {
            current.setProcessState(ProcessState.FULL);
        }

        current.onLogin(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        current.updateRuntimeInformation();
        BungeeExecutor.getInstance().setThisProcessInformation(current); //Update it directly on the current host to prevent issues
        ExecutorAPI.getInstance().update(current);

        CommonHelper.EXECUTOR.execute(() -> DefaultChannelManager.INSTANCE.get("Controller").ifPresent(packetSender -> packetSender.sendPacket(new APIPacketOutPlayerLoggedIn(event.getPlayer().getName()))));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(final ServerKickEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();
        DefaultChannelManager.INSTANCE.get("Controller").ifPresent(packetSender -> {
            Packet result = BungeeExecutor.getInstance().packetHandler().getQueryHandler().sendQueryAsync(packetSender,
                    new APIPacketOutGetBestLobbyForPlayer(proxiedPlayer.getPermissions(), version)
            ).getTask().getUninterruptedly(TimeUnit.SECONDS, 3);
            if (result != null) {
                ProcessInformation info = result.content().get("result", ProcessInformation.TYPE);
                if (info != null && ProxyServer.getInstance().getServers().containsKey(info.getName())) {
                    event.setCancelled(true);
                    event.setCancelServer(ProxyServer.getInstance().getServerInfo(info.getName()));
                    proxiedPlayer.sendMessage(event.getKickReasonComponent());
                    return;
                }
            }

            proxiedPlayer.disconnect(TextComponent.fromLegacyText("There is currently no lobby server available"));
            event.setCancelled(false);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(final PlayerDisconnectEvent event) {
        DefaultChannelManager.INSTANCE.get("Controller").ifPresent(packetSender -> packetSender.sendPacket(new APIPacketOutLogoutPlayer(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName()
        )));

        CommonHelper.EXECUTOR.execute(() -> {
            ProcessInformation current = ExecutorAPI.getInstance().getThisProcessInformation();
            if (ProxyServer.getInstance().getOnlineCount() < current.getMaxPlayers()
                    && !current.getProcessState().equals(ProcessState.READY)
                    && !current.getProcessState().equals(ProcessState.INVISIBLE)) {
                current.setProcessState(ProcessState.READY);
            }

            current.updateRuntimeInformation();
            current.onLogout(event.getPlayer().getUniqueId());
            BungeeExecutor.getInstance().setThisProcessInformation(current);
            ExecutorAPI.getInstance().update(current);
        });
    }

    @EventHandler
    public void handle(final ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) && event.isCommand()) {
            return;
        }

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();
        DefaultChannelManager.INSTANCE.get("Controller").ifPresent(packetSender -> packetSender.sendPacket(new APIPacketOutPlayerCommandExecute(
                proxiedPlayer.getName(),
                proxiedPlayer.getUniqueId(),
                event.getMessage().replaceFirst("/", "")
        )));
    }
}