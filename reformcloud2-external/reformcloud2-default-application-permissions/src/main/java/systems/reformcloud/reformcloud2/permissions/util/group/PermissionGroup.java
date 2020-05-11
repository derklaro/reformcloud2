/*
 * MIT License
 *
 * Copyright (c) ReformCloud-Team
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package systems.reformcloud.reformcloud2.permissions.util.group;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import systems.reformcloud.reformcloud2.executor.api.common.network.SerializableObject;
import systems.reformcloud.reformcloud2.executor.api.common.network.data.ProtocolBuffer;
import systems.reformcloud.reformcloud2.permissions.util.basic.checks.GeneralCheck;
import systems.reformcloud.reformcloud2.permissions.util.basic.checks.WildcardCheck;
import systems.reformcloud.reformcloud2.permissions.util.permission.PermissionNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PermissionGroup implements SerializableObject {

    public static final TypeToken<PermissionGroup> TYPE = new TypeToken<PermissionGroup>() {
    };

    public PermissionGroup(
            @NotNull Collection<PermissionNode> permissionNodes,
            @NotNull Map<String, Collection<PermissionNode>> perGroupPermissions,
            @NotNull Collection<String> subGroups,
            @NotNull String name,
            int priority
    ) {
        this.permissionNodes = permissionNodes;
        this.perGroupPermissions = perGroupPermissions;
        this.subGroups = subGroups;
        this.name = name;
        this.priority = priority;
    }

    private Collection<PermissionNode> permissionNodes;

    private Map<String, Collection<PermissionNode>> perGroupPermissions;

    private Collection<String> subGroups;

    private String name;

    private int priority;

    private @Nullable String prefix;

    private @Nullable String suffix;

    private @Nullable String display;

    private @Nullable String colour;

    @NotNull
    public Collection<PermissionNode> getPermissionNodes() {
        return permissionNodes;
    }

    @NotNull
    public Map<String, Collection<PermissionNode>> getPerGroupPermissions() {
        return perGroupPermissions;
    }

    @NotNull
    public Collection<String> getSubGroups() {
        return subGroups;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @NotNull
    public Optional<String> getPrefix() {
        return Optional.ofNullable(this.prefix);
    }

    @NotNull
    public Optional<String> getSuffix() {
        return Optional.ofNullable(this.suffix);
    }

    @NotNull
    public Optional<String> getDisplay() {
        return Optional.ofNullable(this.display);
    }

    @NotNull
    public Optional<String> getColour() {
        return Optional.ofNullable(this.colour);
    }

    public void setPrefix(@Nullable String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(@Nullable String suffix) {
        this.suffix = suffix;
    }

    public void setDisplay(@Nullable String display) {
        this.display = display;
    }

    public void setColour(@Nullable String colour) {
        this.colour = colour;
    }

    public boolean hasPermission(@NotNull String perm) {
        if (WildcardCheck.hasWildcardPermission(this, perm)) {
            return true;
        }

        return GeneralCheck.hasPermission(this, perm);
    }

    @Override
    public void write(@NotNull ProtocolBuffer buffer) {
        buffer.writeObjects(this.permissionNodes);

        buffer.writeVarInt(this.perGroupPermissions.size());
        for (Map.Entry<String, Collection<PermissionNode>> stringCollectionEntry : this.perGroupPermissions.entrySet()) {
            buffer.writeString(stringCollectionEntry.getKey());
            buffer.writeObjects(stringCollectionEntry.getValue());
        }

        buffer.writeStringArray(this.subGroups);
        buffer.writeString(this.name);
        buffer.writeInt(this.priority);

        buffer.writeString(this.prefix);
        buffer.writeString(this.suffix);
        buffer.writeString(this.display);
        buffer.writeString(this.colour);
    }

    @Override
    public void read(@NotNull ProtocolBuffer buffer) {
        this.permissionNodes = buffer.readObjects(PermissionNode.class);

        int size = buffer.readVarInt();
        this.perGroupPermissions = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            this.perGroupPermissions.put(buffer.readString(), buffer.readObjects(PermissionNode.class));
        }

        this.subGroups = buffer.readStringArray();
        this.name = buffer.readString();
        this.priority = buffer.readInt();

        this.prefix = buffer.readString();
        this.suffix = buffer.readString();
        this.display = buffer.readString();
        this.colour = buffer.readString();
    }
}
