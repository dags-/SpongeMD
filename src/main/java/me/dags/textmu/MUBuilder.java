package me.dags.textmu;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MUBuilder {

    private final LiteralText.Builder builder = (LiteralText.Builder) LiteralText.builder();
    private final StringBuilder stringBuilder = new StringBuilder(128);
    private final StringBuilder argBuilder = new StringBuilder(16);
    private final MarkupSpec markupSpec;
    private final Map<?, ?> arguments;
    private final boolean argsEnabled;

    private boolean buildingArg = false;
    private boolean escaped = false;
    private boolean quoted = false;
    private boolean empty = true;

    MUBuilder(MarkupSpec markupSpec, Map<?, ?> arguments) {
        this.arguments = arguments;
        this.markupSpec = markupSpec;
        this.argsEnabled = arguments != MUParser.EMPTY;
    }

    boolean isEmpty() {
        return empty && stringBuilder.length() == 0 && argBuilder.length() == 0;
    }

    boolean isRaw() {
        return empty;
    }

    boolean isQuoted() {
        return quoted;
    }

    boolean isEscaped() {
        return escaped;
    }

    void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    void setQuoted(boolean quoted) {
        this.quoted = quoted;
    }

    MUBuilder append(char c) {
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

    MUBuilder append(Text other) {
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

    private MUBuilder appendString() {
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
        int joinPoint = name.indexOf(":", 1 + splitPoint);

        if (splitPoint < 0) {
            Object value = arguments.get(name);
            appendObject(value);
        } else if (splitPoint == 0) {
            String templateKey = name.substring(splitPoint + 1);
            appendTemplate(templateKey);
        } else if (joinPoint > 0) {
            String valueKey = name.substring(0, splitPoint);
            String templateKey = name.substring(splitPoint + 1, joinPoint);
            String separator = name.substring(joinPoint + 1);
            appendTemplate(templateKey, valueKey, separator);
        } else {
            String valueKey = name.substring(0, splitPoint);
            String templateKey = name.substring(splitPoint + 1);
            appendTemplate(templateKey, valueKey, "");
        }
    }

    private void appendTemplate(String templateKey) {
        Object template = arguments.get(templateKey);
        if (template != null) {
            if (MarkupTemplate.class.isInstance(template)) {
                Text text = MarkupTemplate.class.cast(template).renderTemplate(arguments);
                append(text);
            } else if (String.class.isInstance(template)) {
                Text text = markupSpec.template((String) template).renderTemplate(arguments);
                append(text);
            }
        }
    }

    private void appendTemplate(String templateKey, String valueKey, String separator) {
        Object templ = arguments.get(templateKey);
        Object value = arguments.get(valueKey);

        if (value != null && templ != null) {
            MarkupTemplate template = null;
            if (MarkupTemplate.class.isInstance(templ)) {
                template = MarkupTemplate.class.cast(templ);
            } else if (String.class.isInstance(templ)) {
                template = markupSpec.template((String) templ);
            }

            if (template != null) {
                if (Map.class.isInstance(value)) {
                    appendMap(template, value, separator);
                } else if (Iterable.class.isInstance(value)) {
                    appendIterable(template, value, separator);
                } else if (value.getClass().isArray()) {
                    appendArray(template, value, separator);
                } else {
                    Text text = template.applier().withUnchecked(arguments).with(value).render();
                    append(text);
                }
            }
        }
    }

    private void appendObject(Object value) {
        if (value != null) {
            if (MarkupTemplate.class.isInstance(value)) {
                Text text = MarkupTemplate.class.cast(value).renderTemplate(arguments);
                append(text);
            } else if (TextRepresentable.class.isInstance(value)) {
                TextRepresentable text = TextRepresentable.class.cast(value);
                append(text.toText());
            } else {
                stringBuilder.append(value);
            }
        }
    }

    private void appendIterable(MarkupTemplate template, Object value, String separator) {
        MarkupTemplate.Applier applier = template.applier().withUnchecked(arguments);
        Iterator iterator = Iterable.class.cast(value).iterator();
        while (iterator.hasNext()) {
            Object child = iterator.next();
            Text one = applier.with(child).render();
            Text text = !separator.isEmpty() && iterator.hasNext() ? Text.of(one, separator) : one;
            append(text);
        }
    }

    private void appendArray(MarkupTemplate template, Object value, String separator) {
        MarkupTemplate.Applier applier = template.applier().withUnchecked(arguments);
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            Object child = Array.get(value, i);
            if (child != null) {
                Text one = applier.with(child).render();
                Text text = !separator.isEmpty() && i + 1 < length ? Text.of(one, separator) : one;
                append(text);
            }
        }
    }

    private void appendMap(MarkupTemplate template, Object value, String separator) {
        MarkupTemplate.Applier applier = template.applier().withUnchecked(arguments);
        Map<?, ?> map = Map.class.cast(value);
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            Text one = applier.with(entry).render();
            Text text = !separator.isEmpty() && iterator.hasNext() ? Text.of(one, separator) : one;
            append(text);
        }
    }

    String string() {
        return stringBuilder.toString();
    }

    String serialized() {
        return markupSpec.write(build());
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
