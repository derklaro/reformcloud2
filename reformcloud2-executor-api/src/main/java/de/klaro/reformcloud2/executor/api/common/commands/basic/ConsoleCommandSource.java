package de.klaro.reformcloud2.executor.api.common.commands.basic;

import de.klaro.reformcloud2.executor.api.common.commands.manager.CommandManager;
import de.klaro.reformcloud2.executor.api.common.commands.permission.Permission;
import de.klaro.reformcloud2.executor.api.common.commands.permission.PermissionCheck;
import de.klaro.reformcloud2.executor.api.common.commands.permission.PermissionHolder;
import de.klaro.reformcloud2.executor.api.common.commands.permission.PermissionResult;
import de.klaro.reformcloud2.executor.api.common.commands.source.CommandSource;
import de.klaro.reformcloud2.executor.api.common.logger.coloured.Colours;

import java.util.Collection;
import java.util.Collections;

public final class ConsoleCommandSource implements CommandSource {

    private static final PermissionCheck CONSOLE_COMMAND_CHECK = new ConsoleCommandCheck();

    private static final Collection<Permission> PERMISSIONS = Collections.singletonList(new Permission() {
        @Override
        public String permission() {
            return "*";
        }

        @Override
        public PermissionResult defaultResult() {
            return PermissionResult.ALLOWED;
        }
    });

    public ConsoleCommandSource(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    private final CommandManager commandManager;

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        if (hasPermission(permission.permission())) {
            return true;
        }

        return permission.defaultResult().isAllowed();
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        if (isPermissionSet(permission.permission())) {
            return true;
        }

        return permission.defaultResult().isAllowed();
    }

    @Override
    public Collection<Permission> getEffectivePermissions() {
        return PERMISSIONS;
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public PermissionCheck check() {
        return CONSOLE_COMMAND_CHECK;
    }

    @Override
    public String getName() {
        return "ReformCloudConsole";
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void sendRawMessage(String message) {
        System.out.println(Colours.stripColor(message));
    }

    @Override
    public void sendMessages(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendRawMessages(String[] messages) {
        for (String message : messages) {
            sendRawMessage(message);
        }
    }

    @Override
    public CommandManager commandManager() {
        return commandManager;
    }

    private static class ConsoleCommandCheck implements PermissionCheck {

        @Override
        public PermissionResult checkPermission(PermissionHolder permissionHolder, Permission permission) {
            return PermissionResult.ALLOWED;
        }

        @Override
        public PermissionResult checkPermission(PermissionHolder permissionHolder, String permission) {
            return PermissionResult.ALLOWED;
        }
    }
}