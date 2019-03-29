package me.dags.text;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

interface Property {

    Property NONE = b -> {};
    Map<String, TextColor> COLORS = ImmutableMap.copyOf(textColors());
    Map<String, TextStyle> STYLES = ImmutableMap.copyOf(textStyles());

    void apply(Text.Builder builder);
    
    interface Predicate {
        
        boolean test(Object property);
    }

    static boolean isURL(String in) {
        return in.matches("^((ht|f)tp(s?)://|www\\.)?([\\da-z-]+)(\\.([\\da-z-]+))*((\\.[a-z]{2,6})+|:[0-9]+)(/[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)*?$");
    }

    static Property parse(String in, Predicate predicate) throws IOException {
        if (in.isEmpty()) {
            return Property.NONE;
        }
        if (in.startsWith("//")) {
            return parseSuggestion(in.substring(1), predicate);
        }
        if (in.startsWith("/")) {
            return parseCommand(in, predicate);
        }
        if (isURL(in)) {
            return parseURL(in, predicate);
        }
        Optional<TextColor> color = match(in, COLORS);
        if (color.isPresent()) {
            return parseColor(color.get(), predicate);
        }
        Optional<TextStyle> style = match(in, STYLES);
        if (style.isPresent()) {
            return parseStyle(style.get(), predicate);
        }
        return parseHover(in, predicate);
    }

    static Property parseColor(TextColor color, Predicate predicate) {
        if (predicate.test(color)) {
            return b -> b.color(color);
        }
        return NONE;
    }

    static Property parseCommand(String in, Predicate predicate) {
        if (predicate.test(MUPerms.COMMAND)) {
            return b -> b.onClick(TextActions.runCommand(in));
        }
        return NONE;
    }

    static Property parseHover(String in, Predicate predicate) throws IOException {
        if (predicate.test(MUPerms.HOVER)) {
            Text text = Parser.parse(in, predicate);
            return b -> b.onHover(TextActions.showText(text));
        }
        return NONE;
    }

    static Property parseStyle(TextStyle style, Predicate predicate) {
        if (predicate.test(style)) {
            return b -> b.style(style);
        }
        return NONE;
    }

    static Property parseSuggestion(String in, Predicate predicate) {
        if (predicate.test(MUPerms.SUGGESTION)) {
            return b -> b.onClick(TextActions.suggestCommand(in));
        }
        return NONE;
    }

    static Property parseURL(String in, Predicate predicate) throws IOException {
        if (predicate.test(MUPerms.URL)) {
            URL url = new URL(in.trim());
            return b -> b.onClick(TextActions.openUrl(url));
        }
        return NONE;
    }

    static <T> Optional<T> match(String in, Map<String, T> map) {
        T t = map.get(in);
        if (t != null) {
            return Optional.of(t);
        }
        String match = "";
        for (String key : map.keySet()) {
            if (key.startsWith(in)) {
                if (match.isEmpty()) {
                    match = key;
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.ofNullable(map.get(match));
    }
    
    static Map<String, TextColor> textColors() {
        return Sponge.getRegistry().getAllOf(TextColor.class).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c));
    }

    static Map<String, TextStyle> textStyles() {
        return Sponge.getRegistry().getAllOf(TextStyle.Base.class).stream()
                .collect(Collectors.toMap(c -> c.getName().toLowerCase(), s -> s));
    }
}
