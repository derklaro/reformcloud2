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
package systems.reformcloud.node.commands;

import org.jetbrains.annotations.NotNull;
import systems.reformcloud.ExecutorAPI;
import systems.reformcloud.command.Command;
import systems.reformcloud.command.CommandSender;
import systems.reformcloud.group.process.ProcessGroup;
import systems.reformcloud.group.template.Template;
import systems.reformcloud.language.TranslationHolder;
import systems.reformcloud.process.ProcessState;
import systems.reformcloud.process.builder.ProcessBuilder;
import systems.reformcloud.process.builder.ProcessInclusion;
import systems.reformcloud.utility.MoreCollections;
import systems.reformcloud.shared.StringUtil;
import systems.reformcloud.shared.parser.Parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public final class CommandLaunch implements Command {

  @NotNull
  private static Collection<ProcessInclusion> parseInclusions(@NotNull String from) {
    Collection<ProcessInclusion> out = new ArrayList<>();
    for (String inclusion : from.split(";")) {
      String[] parts = inclusion.split(",");
      if (parts.length != 2) {
        continue;
      }

      out.add(ProcessInclusion.inclusion(parts[0], parts[1]));
    }

    return out;
  }

  public void describeCommandToSender(@NotNull CommandSender source) {
    source.sendMessages((
      "launch <group-name>            | Creates a new process bases on the group\n" +
        " --template=[template]         | Uses a specific template for the startup (default: random)\n" +
        " --unique-id=[unique-id]       | Sets the unique id of the new process (default: random)\n" +
        " --display-name=[display-name] | Sets the display name of the new process (default: none)\n" +
        " --max-memory=[memory]         | Sets the maximum amount of memory for the new process (default: group-based)\n" +
        " --id=[id]                     | Sets the id of the new process (default: chosen from amount of online processes)\n" +
        " --max-players=[max-players]   | Sets the maximum amount of players for the process (default: group-based)\n" +
        " --inclusions=[url,name;...]   | Sets the inclusions of the process (default: none)\n" +
        " --amount=[amount]             | Starts the specified amount of processes (default: 1)\n" +
        " --prepare-only=[prepare-only] | Prepares the process but does not start it (default: false)"
    ).split("\n"));
  }

  @Override
  public void process(@NotNull CommandSender sender, String[] strings, @NotNull String commandLine) {
    if (strings.length == 0) {
      this.describeCommandToSender(sender);
      return;
    }

    Optional<ProcessGroup> base = ExecutorAPI.getInstance().getProcessGroupProvider().getProcessGroup(strings[0]);
    if (!base.isPresent()) {
      sender.sendMessage(TranslationHolder.translate("command-launch-start-not-possible-group-not-exists", strings[0]));
      return;
    }

    ProcessBuilder builder = ExecutorAPI.getInstance().getProcessProvider().createProcess().group(base.get());
    Properties properties = StringUtil.parseProperties(strings, 1);

    boolean prepareOnly = false;
    int amount = 1;

    if (properties.containsKey("template")) {
      Template baseTemplate = MoreCollections.filter(base.get().getTemplates(), e -> e.getName().equals(properties.getProperty("template")));
      if (baseTemplate == null) {
        sender.sendMessage(TranslationHolder.translate("command-launch-template-not-exists", properties.getProperty("template"), base.get().getName()));
        return;
      }

      builder.template(baseTemplate);
    }

    if (properties.containsKey("unique-id")) {
      UUID uniqueID = Parsers.UNIQUE_ID.parse(properties.getProperty("unique-id"));
      if (uniqueID == null) {
        sender.sendMessage(TranslationHolder.translate("command-unique-id-failed", properties.getProperty("unique-id")));
        return;
      }

      builder.uniqueId(uniqueID);
    }

    if (properties.containsKey("display-name")) {
      builder.displayName(properties.getProperty("display-name"));
    }

    if (properties.containsKey("max-memory")) {
      Integer maxMemory = Parsers.INT.parse(properties.getProperty("max-memory"));
      if (maxMemory == null || maxMemory <= 100) {
        sender.sendMessage(TranslationHolder.translate("command-integer-failed", 100, properties.getProperty("max-memory")));
        return;
      }

      builder.memory(maxMemory);
    }

    if (properties.containsKey("id")) {
      Integer id = Parsers.INT.parse(properties.getProperty("id"));
      if (id == null || id <= 0) {
        sender.sendMessage(TranslationHolder.translate("command-integer-failed", 0, properties.getProperty("id")));
        return;
      }

      builder.id(id);
    }

    if (properties.containsKey("max-players")) {
      Integer maxPlayers = Parsers.INT.parse(properties.getProperty("max-players"));
      if (maxPlayers == null || maxPlayers <= 0) {
        sender.sendMessage(TranslationHolder.translate("command-integer-failed", 0, properties.getProperty("max-players")));
        return;
      }

      builder.maxPlayers(maxPlayers);
    }

    if (properties.containsKey("inclusions")) {
      builder.inclusions(parseInclusions(properties.getProperty("inclusions")));
    }

    if (properties.containsKey("prepare-only")) {
      Boolean prepare = Parsers.BOOLEAN.parse(properties.getProperty("prepare-only"));
      if (prepare == null) {
        sender.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("prepare-only")));
        return;
      }

      prepareOnly = prepare;
    }

    if (properties.containsKey("amount")) {
      Integer amountToStart = Parsers.INT.parse(properties.getProperty("amount"));
      if (amountToStart == null || amountToStart <= 0) {
        sender.sendMessage(TranslationHolder.translate("command-integer-failed", 0, properties.getProperty("amount")));
        return;
      }

      amount = amountToStart;
    }

    if (prepareOnly) {
      for (int i = 1; i <= amount; i++) {
        builder.prepare();
      }

      sender.sendMessage(TranslationHolder.translate("command-launch-prepared-processes", amount, base.get().getName()));
    } else {
      for (int i = 1; i <= amount; i++) {
        builder.prepare().thenAccept(e -> e.setRuntimeStateAsync(ProcessState.STARTED));
      }

      sender.sendMessage(TranslationHolder.translate("command-launch-started-processes", amount, base.get().getName()));
    }
  }

  @Override
  public @NotNull List<String> suggest(@NotNull CommandSender commandSender, String[] strings, int bufferIndex, @NotNull String commandLine) {
    List<String> result = new ArrayList<>();
    if (bufferIndex == 0) {
      result.addAll(ExecutorAPI.getInstance().getProcessGroupProvider().getProcessGroupNames());
    } else if (bufferIndex >= 1) {
      result.addAll(Arrays.asList("--template=", "--unique-id=" + UUID.randomUUID(), "--display-name=",
        "--max-memory=512", "--id=", "--max-players=20", "--inclusions=", "--amount=1", "--prepare-only=false"));
    }

    return result;
  }
}