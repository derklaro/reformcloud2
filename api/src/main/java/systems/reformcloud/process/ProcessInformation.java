/*
 * This file is part of reformcloud, licensed under the MIT License (MIT).
 *
 * Copyright (c) ReformCloud <https://github.com/ReformCloud>
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
package systems.reformcloud.process;

import org.jetbrains.annotations.NotNull;
import systems.reformcloud.ExecutorAPI;
import systems.reformcloud.configuration.data.JsonDataHolder;
import systems.reformcloud.functional.Sorted;
import systems.reformcloud.group.process.ProcessGroup;
import systems.reformcloud.group.template.Template;
import systems.reformcloud.network.address.NetworkAddress;
import systems.reformcloud.network.data.SerializableObject;
import systems.reformcloud.process.builder.ProcessBuilder;
import systems.reformcloud.utility.name.Nameable;
import systems.reformcloud.wrappers.ProcessWrapper;

import java.util.Optional;
import java.util.UUID;

public interface ProcessInformation extends JsonDataHolder<ProcessInformation>, ProcessStateHolder, ProcessInclusionHolder, PlayerHolder, Nameable, Sorted<ProcessInformation>, SerializableObject, Cloneable {

  @NotNull
  static Optional<ProcessWrapper> getByName(@NotNull String name) {
    return ExecutorAPI.getInstance().getProcessProvider().getProcessByName(name);
  }

  @NotNull
  static Optional<ProcessWrapper> getByUniqueId(@NotNull UUID uniqueId) {
    return ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(uniqueId);
  }

  @NotNull
  static ProcessBuilder builder(@NotNull String group) {
    return ExecutorAPI.getInstance().getProcessProvider().createProcess().group(group);
  }

  @NotNull
  static ProcessBuilder builder(@NotNull ProcessGroup group) {
    return ExecutorAPI.getInstance().getProcessProvider().createProcess().group(group);
  }

  @NotNull
  Identity getId();

  @NotNull
  NetworkAddress getHost();

  @NotNull
  Template getPrimaryTemplate();

  @NotNull
  ProcessGroup getProcessGroup();

  void setProcessGroup(@NotNull ProcessGroup processGroup);

  @NotNull
  ProcessRuntimeInformation getRuntimeInformation();

  void setRuntimeInformation(@NotNull ProcessRuntimeInformation information);

  @NotNull
  ProcessInformation clone();

  void update();
}
