package me.dags.spongemd;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
class MDParser {

    private final String input;
    private final MarkdownSpec spec;
    private int pos = -1;

    MDParser(String input, MarkdownSpec spec) {
        this.input = input;
        this.spec = spec;
    }

    private boolean hasNext() {
        return pos + 1 < input.length();
    }

    private char next() {
        if (hasNext()) {
            return input.charAt(++pos);
        }
        throw new TextParseException("Unexpected end of String: " + input);
    }

    private char peek() {
        return hasNext() ? input.charAt(pos + 1) : input.charAt(pos);
    }

    private boolean skip(int count) {
        if (pos + count < input.length()) {
            pos += count;
            return true;
        }
        pos = input.length() - 1;
        return false;
    }

    Text parse() {
        MDBuilder builder = new MDBuilder();
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            char c = next();
            if (!escaped) {
                if (c == '`') {
                    quoted = !quoted;
                    continue;
                }

                if (!quoted) {
                    if (c == '[') {
                        Text text = parseStatement();
                        // avoid unnecessary nesting of texts
                        if (!hasNext() && builder.isEmpty()) {
                            return text;
                        }
                        builder.append(text);
                        continue;
                    }
                    if (escaped = c == '\\') {
                        continue;
                    }
                }
            }
            builder.append(c);
            escaped = false;
        }
        return builder.build();
    }

    private Text parseStatement() {
        int start = pos;
        int end = input.length();
        List<MDParam> params = parseParams();
        if (hasNext()) {
            if (next() == '(') {
                Text.Builder content = nextContent();
                for (MDParam param : params) {
                    if (param.test(spec)) {
                        param.apply(content);
                    }
                }
                return content.build();
            }
            end = pos;
        }
        return Text.of(input.substring(start, end));
    }

    private List<MDParam> parseParams() {
        List<MDParam> params = new ArrayList<>();
        while (hasNext()) {
            MDParam param = nextParam();
            params.add(param);
            if (peek() == ']') {
                next();
                break;
            }
        }
        return params;
    }

    private MDParam nextParam() {
        StringBuilder builder = new StringBuilder();
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            char peek = peek();

            if (!escaped) {
                if (peek == '`') {
                    quoted = !quoted;
                    next();
                    continue;
                }

                if (!quoted) {
                    if (peek == ']') {
                        break;
                    }
                    if (peek == ',') {
                        next();
                        break;
                    }
                    if (peek == '[' && skip(1)) {
                        return MDParam.of(parseStatement());
                    }
                    if (escaped = peek == '\\') {
                        next();
                        continue;
                    }
                }
            }

            escaped = false;
            builder.append(next());
        }
        return MDParam.of(builder.toString());
    }

    private Text.Builder nextContent() {
        MDBuilder builder = new MDBuilder();
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            if (!escaped) {
                char peek = peek();
                if (peek == '`') {
                    quoted = !quoted;
                    next();
                    continue;
                }

                if (!quoted) {
                    if (peek == ')') {
                        next();
                        return builder.toBuilder();
                    }
                    if (peek == '[') {
                        next();
                        builder.append(parseStatement());
                        continue;
                    }
                    if (escaped = peek == '\\') {
                        next();
                        continue;
                    }
                }
            }

            escaped = false;
            builder.append(next());
        }

        return builder.plain();
    }
}
