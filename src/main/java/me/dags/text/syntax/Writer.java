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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.translation.Translation;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class Writer {

    private static final Pattern ESCAPE_CHARS = Pattern.compile("[\\[\\](),]");

    private final java.io.Writer writer;

    public Writer(java.io.Writer writer) {
        this.writer = writer;
    }

    public void write(Text text, boolean escape) throws IOException {
        if (isPlain(text)) {
            writePlain(text, escape);
        } else {
            writer.write('[');
            writePlain(text, escape);
            writer.write(']');
            writer.write('(');
            boolean comma;
            comma = writeClickAction(text, false);
            comma = writeHoverAction(text, comma);
            comma = writeColor(text, comma);
            comma = writeStyle(text, comma);
            writer.write(')');
        }
    }

    private void writePlain(Text text, boolean escape) throws IOException {
        if (text instanceof TranslatableText) {
            TranslatableText translatable = (TranslatableText) text;
            Translation translation = translatable.getTranslation();
            // well this sucks :/
            Object[] arguments = translatable.getArguments().stream()
                    .map(o -> o instanceof TextRepresentable ? ((Text) o).toPlain() : o)
                    .toArray();

            String plain = translation.get(arguments);
            writeString(plain, escape && ESCAPE_CHARS.matcher(plain).find());
        } else {
            String plain = text.toPlainSingle();
            writeString(plain, escape && ESCAPE_CHARS.matcher(plain).find());
        }
        for (Text child : text.getChildren()) {
            write(child, escape);
        }
    }

    private boolean writeClickAction(Text text, boolean comma) throws IOException {
        Optional<ClickAction<?>> optional = text.getClickAction();
        if (!optional.isPresent()) {
            return comma;
        }
        ClickAction<?> action = optional.get();
        if (action instanceof ClickAction.OpenUrl) {
            return writeProperty(action.getResult().toString(), comma);
        }
        if (action instanceof ClickAction.RunCommand) {
            return writeProperty(action.getResult().toString(), comma);
        }
        if (action instanceof ClickAction.SuggestCommand) {
            return writeProperty("/" + action.getResult().toString(), comma);
        }
        return comma;
    }

    private boolean writeHoverAction(Text text, boolean comma) throws IOException {
        Optional<HoverAction<?>> optional = text.getHoverAction();
        if (!optional.isPresent()) {
            return comma;
        }

        HoverAction<?> action = optional.get();
        if (action instanceof HoverAction.ShowText) {
            if (comma) {
                writer.write(',');
            }
            write(((HoverAction.ShowText) action).getResult(), true);
            return true;
        }

        return comma;
    }

    private boolean writeColor(Text text, boolean comma) throws IOException {
        if (text.getColor() != TextColors.NONE) {
            return writeProperty(text.getColor().getName().toLowerCase(), comma);
        }
        return false;
    }

    private boolean writeStyle(Text text, boolean comma) throws IOException {
        TextStyle style = text.getStyle();
        if (style.isBold().orElse(false)) {
            comma = writeProperty("bold", comma);
        }
        if (style.isItalic().orElse(false)) {
            comma = writeProperty("italic", comma);
        }
        if (style.isObfuscated().orElse(false)) {
            comma = writeProperty("obfuscated", comma);
        }
        if (style.hasUnderline().orElse(false)) {
            comma = writeProperty("underline", comma);
        }
        if (style.hasStrikethrough().orElse(false)) {
            comma = writeProperty("strikethrough", comma);
        }
        return comma;
    }

    private boolean writeProperty(String string, boolean comma) throws IOException {
        if (comma) {
            writer.write(',');
        }
        writeString(string, ESCAPE_CHARS.matcher(string).find());
        return true;
    }

    private void writeString(String string, boolean escape) throws IOException {
        if (escape) {
            writer.write('`');
            writer.write(string);
            writer.write('`');
        } else {
            writer.write(string);
        }
    }

    private static boolean isPlain(Text text) {
        if (text.getColor() != TextColors.NONE) {
            return false;
        }
        if (text.getStyle() != TextStyles.NONE && !text.getStyle().isEmpty()) {
            return false;
        }
        if (text.getClickAction().isPresent()) {
            return false;
        }
        if (text.getHoverAction().isPresent()) {
            return false;
        }
        return true;
    }
}
