package me.dags.spongemd;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MDParser {

    static final Map<?, ?> EMPTY = Collections.EMPTY_MAP;

    private final String input;
    private final MarkdownSpec spec;
    private final Map<?, ?> arguments;
    private int pos = -1;

    MDParser(MarkdownSpec spec, String input) {
        this.spec = spec;
        this.input = input;
        this.arguments = EMPTY;
    }

    MDParser(MarkdownSpec spec, String input, Map<?, ?> arguments) {
        this.spec = spec;
        this.input = input;
        this.arguments = arguments.isEmpty() ? EMPTY : arguments;
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
        MDBuilder builder = new MDBuilder(arguments);
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            char c = next();
            if (!escaped) {
                if (c == '`') {
                    builder.setQuoted(true);
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
                        builder.setEscaped(true);
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
        MDBuilder builder = new MDBuilder(arguments);
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            char peek = peek();

            if (!escaped) {
                if (peek == '`') {
                    quoted = !quoted;
                    next();
                    builder.setQuoted(quoted);
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
                        builder.setEscaped(true);
                        next();
                        continue;
                    }
                }
            }

            builder.append(next());
            escaped = false;
        }
        return MDParam.of(builder.string());
    }

    private Text.Builder nextContent() {
        MDBuilder builder = new MDBuilder(arguments);
        boolean quoted = false;
        boolean escaped = false;

        while (hasNext()) {
            if (!escaped) {
                char peek = peek();
                if (peek == '`') {
                    quoted = !quoted;
                    next();
                    builder.setQuoted(quoted);
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
                        builder.setEscaped(true);
                        next();
                        continue;
                    }
                }
            }

            builder.append(next());
            escaped = false;
        }

        return builder.plain();
    }
}
