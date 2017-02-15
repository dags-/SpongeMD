package me.dags.spongemd;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class MDWriter {

    private final MarkdownSpec spec;
    private final boolean escaping;

    MDWriter(MarkdownSpec spec, boolean escaping) {
        this.spec = spec;
        this.escaping = escaping;
    }

    String write(Text textIn) {
        return writeChild(textIn);
    }

    private String writeChild(Text child) {
        if (child == Text.NEW_LINE) {
            return "\\n";
        }

        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        comma = writeColor(sb, child, comma);
        comma = writeStyle(sb, child, comma);
        comma = writeHover(sb, child, comma);
        comma = writeClick(sb, child, comma);
        writeShiftClick(sb, child, comma);
        writeContent(sb, child, sb.length() > 0);

        return sb.toString();
    }

    private boolean writeColor(StringBuilder sb, Text child, boolean comma) {
        if (child.getColor() != TextColors.NONE && spec.allow(child.getColor())) {
            sb.append(comma ? "," : "").append(child.getColor().getId().toLowerCase());
            comma = true;
        }
        return comma;
    }

    private boolean writeStyle(StringBuilder sb, Text child, boolean comma) {
        boolean bold = child.getStyle().isBold().orElse(false);
        if (bold && spec.allow(TextStyles.BOLD)) {
            sb.append(comma ? "," : "").append("bold");
            comma = true;
        }
        boolean italic = child.getStyle().isItalic().orElse(false);
        if (italic && spec.allow(TextStyles.ITALIC)) {
            sb.append(comma ? "," : "").append("italic");
            comma = true;
        }
        boolean underline = child.getStyle().hasUnderline().orElse(false);
        if (underline && spec.allow(TextStyles.UNDERLINE)) {
            sb.append(comma ? "," : "").append("underline");
            comma = true;
        }
        boolean strikethrough = child.getStyle().hasStrikethrough().orElse(false);
        if (strikethrough && spec.allow(TextStyles.STRIKETHROUGH)) {
            sb.append(comma ? "," : "").append("strikethrough");
            comma = true;
        }
        boolean obfuscated = child.getStyle().isObfuscated().orElse(false);
        if (obfuscated && spec.allow(TextStyles.OBFUSCATED)) {
            sb.append(comma ? "," : "").append("obfuscated");
            comma = true;
        }
        boolean reset = child.getStyle() == TextStyles.RESET;
        if (reset && spec.allow(TextStyles.RESET)) {
            sb.append(comma ? "," : "").append("reset");
            comma = true;
        }
        return comma;
    }

    private boolean writeHover(StringBuilder sb, Text child, boolean comma) {
        Optional<HoverAction<?>> hover = child.getHoverAction();
        if (hover.isPresent() && hover.get() instanceof HoverAction.ShowText) {
            HoverAction.ShowText action = (HoverAction.ShowText) hover.get();
            sb.append(comma ? "," : "");
            sb.append(writeChild(action.getResult()));
            comma = true;
        }
        return comma;
    }

    private boolean writeClick(StringBuilder sb, Text child, boolean comma) {
        Optional<ClickAction<?>> click = child.getClickAction();
        if (click.isPresent()) {
            if (click.get() instanceof ClickAction.OpenUrl) {
                ClickAction.OpenUrl action = (ClickAction.OpenUrl) click.get();
                sb.append(comma ? "," : "");
                appendSafeArg(sb, action.getResult().toString());
                comma = true;
            } else if (click.get() instanceof ClickAction.RunCommand) {
                ClickAction.RunCommand action = (ClickAction.RunCommand) click.get();
                sb.append(comma ? "," : "");
                appendSafeArg(sb, "//" + action.getResult());
                comma = true;
            } else if (click.get() instanceof ClickAction.SuggestCommand) {
                ClickAction.SuggestCommand action = (ClickAction.SuggestCommand) click.get();
                sb.append(comma ? "," : "");
                appendSafeArg(sb, "/" + action.getResult());
                comma = true;
            }
        }
        return comma;
    }

    private boolean writeShiftClick(StringBuilder sb, Text child, boolean comma) {
        Optional<ShiftClickAction<?>> click = child.getShiftClickAction();
        if (click.isPresent()) {
            if (click.get() instanceof ShiftClickAction.InsertText) {
                ShiftClickAction.InsertText action = (ShiftClickAction.InsertText) click.get();
                sb.append(comma ? "," : "");
                appendSafeArg(sb, action.getResult());
                comma = true;
            }
        }
        return comma;
    }

    private void writeContent(StringBuilder sb, Text child, boolean withArgs) {
        if (withArgs) {
            sb.insert(0,  "[").append("](");
        }
        appendSafeContent(sb, child.toPlainSingle());
        child.getChildren().forEach(c -> sb.append(writeChild(c)));
        if (withArgs) {
            sb.append(")");
        }
    }

    private void appendSafeArg(StringBuilder builder, String text) {
        if (escaping) {
            text = text.replace("`", "\\`");
            String escape = text.matches(".*[\\\\].*") ? "`" : "";
            builder.append(escape).append(text).append(escape);
        } else {
            builder.append(text);
        }
    }

    private void appendSafeContent(StringBuilder builder, String text) {
        if (escaping) {
            String escape = text.matches(".*[\\[)].*") ? "`" : "";
            builder.append(escape).append(text.replace("`", "\\`")).append(escape);
        } else {
            builder.append(text);
        }
    }
}
