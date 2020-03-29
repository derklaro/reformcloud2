package systems.reformcloud.reformcloud2.permissions.packets.api.in;

import org.jetbrains.annotations.NotNull;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.PacketSender;
import systems.reformcloud.reformcloud2.executor.api.common.network.channel.handler.DefaultJsonNetworkHandler;
import systems.reformcloud.reformcloud2.executor.api.common.network.packet.Packet;
import systems.reformcloud.reformcloud2.permissions.PermissionAPI;
import systems.reformcloud.reformcloud2.permissions.packets.PacketHelper;
import systems.reformcloud.reformcloud2.permissions.packets.util.PermissionAction;
import systems.reformcloud.reformcloud2.permissions.util.group.PermissionGroup;

import java.util.function.Consumer;

public class APIPacketInGroupAction extends DefaultJsonNetworkHandler {

    public APIPacketInGroupAction() {
        super(PacketHelper.PERMISSION_BUS + 2);
    }

    @Override
    public void handlePacket(@NotNull PacketSender packetSender, @NotNull Packet packet, @NotNull Consumer<Packet> responses) {
        final PermissionGroup permissionGroup = packet.content().get("group", PermissionGroup.TYPE);
        final PermissionAction permissionAction = packet.content().get("action", PermissionAction.class);

        switch (permissionAction) {
            case UPDATE: {
                PermissionAPI.getInstance().getPermissionUtil().handleInternalPermissionGroupUpdate(permissionGroup);
                break;
            }

            case DELETE: {
                PermissionAPI.getInstance().getPermissionUtil().handleInternalPermissionGroupDelete(permissionGroup);
                break;
            }

            case CREATE: {
                PermissionAPI.getInstance().getPermissionUtil().handleInternalPermissionGroupCreate(permissionGroup);
                break;
            }

            case DEFAULT_GROUPS_CHANGED: {
                PermissionAPI.getInstance().getPermissionUtil().handleInternalDefaultGroupsUpdate();
                break;
            }
        }
    }
}
