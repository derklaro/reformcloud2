package systems.reformcloud.reformcloud2.runner.setup;

import systems.reformcloud.reformcloud2.runner.util.RunnerUtils;

import javax.annotation.Nonnull;
import java.io.Console;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class RunnerExecutorSetup {

    private RunnerExecutorSetup() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return If the runner executor setup is required for the current process
     */
    public static boolean isSetupRequired() {
        return Files.notExists(RunnerUtils.EXECUTOR_PATH);
    }

    /**
     * Executes the runner executor setup
     */
    public static void executeSetup() {
        System.out.println("Please choose an executor: \"node\" (recommended), \"controller\", \"client\"");
        System.out.println("For more information check out the README on GitHub: " + RunnerUtils.README_URL);

        String executor = readFromConsoleOrFromSystemProperties(
                s -> RunnerUtils.AVAILABLE_EXECUTORS.contains(s.toLowerCase()),
                s -> {
                    System.out.println("The executor " + s + " is not available.");
                    System.out.println("Please choose one of these: " + String.join(", ", RunnerUtils.AVAILABLE_EXECUTORS));
                });
    }

    @Nonnull
    private static String readFromConsoleOrFromSystemProperties(@Nonnull Predicate<String> predicate,
                                                                @Nonnull Consumer<String> wrongInput) {
        String property = System.getProperty("reformcloud.executor.type");
        if (property != null && predicate.test(property)) {
            return property;
        }

        Console console = System.console();
        String s = console.readLine();
        while (s == null || s.trim().isEmpty() || !predicate.test(s)) {
            wrongInput.accept(s);
            s = console.readLine();
        }

        return s;
    }

}