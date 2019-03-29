package me.dags.text;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;

import java.util.HashMap;
import java.util.Map;

public class MUPerms {

    public static final MUPerms DEFAULTS = MUPerms.of("text");
    public static final Property.Predicate ANY = property -> true;
    public static final Property.Predicate NONE = property -> false;

    public static final String URL = "url";
    public static final String HOVER = "hover";
    public static final String COMMAND = "command";
    public static final String SUGGESTION = "suggestion";

    private final Map<Object, String> nodes;

    private MUPerms(Builder builder) {
        this.nodes = ImmutableMap.copyOf(builder.nodes);
    }

    public Property.Predicate wrap(Subject subject) {
        return property -> {
            String node = nodes.get(property);
            if (node == null) {
                return false;
            }
            if (node.isEmpty()) {
                return true;
            }
            return subject.hasPermission(node);
        };
    }

    public static Builder builder(String base) {
        if (base.endsWith(".")) {
            base = base.substring(0, base.length() - 2);
        }
        return new Builder(base);
    }

    public static MUPerms of(String baseNode) {
        Builder builder = builder(baseNode);
        Property.COLORS.values().forEach(builder::color);
        Property.STYLES.values().forEach(builder::style);
        builder.action(URL);
        builder.action(HOVER);
        builder.action(COMMAND);
        builder.action(SUGGESTION);
        return builder.build();
    }

    public static class Builder {

        private final Map<Object, String> nodes = new HashMap<>();

        private final String base;

        public Builder(String base) {
            this.base = base;
        }

        public Builder color(TextColor color) {
            nodes.put(color, base + ".color." + color.getName().toLowerCase());
            return this;
        }

        public Builder style(TextStyle style) {
            if (style instanceof TextStyle.Base) {
                nodes.put(style, base + ".style." + ((TextStyle.Base) style).getName().toLowerCase());
            }
            return this;
        }

        public Builder action(String name) {
            nodes.put(name, base + ".action." + name);
            return this;
        }

        public MUPerms build() {
            return new MUPerms(this);
        }
    }
}
