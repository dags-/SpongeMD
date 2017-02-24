package me.dags.spongemd;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MDBuilder {

    private final LiteralText.Builder builder = (LiteralText.Builder) LiteralText.builder();
    private final StringBuilder stringBuilder = new StringBuilder(128);
    private final StringBuilder argBuilder = new StringBuilder(16);
    private final MarkdownSpec markdownSpec;
    private final Map<?, ?> arguments;
    private final boolean argsEnabled;

    private boolean buildingArg = false;
    private boolean escaped = false;
    private boolean quoted = false;
    private boolean empty = true;

    MDBuilder(MarkdownSpec markdownSpec, Map<?, ?> arguments) {
        this.arguments = arguments;
        this.markdownSpec = markdownSpec;
        this.argsEnabled = arguments != MDParser.EMPTY;
    }

    boolean isEmpty() {
        return empty && stringBuilder.length() == 0;
    }

    void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    void setQuoted(boolean quoted) {
        this.quoted = quoted;
    }

    MDBuilder append(char c) {
        if (escaped || quoted) {
            stringBuilder.append(c);
            escaped = false;
        } else if (buildingArg) {
            if (c == '}') {
                String argument = argBuilder.toString();
                argBuilder.setLength(0);
                buildingArg = false;
                appendArg(argument);
            } else {
                argBuilder.append(c);
            }
        } else if (argsEnabled && c == '{') {
            buildingArg = true;
        } else {
            stringBuilder.append(c);
            escaped = false;
        }

        return this;
    }

    MDBuilder append(Text other) {
        if (buildingArg) {
            stringBuilder.append(argBuilder);
            argBuilder.setLength(0);
            buildingArg = false;
        }
        appendString();
        builder.append(other);
        empty = false;
        return this;
    }

    private MDBuilder appendString() {
        if (stringBuilder.length() > 0) {
            if (empty) {
                builder.content(stringBuilder.toString());
            } else {
                builder.append(Text.of(stringBuilder.toString()));
            }
            stringBuilder.setLength(0);
            empty = false;
        }
        return this;
    }

    private void appendArg(String name) {
        int splitPoint = name.indexOf(":");

        if (splitPoint < 0) {
            Object value = arguments.get(name);
            appendObject(value);
        } else if (splitPoint == 0) {
            String templateKey = name.substring(splitPoint + 1);
            appendTemplate(templateKey);
        } else {
            String valueKey = name.substring(0, splitPoint);
            String templateKey = name.substring(splitPoint + 1);
            appendTemplate(templateKey, valueKey);
        }
    }

    private void appendTemplate(String templateKey) {
        Object template = arguments.get(templateKey);
        if (template != null) {
            if (MarkdownTemplate.class.isInstance(template)) {
                Text text = MarkdownTemplate.class.cast(template).renderTemplate(arguments);
                append(text);
            } else if (String.class.isInstance(template)) {
                Text text = markdownSpec.template((String) template).renderTemplate(arguments);
                append(text);
            }
        }
    }

    private void appendTemplate(String templateKey, String valueKey) {
        Object templ = arguments.get(templateKey);
        Object value = arguments.get(valueKey);

        if (value != null && templ != null) {
            MarkdownTemplate template = null;
            if (MarkdownTemplate.class.isInstance(templ)) {
                template = MarkdownTemplate.class.cast(templ);
            } else if (String.class.isInstance(templ)) {
                template = markdownSpec.template((String) templ);
            }

            if (template != null) {
                if (Map.class.isInstance(value)) {
                    appendMap(template, value);
                } else if (Iterable.class.isInstance(value)) {
                    appendIterable(template, value);
                } else if (value.getClass().isArray()) {
                    appendArray(template, value);
                } else {
                    Text text = template.applier().withUnchecked(arguments).with(value).render();
                    append(text);
                }
            }
        }
    }

    private void appendObject(Object value) {
        if (value != null) {
            if (Text.class.isInstance(value)) {
                Text text = Text.class.cast(value);
                append(text);
            } else if (MarkdownTemplate.class.isInstance(value)) {
                Text text = MarkdownTemplate.class.cast(value).renderTemplate(arguments);
                append(text);
            } else {
                stringBuilder.append(value);
            }
        }
    }

    private void appendIterable(MarkdownTemplate template, Object value) {
        MarkdownTemplate.Applier applier = template.applier().withUnchecked(arguments);
        Iterable iterable = Iterable.class.cast(value);
        for (Object child : iterable) {
            Text text = applier.with(child).render();
            append(text);
        }
    }

    private void appendArray(MarkdownTemplate template, Object value) {
        MarkdownTemplate.Applier applier = template.applier().withUnchecked(arguments);
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            Object child = Array.get(value, i);
            if (child != null) {
                Text text = applier.with(child).render();
                append(text);
            }
        }
    }

    private void appendMap(MarkdownTemplate template, Object value) {
        MarkdownTemplate.Applier applier = template.applier().withUnchecked(arguments);
        Map<?, ?> map = Map.class.cast(value);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Text text = applier.with(entry).render();
            append(text);
        }
    }

    String string() {
        return stringBuilder.toString();
    }

    Text.Builder plain() {
        return Text.builder(build().toPlain());
    }

    Text.Builder toBuilder() {
        return appendString().builder;
    }

    Text build() {
        return appendString().builder.build();
    }
}
