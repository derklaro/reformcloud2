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
package systems.reformcloud.reformcloud2.signs.event;

import systems.reformcloud.reformcloud2.executor.api.common.event.Event;
import systems.reformcloud.reformcloud2.signs.util.sign.CloudSign;

import java.util.UUID;
import java.util.function.Function;

public class UserSignPreConnectEvent extends Event {

    public UserSignPreConnectEvent(UUID playerUniqueId, Function<String, Boolean> permissionChecker, CloudSign target, boolean allowConnection) {
        this.playerUniqueId = playerUniqueId;
        this.permissionChecker = permissionChecker;
        this.target = target;
        this.allowConnection = allowConnection;
    }

    private final UUID playerUniqueId;

    private final Function<String, Boolean> permissionChecker;

    private final CloudSign target;

    private boolean allowConnection;

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    public Function<String, Boolean> getPermissionChecker() {
        return permissionChecker;
    }

    public CloudSign getTarget() {
        return target;
    }

    public boolean isAllowConnection() {
        return allowConnection;
    }

    public void setAllowConnection(boolean allowConnection) {
        this.allowConnection = allowConnection;
    }
}
