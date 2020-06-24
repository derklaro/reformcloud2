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
package systems.refomcloud.reformcloud2.embedded.group;

import org.jetbrains.annotations.NotNull;
import systems.refomcloud.reformcloud2.embedded.Embedded;
import systems.reformcloud.reformcloud2.executor.api.groups.MainGroup;
import systems.reformcloud.reformcloud2.executor.api.network.packet.Packet;
import systems.reformcloud.reformcloud2.executor.api.task.Task;
import systems.reformcloud.reformcloud2.protocol.node.ApiToNodeCreateMainGroup;
import systems.reformcloud.reformcloud2.protocol.node.ApiToNodeCreateMainGroupResult;
import systems.reformcloud.reformcloud2.shared.group.DefaultMainGroupBuilder;

import java.util.Optional;

class DefaultEmbeddedMainGroupBuilder extends DefaultMainGroupBuilder {

    @NotNull
    @Override
    public Task<MainGroup> create() {
        return Task.supply(() -> {
            Optional<Packet> result = Embedded.getInstance().sendSyncQuery(new ApiToNodeCreateMainGroup(super.name, super.subGroups));
            if (!result.isPresent() || !(result.get() instanceof ApiToNodeCreateMainGroupResult)) {
                return null;
            }

            return ((ApiToNodeCreateMainGroupResult) result.get()).getMainGroup();
        });
    }
}
