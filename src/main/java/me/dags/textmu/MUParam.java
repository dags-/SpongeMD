package me.dags.textmu;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author dags <dags@dags.me>
 */
abstract class MUParam {

    abstract boolean test(MarkupSpec spec);

    abstract void apply(Text.Builder builder);

    private static final String URL = "((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)";
    private static final Pattern URL_PATTERN = Pattern.compile(URL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    static final MUParam EMPTY = new MUParam() {
        @Override
        boolean test(MarkupSpec spec) {
            return false;
        }

        @Override
        void apply(Text.Builder builder) {}
    };

    private static Map<String, TextColor> colors = Collections.emptyMap();
    private static Map<String, TextStyle.Base> styles = Collections.emptyMap();

    static Map<String, TextColor> colors() {
        if (MUParam.colors.isEmpty()) {
            Map<String, TextColor> colors = new HashMap<>();
            Sponge.getRegistry().getAllOf(TextColor.class).stream()
                    .filter(color -> color != TextColors.RESET && color != TextColors.NONE)
                    .forEach(color -> colors.put(color.getId().toLowerCase(), color));

            colors.put("a", TextColors.GREEN);
            colors.put("b", TextColors.AQUA);
            colors.put("c", TextColors.AQUA);
            colors.put("d", TextColors.RED);
            colors.put("e", TextColors.LIGHT_PURPLE);
            colors.put("f", TextColors.YELLOW);
            colors.put("0", TextColors.BLACK);
            colors.put("1", TextColors.DARK_BLUE);
            colors.put("2", TextColors.DARK_GREEN);
            colors.put("3", TextColors.DARK_AQUA);
            colors.put("4", TextColors.DARK_RED);
            colors.put("5", TextColors.DARK_PURPLE);
            colors.put("6", TextColors.GOLD);
            colors.put("7", TextColors.GRAY);
            colors.put("8", TextColors.DARK_GRAY);
            colors.put("9", TextColors.BLUE);
            MUParam.colors = colors;
        }
        return MUParam.colors;
    }

    static Map<String, TextStyle.Base> styles() {
        if (MUParam.styles.isEmpty()) {
            Map<String, TextStyle.Base> styles = new HashMap<>();
            Sponge.getRegistry().getAllOf(TextStyle.Base.class).stream()
                    .filter(style -> style != TextStyles.NONE && style != TextStyles.RESET)
                    .forEach(style -> styles.put(style.getId().toLowerCase(), style));

            styles.put("k", TextStyles.OBFUSCATED);
            styles.put("l", TextStyles.BOLD);
            styles.put("m", TextStyles.STRIKETHROUGH);
            styles.put("n", TextStyles.UNDERLINE);
            styles.put("o", TextStyles.ITALIC);
            MUParam.styles = styles;
        }
        return MUParam.styles;
    }

    static MUParam of(Text text) {
        return new HoverText(text);
    }

    static MUParam of(String in) {
        String id = in.trim().toLowerCase();

        TextColor color = colors().get(id);
        if (color != null) {
            return new ColorParam(color);
        }

        TextStyle style = styles().get(id);
        if (style != null) {
            return new StyleParam(style);
        }

        if (id.equals("reset") || id.equals("r")) {
            return new ResetParam();
        }

        if (in.startsWith("/")) {
            return new RunCommand(in);
        }

        if (in.startsWith("./")) {
            return new SuggestCommand(in.substring(1));
        }

        if (URL_PATTERN.matcher(id).find()) {
            try {
                return new OpenUrl(new URL(in.matches("^https?://.*$") ? in : "http://" + in));
            } catch (MalformedURLException e) {
                return MUParam.EMPTY;
            }
        }

        return new InsertText(in);
    }

    private static class ResetParam extends MUParam {

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowReset();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.color(TextColors.RESET);
            builder.style(TextStyles.RESET);
        }
    }

    private static class ColorParam extends MUParam {

        private final TextColor color;

        ColorParam(TextColor color) {
            this.color = color;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allow(color);
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.color(color);
        }
    }

    private static class StyleParam extends MUParam {

        private final TextStyle style;

        StyleParam(TextStyle style) {
            this.style = style;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allow(style);
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.style(style);
        }
    }

    private static class RunCommand extends MUParam {

        private final String command;

        RunCommand(String command) {
            this.command = command;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowRunCommand();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.onClick(TextActions.runCommand(command));
        }
    }

    private static class SuggestCommand extends MUParam {

        private final String command;

        SuggestCommand(String command) {
            this.command = command;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowSuggestCommand();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.onClick(TextActions.suggestCommand(command));
        }
    }

    private static class OpenUrl extends MUParam {

        private final URL url;

        OpenUrl(URL url) {
            this.url = url;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowUrl();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.onClick(TextActions.openUrl(url));
        }
    }

    private static class HoverText extends MUParam {

        private final Text text;

        HoverText(Text text) {
            this.text = text;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowShowText();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.onHover(TextActions.showText(text));
        }
    }

    private static class InsertText extends MUParam {

        private final String text;

        InsertText(String text) {
            this.text = text;
        }

        @Override
        public boolean test(MarkupSpec spec) {
            return spec.allowInsertText();
        }

        @Override
        public void apply(Text.Builder builder) {
            builder.onShiftClick(TextActions.insertText(text));
        }
    }
}
