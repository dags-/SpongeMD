package me.dags.text;

import me.dags.template.CharReader;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.Reader;

class Parser {

    static Text parse(String string, Property.Predicate predicate) throws IOException {
        return parse(new CharReader(string), new StringBuilder(string.length()), predicate);
    }

    static Text parse(Reader reader, Property.Predicate predicate) throws IOException {
        return parse(new CharReader(reader), new StringBuilder(), predicate);
    }

    private static Text parse(CharReader reader, StringBuilder raw, Property.Predicate predicate) throws IOException {
        Context context = new Context(new Builder(), raw);
        while (reader.next()) {
            char end = readText(reader, context);
            if (end == '[') {
                int start = context.raw.length();
                Builder child = parseMarkdown(reader, context.raw, predicate);
                if (child.isEmpty()) {
                    context.builder.text(context.raw.substring(start));
                } else {
                    context.builder.child(child);
                }
            }
        }
        return context.root.build().build();
    }

    private static Builder parseMarkdown(CharReader reader, StringBuilder raw, Property.Predicate predicate) throws IOException {
        Context context = new Context(new Builder(), raw);
        while (reader.next()) {
            char end = readText(reader, context);
            if (end == ']') {
                break;
            }
            if (end == '[') {
                int start = context.raw.length();
                Builder child = parseMarkdown(reader, raw, predicate);
                if (child.isEmpty()) {
                    context.builder.text(context.raw.substring(start));
                } else {
                    context.builder.child(child);
                }
            }
        }
        if (!reader.next() || reader.character() != '(') {
            return Builder.EMPTY;
        }
        return parseProperties(reader, predicate, context);
    }

    private static Builder parseProperties(CharReader reader, Property.Predicate predicate, Context context) throws IOException {
        StringBuilder fmt = new StringBuilder();
        while (reader.next()) {
            char end = readProperty(reader, context);
            if (end == ')') {
                Property property = Property.parse(fmt.toString().trim(), predicate);
                context.root.property(property);
                return context.builder;
            }
            if (end == ',') {
                Property property = Property.parse(fmt.toString().trim(), predicate);
                context.root.property(property);
                fmt.setLength(0);
            }
        }
        return Builder.EMPTY;
    }

    private static char readText(CharReader reader, Context context) throws IOException {
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();
            context.raw.append(c);

            if (charEscaped) {
                context.accept(c);
                charEscaped = false;
                continue;
            }
            if (stringEscaped) {
                if (c == '`') {
                    stringEscaped = false;
                } else {
                    context.accept(c);
                }
                continue;
            }
            if (c == '[' || c == ']') {
                return c;
            }
            if (c == '\\') {
                charEscaped = true;
                continue;
            }
            if (c == '`') {
                stringEscaped = true;
                continue;
            }

            context.accept(c);
        }
        return CharReader.EOF;
    }

    private static char readProperty(CharReader reader, Context context) throws IOException {
        boolean charEscaped = false;
        boolean stringEscaped = false;
        while (reader.next()) {
            char c = reader.character();
            context.raw.append(c);

            if (charEscaped) {
                context.accept(c);
                charEscaped = false;
                continue;
            }
            if (stringEscaped) {
                if (c == '`') {
                    stringEscaped = false;
                } else {
                    context.accept(c);
                }
                continue;
            }
            if (c == ',' || c == ')') {
                return c;
            }
            if (c == '\\') {
                charEscaped = true;
                continue;
            }
            if (c == '`') {
                stringEscaped = true;
                continue;
            }

            context.accept(c);
        }
        return CharReader.EOF;
    }

    private static class Context {

        private final Builder root;
        private final StringBuilder raw;
        private Builder builder;

        private Context(Builder root, StringBuilder raw) {
            this.root = root;
            this.builder = root;
            this.raw = raw;
        }

        private void accept(char c) {
            builder = builder.text(c);
        }
    }
}
