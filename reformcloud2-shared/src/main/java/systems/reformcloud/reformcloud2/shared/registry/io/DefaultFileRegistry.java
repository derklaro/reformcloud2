/*
 * This file is part of reformcloud2, licensed under the MIT License (MIT).
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
package systems.reformcloud.reformcloud2.shared.registry.io;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import systems.reformcloud.reformcloud2.executor.api.configuration.JsonConfiguration;
import systems.reformcloud.reformcloud2.shared.io.IOUtils;
import systems.reformcloud.reformcloud2.executor.api.registry.io.FileRegistry;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultFileRegistry implements FileRegistry {

    private final Path operatingFolder;

    public DefaultFileRegistry(String operatingFolder) {
        this.operatingFolder = Paths.get(operatingFolder);
        IOUtils.createDirectory(this.operatingFolder);
    }

    @NotNull
    @Override
    public <T> T createKey(@NotNull String keyName, @NotNull T t) {
        Path filePath = this.operatingFolder.resolve(keyName + ".json");
        if (Files.exists(filePath)) {
            return t;
        }

        new JsonConfiguration().add("key", t).write(filePath);
        return t;
    }

    @NotNull
    @Override
    public <T> Optional<T> getKey(@NotNull String keyName) {
        Path filePath = this.operatingFolder.resolve(keyName + ".json");
        if (Files.notExists(filePath)) {
            return Optional.empty();
        }

        return Optional.ofNullable(JsonConfiguration.read(filePath).get("key", new TypeToken<>() {
        }));
    }

    @Override
    public void deleteKey(@NotNull String key) {
        IOUtils.deleteFile(this.operatingFolder.resolve(key + ".json"));
    }

    @Override
    public <T> void updateKey(@NotNull String key, @NotNull T newValue) {
        Path filePath = this.operatingFolder.resolve(key + ".json");
        if (Files.notExists(filePath)) {
            return;
        }

        new JsonConfiguration().add("key", newValue).write(filePath);
    }

    @NotNull
    @Override
    public <T> Collection<T> readKeys(@NotNull Function<JsonConfiguration, T> function,
                                      @NotNull Consumer<Path> failureHandler) {
        Collection<T> result = new CopyOnWriteArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.operatingFolder, path -> path.toString().endsWith(".json"))) {
            for (Path path : stream) {
                T t = function.apply(JsonConfiguration.read(path));
                if (t == null) {
                    failureHandler.accept(path);
                    continue;
                }

                result.add(t);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return result;
    }
}
