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
package systems.reformcloud.reformcloud2.node.logger;

import systems.reformcloud.reformcloud2.shared.StringUtil;
import systems.reformcloud.reformcloud2.executor.api.utility.list.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingOutputStream extends ByteArrayOutputStream {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final Collection<String> EXCLUDED_LINE_STARTS = new ArrayList<>(); // No need to be thread save

    static {
        String excluded = System.getProperty("systems.reformcloud.logging-excluded-line-starts", "SLF4J:");
        EXCLUDED_LINE_STARTS.addAll(Arrays.asList(excluded.split(";")));
    }

    private final Logger parent;
    private final Level level;

    public LoggingOutputStream(Logger parent, Level level) {
        this.parent = parent;
        this.level = level;
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            super.flush();
            String content = this.toString(StandardCharsets.UTF_8.name());
            super.reset();

            if (content.isEmpty()
                || content.equals(LINE_SEPARATOR)
                || Streams.hasMatch(LoggingOutputStream.EXCLUDED_LINE_STARTS, content::startsWith)) {
                return;
            }

            if (content.endsWith(LINE_SEPARATOR)) {
                content = StringUtil.replaceLastEmpty(content, LINE_SEPARATOR);
            }

            this.parent.log(this.level, content);
        }
    }
}
