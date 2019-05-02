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

package me.dags.text.syntax;

import me.dags.template.CharReader;
import me.dags.text.preset.MUPresets;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.io.IOException;

public class Parser {

    private final MUPresets presets;
    private final Reader reader;
    private final Property.Predicate predicate;

    public Parser(CharReader reader, MUPresets presets, Property.Predicate predicate) {
        this.predicate = predicate;
        this.reader = new Reader(reader);
        this.presets = presets;
    }

    public Text.Builder parse() throws IOException {
        Context context = new Context(new Builder());
        while (reader.next()) {
            char end = readText(context);
            if (end == '[') {
                Builder child = parseMarkdown();
                context.append(child, '[');
            } else if (end != CharReader.EOF) {
                context.root.text(end);
            }
        }
        return context.root.build(presets, predicate);
    }

    private Builder parseMarkdown() throws IOException {
        Context context = new Context(new Builder());
        while (reader.next()) {
            char end = readText(context);
            if (end == ']') {
                if (!reader.next()) {
                    return context.root.fail(end);
                }
                char next = reader.peek();
                if (next != '(') {
                    return context.root.fail(end);
                }
                reader.character();
                return parseProperties(context);
            }
            if (end == '[') {
                Builder child = parseMarkdown();
                context.append(child, '[');
            }
        }
        return context.root.fail(CharReader.EOF);
    }

    private Builder parseProperties(Context context) throws IOException {
        StringBuilder raw = new StringBuilder("](");
        StringBuilder buffer = new StringBuilder();
        while (reader.next()) {
            char end = readProperty(buffer, raw);
            if (end == CharReader.EOF) {
                break;
            }
            if (end == ')') {
                Property property = Property.parse(buffer.toString().trim(), presets, predicate);
                context.root.property(property);
                return context.root;
            }
            if (end == ',') {
                Property property = Property.parse(buffer.toString().trim(), presets, predicate);
                context.root.property(property);
                buffer.setLength(0);
            }
        }
        return context.root.text(raw.toString()).fail(CharReader.EOF);
    }

    private char readText(Context context) throws IOException {
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();

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

    private char readProperty(StringBuilder buffer, StringBuilder raw) throws IOException {
        int depth = 0;
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();
            raw.append(c);

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
            if (c == '\\') {
                charEscaped = true;
                continue;
            }
            if (c == '`') {
                stringEscaped = true;
                continue;
            }
            if (c == ',') {
                if (depth < 1) {
                    return c;
                }
                buffer.append(c);
                continue;
            }
            if (c == ')') {
                if (depth < 1) {
                    return c;
                }
                depth--;
                buffer.append(c);
                continue;
            }
            if (c == '(') {
                depth++;
            }
            buffer.append(c);
        }
        return CharReader.EOF;
    }

    public static Text.Builder parse(String input, MUPresets presets, Property.Predicate predicate) throws IOException {
        CharReader reader = new CharReader(input);
        return new Parser(reader, presets, predicate).parse();
    }

    public static Text.Builder parse(Text text, @Nullable Text.Builder parent, MUPresets presets, Property.Predicate predicate) throws IOException {
        Text.Builder builder;

        if (text instanceof LiteralText) {
            String content = ((LiteralText) text).getContent();
            if (content.isEmpty()) {
                builder = text.toBuilder().removeAll();
            } else {
                builder = parse(content, presets, predicate);
                if (builder.getColor() == TextColors.NONE) {
                    builder.color(text.getColor());
                }
                if (builder.getStyle().isEmpty()) {
                    builder.style(text.getStyle());
                }
                if (!builder.getHoverAction().isPresent()) {
                    text.getHoverAction().ifPresent(builder::onHover);
                }
                if (!builder.getClickAction().isPresent()) {
                    text.getClickAction().ifPresent(builder::onClick);
                }
                if (!builder.getShiftClickAction().isPresent()) {
                    text.getShiftClickAction().ifPresent(builder::onShiftClick);
                }
            }
        } else {
            builder = text.toBuilder().removeAll();
        }

        for (Text child : text.getChildren()) {
            parse(child, builder, presets, predicate);
        }

        if (parent != null) {
            parent.append(builder.build());
        }

        return builder;
    }
}
