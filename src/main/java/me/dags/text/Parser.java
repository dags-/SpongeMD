/*
 * MIT License
 *
 * Copyright (c) 2019 dags
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
 *
 */

package me.dags.text;

import me.dags.template.CharReader;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.Reader;

class Parser {

    private static Text parse(CharReader reader, StringBuilder raw, Property.Predicate predicate) throws IOException {
        Context context = new Context(new Builder(), raw);
        while (reader.next()) {
            char end = readText(reader, context);
            if (end == '[') {
                int start = context.raw.length() - 1;
                Builder child = parseMarkdown(reader, context.raw, predicate);
                if (child.isEmpty()) {
                    context.builder.text(context.raw.substring(start));
                } else {
                    context.builder.child(child);
                }
            } else if (end != CharReader.EOF) {
                context.builder.text(end);
            }
        }
        return context.root.build().build();
    }

    private static Builder parseMarkdown(CharReader reader, StringBuilder raw, Property.Predicate predicate) throws IOException {
        Context context = new Context(new Builder(), raw);
        while (reader.next()) {
            char end = readText(reader, context);
            if (end == ']') {
                if (!reader.next()) {
                    return Builder.EMPTY;
                }
                char next = reader.character();
                raw.append(next);
                if (next != '(') {
                    return Builder.EMPTY;
                }
                return parseProperties(reader, predicate, context);
            }
            if (end == '[') {
                int start = context.raw.length() - 1;
                Builder child = parseMarkdown(reader, raw, predicate);
                if (child.isEmpty()) {
                    context.builder.text(context.raw.substring(start));
                } else {
                    context.builder.child(child);
                }
            }
        }
        return Builder.EMPTY;
    }

    private static Builder parseProperties(CharReader reader, Property.Predicate predicate, Context context) throws IOException {
        StringBuilder buffer = new StringBuilder();
        while (reader.next()) {
            char end = readProperty(reader, context, buffer);
            if (end == ')') {
                Property property = Property.parse(buffer.toString().trim(), predicate);
                context.root.property(property);
                return context.root;
            }
            if (end == ',') {
                Property property = Property.parse(buffer.toString().trim(), predicate);
                context.root.property(property);
                buffer.setLength(0);
            }
        }
        return Builder.EMPTY;
    }

    private static char readText(CharReader reader, Context context) throws IOException {
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();
            context.raw.append(c);

            if (charEscaped) {
                context.accept(c);
                charEscaped = false;
                continue;
            }
            if (stringEscaped) {
                if (c == '`') {
                    stringEscaped = false;
                } else {
                    context.accept(c);
                }
                continue;
            }
            if (c == '[' || c == ']') {
                return c;
            }
            if (c == '\\') {
                charEscaped = true;
                continue;
            }
            if (c == '`') {
                stringEscaped = true;
                continue;
            }

            context.accept(c);
        }
        return CharReader.EOF;
    }

    private static char readProperty(CharReader reader, Context context, StringBuilder buffer) throws IOException {
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();
            context.raw.append(c);

            if (charEscaped) {
                buffer.append(c);
                charEscaped = false;
                continue;
            }
            if (stringEscaped) {
                if (c == '`') {
                    stringEscaped = false;
                } else {
                    buffer.append(c);
                }
                continue;
            }
            if (c == ',' || c == ')') {
                return c;
            }
            if (c == '\\') {
                charEscaped = true;
                continue;
            }
            if (c == '`') {
                stringEscaped = true;
                continue;
            }

            buffer.append(c);
        }
        return CharReader.EOF;
    }

    private static class Context {

        private final Builder root;
        private final StringBuilder raw;
        private Builder builder;

        private Context(Builder root, StringBuilder raw) {
            this.root = root;
            this.builder = root;
            this.raw = raw;
        }

        private void accept(char c) {
            builder = builder.text(c);
        }
    }

    static Text parse(String string, Property.Predicate predicate) throws IOException {
        return parse(new CharReader(string), new StringBuilder(string.length()), predicate);
    }

    static Text parse(Reader reader, Property.Predicate predicate) throws IOException {
        return parse(new CharReader(reader), new StringBuilder(), predicate);
    }
}
