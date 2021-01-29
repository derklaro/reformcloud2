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
import systems.reformcloud.group.main.MainGroup;
import systems.reformcloud.group.process.ProcessGroup;
import systems.reformcloud.group.template.Template;
import systems.reformcloud.group.template.backend.TemplateBackend;
import systems.reformcloud.group.template.version.Version;
import systems.reformcloud.group.template.version.Versions;
import systems.reformcloud.language.TranslationHolder;
import systems.reformcloud.process.ProcessInformation;
import systems.reformcloud.process.ProcessState;
import systems.reformcloud.utility.MoreCollections;
import systems.reformcloud.wrappers.ProcessWrapper;
import systems.reformcloud.node.template.TemplateBackendManager;
import systems.reformcloud.shared.StringUtil;
import systems.reformcloud.shared.parser.Parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public final class CommandGroup implements Command {

  public void describeCommandToSender(@NotNull CommandSender source) {
    source.sendMessages((
      "group <list>                                   | Shows all registered main and process groups\n" +
        "group <sub | main> <name> [info]               | Shows information about a specific group\n" +
        "group <sub | main> <name> [delete]             | Deletes the specified process group\n" +
        "group <sub | main> <name> [stop]               | Stops either all non-prepared processes of the group or all sub groups of the main group which are not prepared\n" +
        "group <sub | main> <name> [kill]               | Stops either all processes of the group or all sub groups of the main group\n" +
        " \n" +
        "group <sub> <name> [edit]                      | Edits the specified group\n" +
        " --maintenance=[maintenance]                   | Enables or disables the maintenance mode\n" +
        " --static=[static]                             | Enables or disables the deleting of the process after the stop\n" +
        " --lobby=[lobby]                               | Sets if the group can be used as lobby\n" +
        " --max-players=[max]                           | Sets the max player count for the process\n" +
        " --min-process-count=[min]                     | Sets the min process count for the process\n" +
        " --max-process-count=[max]                     | Sets the max process count for the process\n" +
        " --always-prepared-process-count=[count]       | Sets the count of processes which should always be prepared\n" +
        " --max-memory=[default/memory]                 | Sets the max memory of the template (format: <template-name>/<max-memory>)\n" +
        " --startup-pickers=[Node1;Node2]               | Sets the startup pickers for the group\n" +
        " --add-startup-pickers=[Node1;Node2]           | Adds the specified startup pickers to the group\n" +
        " --remove-startup-pickers=[Node1;Node2]        | Removes the specified startup pickers from the group\n" +
        " --clear-startup-pickers=true                  | Clears the startup pickers\n" +
        " --templates=[default/FILE/WATERFALL;...]      | Sets the templates of the group (format: <name>/<backend>/<version>)\n" +
        " --add-templates=[default/FILE/WATERFALL;...]  | Adds the specified templates to the group (format: <name>/<backend>/<version>)\n" +
        " --remove-templates=[default;global]           | Removes the specified templates from the group\n" +
        " --clear-templates=true                        | Clears the templates of the group\n" +
        " \n" +
        "group <main> <name> [edit]                     | Edits the specified main group\n" +
        " --sub-groups=[Group1;Group2]                  | Sets the sub groups of the main group\n" +
        " --add-sub-groups=[Group1;Group2]              | Adds the sub groups to the main group\n" +
        " --remove-sub-groups=[Group1;Group2]           | Removes the sub groups from the main group\n" +
        " --clear-sub-groups=true                       | Clears the sub groups of the main group"
    ).split("\n"));
  }

  @Override
  public void process(@NotNull CommandSender sender, String[] strings, @NotNull String commandLine) {
    if (strings.length == 1 && strings[0].equalsIgnoreCase("list")) {
      this.listGroupsToSender(sender);
      return;
    }

    if (strings.length <= 2) {
      this.describeCommandToSender(sender);
      return;
    }

    Properties properties = StringUtil.parseProperties(strings, 2);
    if (strings[0].equalsIgnoreCase("sub")) {
      this.handleSubGroupRequest(sender, strings, properties);
      return;
    }

    if (strings[0].equalsIgnoreCase("main")) {
      this.handleMainGroupRequest(sender, strings, properties);
      return;
    }

    this.describeCommandToSender(sender);
  }

  @Override
  public @NotNull List<String> suggest(@NotNull CommandSender commandSender, String[] strings, int bufferIndex, @NotNull String commandLine) {
    List<String> result = new ArrayList<>();
    if (bufferIndex == 0) {
      result.addAll(Arrays.asList("list", "sub", "main"));
    } else if (bufferIndex == 1) {
      if (strings[0].equalsIgnoreCase("sub")) {
        result.addAll(ExecutorAPI.getInstance().getProcessGroupProvider().getProcessGroupNames());
      } else if (strings[0].equalsIgnoreCase("main")) {
        result.addAll(ExecutorAPI.getInstance().getMainGroupProvider().getMainGroupNames());
      }
    } else if (bufferIndex == 2) {
      result.addAll(Arrays.asList("stop", "kill", "info", "delete", "edit"));
    } else if (bufferIndex >= 3) {
      if (strings[2].equalsIgnoreCase("edit") && strings[0].equalsIgnoreCase("sub")) {
        result.addAll(Arrays.asList("--maintenance=false", "--static=false", "--max-players=512", "--min-process-count=1",
          "--max-process-count=-1", "--always-prepared-process-count=1", "--start-port=25565", "--max-memory=512",
          "--startup-pickers=", "--add-startup-pickers=", "--remove-startup-pickers=", "--clear-startup-pickers=true", "--lobby=true",
          "--templates=default/FILE/PAPER_1_8_8", "--add-templates=default/FILE/PAPER_1_8_8", "--remove-templates=default", "--clear-templates=true"));
      } else if (strings[2].equalsIgnoreCase("edit") && strings[0].equalsIgnoreCase("main")) {
        result.addAll(Arrays.asList("--sub-groups=", "--add-sub-groups=", "--remove-sub-groups=", "--clear-sub-groups=true"));
      }
    }

    return result;
  }

  private void handleSubGroupRequest(CommandSender source, String[] strings, Properties properties) {
    Optional<ProcessGroup> processGroup = ExecutorAPI.getInstance().getProcessGroupProvider().getProcessGroup(strings[1]);
    if (!processGroup.isPresent()) {
      source.sendMessage(TranslationHolder.translate("command-group-sub-group-not-exists", strings[1]));
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("stop")) {
      List<ProcessInformation> processes = ExecutorAPI.getInstance()
        .getProcessProvider()
        .getProcessesByProcessGroup(processGroup.get().getName())
        .stream()
        .filter(e -> !e.getCurrentState().equals(ProcessState.PREPARED))
        .collect(Collectors.toList());
      source.sendMessage(TranslationHolder.translate("command-group-stopping-all-not-prepared", processGroup.get().getName()));

      for (ProcessInformation process : processes) {
        Optional<ProcessWrapper> wrapper = ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(process.getId().getUniqueId());
        wrapper.ifPresent(processWrapper -> processWrapper.setRuntimeStateAsync(ProcessState.STOPPED));
      }
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("kill")) {
      Collection<ProcessInformation> processes = ExecutorAPI.getInstance()
        .getProcessProvider()
        .getProcessesByProcessGroup(processGroup.get().getName());
      source.sendMessage(TranslationHolder.translate("command-group-stopping-all", processGroup.get().getName()));
      for (ProcessInformation process : processes) {
        Optional<ProcessWrapper> wrapper = ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(process.getId().getUniqueId());
        wrapper.ifPresent(processWrapper -> processWrapper.setRuntimeStateAsync(ProcessState.STOPPED));
      }
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("info")) {
      this.describeProcessGroupToSender(source, processGroup.get());
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("delete")) {
      ExecutorAPI.getInstance().getProcessGroupProvider().deleteProcessGroup(processGroup.get().getName());

      Collection<ProcessInformation> processes = ExecutorAPI.getInstance()
        .getProcessProvider()
        .getProcessesByProcessGroup(processGroup.get().getName());
      for (ProcessInformation process : processes) {
        Optional<ProcessWrapper> wrapper = ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(process.getId().getUniqueId());
        wrapper.ifPresent(processWrapper -> processWrapper.setRuntimeStateAsync(ProcessState.STOPPED));
      }

      source.sendMessage(TranslationHolder.translate("command-group-sub-delete", processGroup.get().getName()));
      return;
    }

    if (strings.length >= 4 && strings[2].equalsIgnoreCase("edit")) {
      if (properties.containsKey("maintenance")) {
        Boolean maintenance = Parsers.BOOLEAN.parse(properties.getProperty("maintenance"));
        if (maintenance == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("maintenance")));
          return;
        }

        processGroup.get().getPlayerAccessConfiguration().setMaintenance(maintenance);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "maintenance",
          processGroup.get().getPlayerAccessConfiguration().isMaintenance()
        ));
      }

      if (properties.containsKey("static")) {
        Boolean isStatic = Parsers.BOOLEAN.parse(properties.getProperty("static"));
        if (isStatic == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("static")));
          return;
        }

        processGroup.get().setCreatesStaticProcesses(isStatic);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "static",
          processGroup.get().createsStaticProcesses()
        ));
      }

      if (properties.containsKey("lobby")) {
        Boolean isLobby = Parsers.BOOLEAN.parse(properties.getProperty("lobby"));
        if (isLobby == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("lobby")));
          return;
        }

        processGroup.get().setLobbyGroup(isLobby);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "lobby",
          isLobby
        ));
      }

      if (properties.containsKey("max-players")) {
        Integer maxPlayers = Parsers.INT.parse(properties.getProperty("max-players"));
        if (maxPlayers == null || maxPlayers <= 0) {
          source.sendMessage(TranslationHolder.translate("command-integer-failed", 0, properties.getProperty("max-players")));
          return;
        }

        processGroup.get().getPlayerAccessConfiguration().setMaxPlayers(maxPlayers);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "max-players",
          processGroup.get().getPlayerAccessConfiguration().getMaxPlayers()
        ));
      }

      if (properties.containsKey("max-memory")) {
        String[] split = properties.getProperty("max-memory").split("/");
        if (split.length == 2) {
          Integer maxMemory = Parsers.INT.parse(split[1]);
          if (maxMemory == null || maxMemory <= 50) {
            source.sendMessage(TranslationHolder.translate("command-integer-failed", 50, split[1]));
            return;
          }

          Template template = processGroup.get().getTemplate(split[0]).orElse(null);
          if (template != null) {
            template.getRuntimeConfiguration().setMaximumJvmMemoryAllocation(maxMemory);
            source.sendMessage(TranslationHolder.translate(
              "command-group-edit",
              "max-memory",
              split[0] + "/" + maxMemory
            ));
          }
        }
      }

      if (properties.containsKey("min-process-count")) {
        Integer minProcessCount = Parsers.INT.parse(properties.getProperty("min-process-count"));
        if (minProcessCount == null || minProcessCount < 0) {
          source.sendMessage(TranslationHolder.translate("command-integer-failed", -1, properties.getProperty("min-process-count")));
          return;
        }

        processGroup.get().getStartupConfiguration().setAlwaysOnlineProcessAmount(minProcessCount);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "min-process-count",
          processGroup.get().getStartupConfiguration().getAlwaysOnlineProcessAmount()
        ));
      }

      if (properties.containsKey("max-process-count")) {
        Integer maxProcessCount = Parsers.INT.parse(properties.getProperty("max-process-count"));
        if (maxProcessCount == null || maxProcessCount <= -2) {
          source.sendMessage(TranslationHolder.translate("command-integer-failed", -2, properties.getProperty("max-process-count")));
          return;
        }

        processGroup.get().getStartupConfiguration().setMaximumProcessAmount(maxProcessCount);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "max-process-count",
          processGroup.get().getStartupConfiguration().getMaximumProcessAmount()
        ));
      }

      if (properties.containsKey("always-prepared-process-count")) {
        Integer alwaysPreparedCount = Parsers.INT.parse(properties.getProperty("always-prepared-process-count"));
        if (alwaysPreparedCount == null || alwaysPreparedCount < 0) {
          source.sendMessage(TranslationHolder.translate("command-integer-failed", -1, properties.getProperty("always-prepared-process-count")));
          return;
        }

        processGroup.get().getStartupConfiguration().setAlwaysPreparedProcessAmount(alwaysPreparedCount);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "always-prepared-process-count",
          processGroup.get().getStartupConfiguration().getAlwaysPreparedProcessAmount()
        ));
      }

      if (properties.containsKey("startup-pickers")) {
        List<String> startPickers = this.parseStrings(properties.getProperty("startup-pickers"));
        processGroup.get().getStartupConfiguration().setStartingNodes(startPickers);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "startup-pickers",
          String.join(", ", startPickers)
        ));
      }

      if (properties.containsKey("add-startup-pickers")) {
        List<String> startPickers = this.parseStrings(properties.getProperty("add-startup-pickers"));
        startPickers.addAll(processGroup.get().getStartupConfiguration().getStartingNodes());
        processGroup.get().getStartupConfiguration().setStartingNodes(startPickers);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "startup-pickers",
          String.join(", ", startPickers)
        ));
      }

      if (properties.containsKey("remove-startup-pickers")) {
        Collection<String> startPickers = processGroup.get().getStartupConfiguration().getStartingNodes();
        startPickers.removeAll(this.parseStrings(properties.getProperty("remove-startup-pickers")));
        processGroup.get().getStartupConfiguration().setStartingNodes(startPickers);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "startup-pickers",
          String.join(", ", startPickers)
        ));
      }

      if (properties.containsKey("clear-startup-pickers")) {
        Boolean clear = Parsers.BOOLEAN.parse(properties.getProperty("clear-startup-pickers"));
        if (clear == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("clear-startup-pickers")));
          return;
        }

        if (clear) {
          processGroup.get().getStartupConfiguration().setStartingNodes(new ArrayList<>());
          source.sendMessage(TranslationHolder.translate(
            "command-group-edit",
            "use-specific-start-picker",
            "false"
          ));
        }
      }

      if (properties.containsKey("templates")) {
        List<Template> newTemplates = this.parseTemplates(this.parseStrings(properties.getProperty("templates")), source, processGroup.get());
        if (!newTemplates.isEmpty()) {
          processGroup.get().setTemplates(newTemplates);
          source.sendMessage(TranslationHolder.translate(
            "command-group-edit",
            "templates",
            newTemplates.stream().map(Template::getName).collect(Collectors.joining(", "))
          ));
        }
      }

      if (properties.containsKey("add-templates")) {
        List<Template> newTemplates = this.parseTemplates(this.parseStrings(properties.getProperty("add-templates")), source, processGroup.get());
        if (!newTemplates.isEmpty()) {
          newTemplates.addAll(processGroup.get().getTemplates());
          processGroup.get().setTemplates(newTemplates);
          source.sendMessage(TranslationHolder.translate(
            "command-group-edit",
            "add-templates",
            newTemplates.stream().map(Template::getName).collect(Collectors.joining(", "))
          ));
        }
      }

      if (properties.containsKey("remove-templates")) {
        Collection<String> templatesToRemove = this.parseStrings(properties.getProperty("remove-templates"));
        Collection<Template> toRemove = MoreCollections.allOf(processGroup.get().getTemplates(), e -> templatesToRemove.contains(e.getName()));
        toRemove.forEach(processGroup.get()::removeTemplate);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "remove-templates",
          toRemove.stream().map(Template::getName).collect(Collectors.joining(", "))
        ));
      }

      if (properties.containsKey("clear-templates")) {
        Boolean clear = Parsers.BOOLEAN.parse(properties.getProperty("clear-templates"));
        if (clear == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("clear-templates")));
          return;
        }

        if (clear) {
          processGroup.get().removeAllTemplates();
          source.sendMessage(TranslationHolder.translate(
            "command-group-edit",
            "templates",
            "clear"
          ));
        }
      }

      ExecutorAPI.getInstance().getProcessGroupProvider().updateProcessGroup(processGroup.get());
      for (ProcessInformation process : ExecutorAPI.getInstance().getProcessProvider().getProcessesByProcessGroup(processGroup.get().getName())) {
        System.out.println(TranslationHolder.translate("command-group-edited-running-process", process.getName()));
      }
      return;
    }

    this.describeCommandToSender(source);
  }

  private void handleMainGroupRequest(CommandSender source, String[] strings, Properties properties) {
    Optional<MainGroup> mainGroup = ExecutorAPI.getInstance().getMainGroupProvider().getMainGroup(strings[1]);
    if (!mainGroup.isPresent()) {
      source.sendMessage(TranslationHolder.translate("command-group-main-group-not-exists", strings[1]));
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("info")) {
      this.describeMainGroupToSender(source, mainGroup.get());
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("delete")) {
      ExecutorAPI.getInstance().getMainGroupProvider().deleteMainGroup(mainGroup.get().getName());
      source.sendMessage(TranslationHolder.translate("command-group-main-delete", mainGroup.get().getName()));
      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("stop")) {
      for (String subGroup : mainGroup.get().getSubGroups()) {
        Collection<ProcessInformation> running = ExecutorAPI.getInstance()
          .getProcessProvider()
          .getProcessesByProcessGroup(subGroup)
          .stream()
          .filter(e -> !e.getCurrentState().equals(ProcessState.PREPARED))
          .collect(Collectors.toList());
        source.sendMessage(TranslationHolder.translate("command-group-stopping-all-not-prepared", subGroup));

        for (ProcessInformation information : running) {
          Optional<ProcessWrapper> wrapper = ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(information.getId().getUniqueId());
          wrapper.ifPresent(processWrapper -> processWrapper.setRuntimeStateAsync(ProcessState.STOPPED));
        }
      }

      return;
    }

    if (strings.length == 3 && strings[2].equalsIgnoreCase("kill")) {
      for (String subGroup : mainGroup.get().getSubGroups()) {
        Collection<ProcessInformation> running = ExecutorAPI.getInstance()
          .getProcessProvider()
          .getProcessesByProcessGroup(subGroup);
        for (ProcessInformation information : running) {
          Optional<ProcessWrapper> wrapper = ExecutorAPI.getInstance().getProcessProvider().getProcessByUniqueId(information.getId().getUniqueId());
          wrapper.ifPresent(processWrapper -> processWrapper.setRuntimeStateAsync(ProcessState.STOPPED));
        }
      }

      return;
    }

    if (strings.length >= 4 && strings[2].equalsIgnoreCase("edit")) {
      if (properties.containsKey("sub-groups")) {
        List<String> groups = this.parseStrings(properties.getProperty("sub-groups"));
        mainGroup.get().setSubGroups(groups);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "sub-groups",
          String.join(", ", groups)
        ));
      }

      if (properties.containsKey("add-sub-groups")) {
        List<String> groups = this.parseStrings(properties.getProperty("add-sub-groups"));
        MoreCollections.allOf(mainGroup.get().getSubGroups(), groups::contains).forEach(groups::remove);
        groups.forEach(mainGroup.get()::addSubGroup);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "sub-groups",
          String.join(", ", mainGroup.get().getSubGroups())
        ));
      }

      if (properties.containsKey("remove-sub-groups")) {
        List<String> groups = this.parseStrings(properties.getProperty("remove-sub-groups"));
        groups.forEach(mainGroup.get()::removeSubGroup);
        source.sendMessage(TranslationHolder.translate(
          "command-group-edit",
          "sub-groups-remove",
          String.join(", ", groups)
        ));
      }

      if (properties.containsKey("clear-sub-groups")) {
        Boolean clear = Parsers.BOOLEAN.parse(properties.getProperty("clear-sub-groups"));
        if (clear == null) {
          source.sendMessage(TranslationHolder.translate("command-required-boolean", properties.getProperty("clear-sub-groups")));
          return;
        }

        if (clear) {
          mainGroup.get().removeAllSubGroups();
          source.sendMessage(TranslationHolder.translate(
            "command-group-edit",
            "sub-groups",
            "clear"
          ));
        }
      }

      ExecutorAPI.getInstance().getMainGroupProvider().updateMainGroup(mainGroup.get());
      return;
    }

    this.describeCommandToSender(source);
  }

  private void describeProcessGroupToSender(CommandSender source, ProcessGroup group) {
    StringBuilder builder = new StringBuilder();

    builder.append(" > Name        - ").append(group.getName()).append("\n");
    builder.append(" > Lobby       - ").append(group.isLobbyGroup() ? "&ayes&r" : "&cno&r").append("\n");
    builder.append(" > Max-Players - ").append(group.getPlayerAccessConfiguration().getMaxPlayers()).append("\n");
    builder.append(" > Maintenance - ").append(group.getPlayerAccessConfiguration().isMaintenance() ? "&ayes&r" : "&cno&r").append("\n");
    builder.append(" > Min-Online  - ").append(group.getStartupConfiguration().getAlwaysOnlineProcessAmount()).append("\n");
    builder.append(" > Max-Online  - ").append(group.getStartupConfiguration().getMaximumProcessAmount()).append("\n");

    builder.append(" ").append("\n");
    builder.append(" > Templates (").append(group.getTemplates().size()).append(")");

    for (Template template : group.getTemplates()) {
      builder.append("\n");
      builder.append("  > Name       - ").append(template.getName()).append("\n");
      builder.append("  > Version    - ").append(template.getVersion().getName()).append("\n");
      builder.append("  > Backend    - ").append(template.getBackend()).append("\n");
      builder.append("  > Priority   - ").append(template.getPriority()).append("\n");
      builder.append("  > Max-Memory - ").append(template.getRuntimeConfiguration().getMaximumJvmMemoryAllocation()).append("MB\n");
      builder.append("  > Global     - ").append(template.isGlobal() ? "&ayes&r" : "&cno&r").append("\n");
      builder.append("  > Start-Port - ").append(template.getVersion().getDefaultStartPort()).append("\n");
      builder.append(" ");
    }

    source.sendMessages(builder.toString().split("\n"));
  }

  private void describeMainGroupToSender(CommandSender source, MainGroup mainGroup) {
    String prefix = " > Sub-Groups (" + mainGroup.getSubGroups().size() + ")";
    String s = " > Name " + mainGroup.getName() + "\n" + prefix + " - " + String.join(", ", mainGroup.getSubGroups()) + "\n";
    source.sendMessages(s.split("\n"));
  }

  private void listGroupsToSender(CommandSender source) {
    StringBuilder builder = new StringBuilder();

    final Collection<MainGroup> mainGroups = ExecutorAPI.getInstance().getMainGroupProvider().getMainGroups();
    final Collection<ProcessGroup> processGroups = ExecutorAPI.getInstance().getProcessGroupProvider().getProcessGroups();

    builder.append(" Main-Groups (").append(mainGroups.size()).append(")");
    for (MainGroup mainGroup : mainGroups) {
      builder.append("\n");
      builder.append("  > Name       - ").append(mainGroup.getName()).append("\n");
      builder.append("  > Sub-Groups - ").append(String.join(", ", mainGroup.getSubGroups())).append("\n");
      builder.append(" ");
    }

    builder.append(mainGroups.isEmpty() ? "\n" : "").append(" \n");

    builder.append(" Process-Groups (").append(processGroups.size()).append(")");
    for (ProcessGroup processGroup : processGroups) {
      builder.append("\n");
      builder.append(" > Name            - ").append(processGroup.getName()).append("\n");
      builder.append(" > Min-Processes   - ").append(processGroup.getStartupConfiguration().getAlwaysOnlineProcessAmount()).append("\n");
      builder.append(" > Max-Processes   - ").append(processGroup.getStartupConfiguration().getMaximumProcessAmount()).append("\n");
      builder.append(" > Startup-Pickers - ").append(processGroup.getStartupConfiguration().getStartingNodes().isEmpty()
        ? "all" : String.join(", ", processGroup.getStartupConfiguration().getStartingNodes())
      ).append("\n");
      builder.append(" ");
    }

    source.sendMessages(builder.toString().split("\n"));
  }

  private List<String> parseStrings(String s) {
    List<String> out = new ArrayList<>();
    if (s.contains(";")) {
      String[] split = s.split(";");
      for (String s1 : split) {
        if (out.contains(s1)) {
          continue;
        }

        out.add(s1);
      }
    } else {
      out.add(s);
    }

    return out;
  }

  private List<Template> parseTemplates(Collection<String> collection, CommandSender source, ProcessGroup processGroup) {
    List<Template> newTemplates = new ArrayList<>();
    for (String template : collection) {
      String[] templateConfig = template.split("/");
      if (templateConfig.length != 3) {
        source.sendMessage(TranslationHolder.translate("command-group-template-format-error", template));
        continue;
      }

      if (processGroup.getTemplate(templateConfig[0]).isPresent()) {
        source.sendMessage(TranslationHolder.translate("command-group-template-already-exists", templateConfig[0]));
        continue;
      }

      Optional<TemplateBackend> backend = TemplateBackendManager.get(templateConfig[1]);
      if (!backend.isPresent()) {
        source.sendMessage(TranslationHolder.translate("command-group-template-backend-invalid", templateConfig[1]));
        continue;
      }

      Version version = Versions.getByName(templateConfig[2]).orElse(null);
      if (version == null) {
        source.sendMessage(TranslationHolder.translate("command-group-template-version-not-found", templateConfig[2]));
        continue;
      }

      newTemplates.add(Template.builder(templateConfig[0], version).backend(backend.get().getName()).build());
    }

    return newTemplates;
  }
}