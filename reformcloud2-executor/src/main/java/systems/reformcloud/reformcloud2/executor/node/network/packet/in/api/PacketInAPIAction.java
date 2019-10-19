package systems.reformcloud.reformcloud2.executor.node.network.packet.in.api;

import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.api.basic.packets.out.ExternalAPIPacketOutAPIAction;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.handler.NetworkHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;

import java.util.UUID;
import java.util.function.Consumer;

public class PacketInAPIAction implements NetworkHandler {

    @Override
    public int getHandlingPacketID() {
        return 46;
    }

    @Override
    public void handlePacket(PacketSender packetSender, Packet packet, Consumer<Packet> responses) {
        ExternalAPIPacketOutAPIAction.APIAction apiAction = packet.content().get("action", ExternalAPIPacketOutAPIAction.APIAction.class);
        UUID targetPlayer = packet.content().get("1", UUID.class);

        switch (apiAction) {
            case CONNECT: {
                ExecutorAPI.getInstance().connect(targetPlayer, packet.content().getString("2"));
                break;
            }

            case CONNECT_PLAYER: {
                UUID uuid = packet.content().get("2", UUID.class);
                ExecutorAPI.getInstance().connect(targetPlayer, uuid);
                break;
            }

            case RESPAWN: {
                ExecutorAPI.getInstance().respawn(targetPlayer);
                break;
            }

            case PLAY_SOUND: {
                ExecutorAPI.getInstance().playSound(targetPlayer,
                        packet.content().getString("2"),
                        packet.content().get("3", Float.class),
                        packet.content().get("4", Float.class)
                );
                break;
            }

            case SEND_TITLE: {
                ExecutorAPI.getInstance().sendTitle(targetPlayer,
                        packet.content().getString("2"),
                        packet.content().getString("3"),
                        packet.content().getInteger("4"),
                        packet.content().getInteger("5"),
                        packet.content().getInteger("6")
                );
                break;
            }

            case KICK_PLAYER: {
                ExecutorAPI.getInstance().kickPlayer(targetPlayer, packet.content().getString("2"));
                break;
            }

            case KICK_SERVER: {
                ExecutorAPI.getInstance().kickPlayerFromServer(targetPlayer, packet.content().getString("2"));
                break;
            }

            case PLAY_EFFECT: {
                ExecutorAPI.getInstance().playEffect(targetPlayer,
                        packet.content().getString("2"),
                        packet.content().get("3", Object.class)
                );
                break;
            }

            case PLAY_ENTITY_EFFECT: {
                ExecutorAPI.getInstance().playEffect(targetPlayer, packet.content().getString("2"));
                break;
            }

            case SEND_MESSAGE: {
                ExecutorAPI.getInstance().sendMessage(targetPlayer, packet.content().getString("2"));
                break;
            }

            case LOCATION_TELEPORT: {
                ExecutorAPI.getInstance().teleport(targetPlayer,
                        packet.content().getString("2"),
                        packet.content().get("3", Double.class),
                        packet.content().get("4", Double.class),
                        packet.content().get("5", Double.class),
                        packet.content().get("6", Float.class),
                        packet.content().get("7", Float.class)
                );
                break;
            }

            case SET_RESOURCE_PACK: {
                ExecutorAPI.getInstance().setResourcePack(targetPlayer, packet.content().getString("2"));
                break;
            }
        }
    }
}