package systems.reformcloud.reformcloud2.executor.api.common.api.basic.packets.api;

import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.api.basic.ExternalAPIImplementation;
import systems.reformcloud.reformcloud2.executor.api.common.groups.MainGroup;
import systems.reformcloud.reformcloud2.executor.api.common.network.challenge.ChallengeAuthHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.NetworkChannelReader;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.data.ProtocolBuffer;
import systems.reformcloud.reformcloud2.executor.api.common.network.handler.ChannelReaderHelper;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;

public class PacketAPIGroupUpdateMainGroup extends Packet {

    public PacketAPIGroupUpdateMainGroup() {
    }

    public PacketAPIGroupUpdateMainGroup(MainGroup mainGroup) {
        this.mainGroup = mainGroup;
    }

    private MainGroup mainGroup;

    @Override
    public int getId() {
        return ExternalAPIImplementation.EXTERNAL_PACKET_ID + 18;
    }

    @Override
    public void handlePacketReceive(@NotNull NetworkChannelReader reader, @NotNull ChallengeAuthHandler authHandler, @NotNull ChannelReaderHelper parent, @Nullable PacketSender sender, @NotNull ChannelHandlerContext channel) {
        ExecutorAPI.getInstance().getSyncAPI().getGroupSyncAPI().updateMainGroup(this.mainGroup);
    }

    @Override
    public void write(@NotNull ProtocolBuffer buffer) {
        buffer.writeObject(this.mainGroup);
    }

    @Override
    public void read(@NotNull ProtocolBuffer buffer) {
        this.mainGroup = buffer.readObject(MainGroup.class);
    }
}