package systems.reformcloud.reformcloud2.executor.api.common.api.basic.packets.api;

import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import systems.reformcloud.reformcloud2.executor.api.common.ExecutorAPI;
import systems.reformcloud.reformcloud2.executor.api.common.api.basic.ExternalAPIImplementation;
import systems.reformcloud.reformcloud2.executor.api.common.network.challenge.ChallengeAuthHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.NetworkChannelReader;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.data.ProtocolBuffer;
import systems.reformcloud.reformcloud2.executor.api.common.network.handler.ChannelReaderHelper;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;

public class PacketAPIDatabaseDeleteDocument extends Packet {

    public PacketAPIDatabaseDeleteDocument() {
    }

    public PacketAPIDatabaseDeleteDocument(String databaseName, String entryKey, String identifier) {
        this.databaseName = databaseName;
        this.entryKey = entryKey;
        this.identifier = identifier;
    }

    private String databaseName;

    private String entryKey;

    private String identifier;

    @Override
    public int getId() {
        return ExternalAPIImplementation.EXTERNAL_PACKET_ID + 13;
    }

    @Override
    public void handlePacketReceive(@NotNull NetworkChannelReader reader, @NotNull ChallengeAuthHandler authHandler, @NotNull ChannelReaderHelper parent, @Nullable PacketSender sender, @NotNull ChannelHandlerContext channel) {
        if (entryKey != null) {
            ExecutorAPI.getInstance().getSyncAPI().getDatabaseSyncAPI().remove(this.databaseName, this.entryKey);
            return;
        }

        if (identifier != null) {
            ExecutorAPI.getInstance().getSyncAPI().getDatabaseSyncAPI().removeIfAbsent(this.databaseName, this.identifier);
        }
    }

    @Override
    public void write(@NotNull ProtocolBuffer buffer) {
        buffer.writeString(this.databaseName);
        buffer.writeString(this.entryKey);
        buffer.writeString(this.identifier);
    }

    @Override
    public void read(@NotNull ProtocolBuffer buffer) {
        this.databaseName = buffer.readString();
        this.entryKey = buffer.readString();
        this.identifier = buffer.readString();
    }
}