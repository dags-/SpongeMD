package me.dags.text.syntax;

import me.dags.text.preset.MUPresets;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.translation.Translation;

import javax.annotation.Nullable;
import java.io.IOException;

public class Renderer {

    private final MUPresets presets;
    private final Property.Predicate predicate;

    public Renderer(MUPresets presets, Property.Predicate predicate) {
        this.presets = presets;
        this.predicate = predicate;
    }

    public Text.Builder render(Text text) throws IOException {
        return render(text, null);
    }

    private Text.Builder render(Text text, @Nullable Text.Builder parent) throws IOException {
        Text.Builder builder;

        if (text instanceof LiteralText) {
            builder = toLiteralBuilder((LiteralText) text);
        } else if (text instanceof TranslatableText) {
            builder = toTranslatableBuilder((TranslatableText) text);
        } else {
            builder = text.toBuilder().removeAll();
        }

        renderTextActions(builder);

        for (Text child : text.getChildren()) {
            render(child, builder);
        }

        if (parent != null) {
            parent.append(builder.build());
        }

        return builder;
    }

    private void inherit(Text text, Text.Builder builder) {
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

    private void renderTextActions(Text.Builder builder) {
        builder.getHoverAction().map(action -> {
            if (action instanceof HoverAction.ShowText) {
                try {
                    Text hover = ((HoverAction.ShowText) action).getResult();
                    return TextActions.showText(render(hover, null).build());
                } catch (IOException e) {
                    return action;
                }
            }
            return action;
        }).ifPresent(builder::onHover);
    }

    private Text.Builder toLiteralBuilder(LiteralText text) throws IOException {
        if (text.getContent().isEmpty()) {
            return text.toBuilder().removeAll();
        } else {
            Text.Builder builder = Parser.parse(text.getContent(), presets, predicate);
            inherit(text, builder);
            return builder;
        }
    }

    private Text.Builder toTranslatableBuilder(TranslatableText text) {
        Translation translation = text.getTranslation();
        Object[] arguments = text.getArguments().stream().map(o -> {
            if (o instanceof Text) {
                Text t = (Text) o;
                try {
                    return render(t, null);
                } catch (IOException e) {
                    return t;
                }
            }
            return o;
        }).toArray();
        Text.Builder builder = Text.builder(translation, arguments);
        inherit(text, builder);
        return builder;
    }

    public static Text.Builder render(Text text, MUPresets presets, Property.Predicate predicate) throws IOException {
        return new Renderer(presets, predicate).render(text);
    }
}
