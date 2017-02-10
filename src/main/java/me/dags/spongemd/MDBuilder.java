package me.dags.spongemd;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
class MDBuilder {

    private final StringBuilder stringBuilder = new StringBuilder();
    private final LiteralText.Builder builder = (LiteralText.Builder) LiteralText.builder();
    private boolean empty = true;

    boolean isEmpty() {
        return empty;
    }

    MDBuilder append(char c) {
        stringBuilder.append(c);
        return this;
    }

    MDBuilder append(Text other) {
        appendString().builder.append(other);
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
