package me.dags.textmu;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MUParser {

    static final Map<?, ?> EMPTY = Collections.EMPTY_MAP;

    private final String input;
    private final MarkupSpec spec;
    private final Map<?, ?> arguments;
    private int pos = -1;

    MUParser(MarkupSpec spec, String input) {
        this.spec = spec;
        this.input = input;
        this.arguments = EMPTY;
    }

    MUParser(MarkupSpec spec, String input, Map<?, ?> arguments) {
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
        MUBuilder builder = new MUBuilder(spec, arguments);
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
        List<MUParam> params = parseParams();
        if (hasNext()) {
            if (next() == '(') {
                Text.Builder content = nextContent();
                for (MUParam param : params) {
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

    private List<MUParam> parseParams() {
        List<MUParam> params = new ArrayList<>();
        while (hasNext()) {
            MUParam param = nextParam();
            params.add(param);
            if (peek() == ']') {
                next();
                break;
            }
        }
        return params;
    }

    private MUParam nextParam() {
        MUBuilder builder = new MUBuilder(spec, arguments);

        while (hasNext()) {
            char peek = peek();
            if (!builder.isEscaped()) {
                if (peek == '`') {
                    next();
                    builder.setQuoted(!builder.isQuoted());
                    continue;
                }

                if (!builder.isQuoted()) {
                    if (peek == ']') {
                        break;
                    }
                    if (peek == ',') {
                        next();
                        break;
                    }
                    if (peek == '[' && skip(1)) {
                        return MUParam.of(parseStatement());
                    }
                    if (peek == '\\') {
                        builder.setEscaped(true);
                        next();
                        continue;
                    }
                }
            }
            builder.append(next());
        }

        if (builder.isRaw()) {
            return MUParam.of(builder.string());
        }

        // parameter is complex (may be from a template) so serialize and parse as a param
        return new MUParser(spec, builder.serialized(), arguments).nextParam();
    }

    private Text.Builder nextContent() {
        MUBuilder builder = new MUBuilder(spec, arguments);
        while (hasNext()) {
            if (!builder.isEscaped()) {
                char peek = peek();
                if (peek == '`') {
                    next();
                    builder.setQuoted(!builder.isQuoted());
                    continue;
                }

                if (!builder.isQuoted()) {
                    if (peek == ')') {
                        next();
                        return builder.toBuilder();
                    }
                    if (peek == '[') {
                        next();
                        builder.append(parseStatement());
                        continue;
                    }
                    if (peek == '\\') {
                        builder.setEscaped(true);
                        next();
                        continue;
                    }
                }
            }
            builder.append(next());
        }

        return builder.plain();
    }
}
