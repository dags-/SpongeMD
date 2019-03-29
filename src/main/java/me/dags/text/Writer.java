package me.dags.text;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.util.Optional;

class Writer {

    private final java.io.Writer writer;

    public Writer(java.io.Writer writer) {
        this.writer = writer;
    }

    public void write(Text text) throws IOException {
        if (isPlain(text)) {
            writer.write(text.toPlainSingle());
            for (Text child : text.getChildren()) {
                write(child);
            }
        } else {
            writer.write('[');
            writer.write(text.toPlainSingle());
            for (Text child : text.getChildren()) {
                write(child);
            }
            writer.write(']');
            writer.write('(');
            boolean comma;
            comma = writeClickAction(false, text);
            comma = writeHoverAction(comma, text);
            comma = writeColor(comma, text);
            comma = writeStyle(comma, text);
            writer.write(')');
        }
    }

    private boolean writeClickAction(boolean comma, Text text) throws IOException {
        Optional<ClickAction<?>> optional = text.getClickAction();
        if (!optional.isPresent()) {
            return false;
        }
        ClickAction<?> action = optional.get();
        if (action instanceof ClickAction.OpenUrl) {
            return writeString(comma, action.getResult().toString());
        }
        if (action instanceof ClickAction.RunCommand) {
            return writeString(comma, "/" + action.getResult());
        }
        if (action instanceof ClickAction.SuggestCommand) {
            return writeString(comma, "//" + action.getResult());
        }
        return false;
    }

    private boolean writeHoverAction(boolean comma, Text text) throws IOException {
        Optional<HoverAction<?>> optional = text.getHoverAction();
        if (!optional.isPresent()) {
            return false;
        }

        HoverAction<?> action = optional.get();
        if (action instanceof HoverAction.ShowText) {
            if (comma) {
                writer.write(',');
            }
            writer.write('`');
            write(((HoverAction.ShowText) action).getResult());
            writer.write('`');
            return true;
        }

        return false;
    }

    private boolean writeColor(boolean comma, Text text) throws IOException {
        if (text.getColor() != TextColors.NONE) {
            return writeString(comma, text.getColor().getName().toLowerCase());
        }
        return false;
    }

    private boolean writeStyle(boolean comma, Text text) throws IOException {
        TextStyle style = text.getStyle();
        if (style.isBold().orElse(false)) {
            comma = writeString(comma, "bold");
        }
        if (style.isItalic().orElse(false)) {
            comma = writeString(comma, "italic");
        }
        if (style.isObfuscated().orElse(false)) {
            comma = writeString(comma, "obfuscated");
        }
        if (style.hasUnderline().orElse(false)) {
            comma = writeString(comma, "underline");
        }
        if (style.hasStrikethrough().orElse(false)) {
            comma = writeString(comma, "strikethrough");
        }
        return comma;
    }

    private boolean writeString(boolean comma, String string) throws IOException {
        if (comma) {
            writer.write(',');
        }
        writer.write(string);
        return true;
    }

    private static boolean isPlain(Text text) {
        if (text.getColor() != TextColors.NONE) {
            return false;
        }
        if (text.getStyle() != TextStyles.NONE) {
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
