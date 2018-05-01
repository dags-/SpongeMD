package me.dags.textmu;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
class MUBuilder {

    private final LiteralText.Builder builder = (LiteralText.Builder) LiteralText.builder();
    private final StringBuilder stringBuilder = new StringBuilder(128);
    private final MarkupSpec markupSpec;

    private boolean escaped = false;
    private boolean quoted = false;
    private boolean empty = true;

    MUBuilder(MarkupSpec markupSpec) {
        this.markupSpec = markupSpec;
    }

    boolean isEmpty() {
        return empty && stringBuilder.length() == 0;
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
        } else {
            stringBuilder.append(c);
            escaped = false;
        }

        return this;
    }

    MUBuilder append(Text other) {
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
