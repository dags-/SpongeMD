package me.dags.spongemd;

import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public final class MarkdownTemplate {

    private final MarkdownSpec spec;
    private final String template;

    MarkdownTemplate(MarkdownSpec spec, String template) {
        this.spec = spec;
        this.template = template;
    }

    public ArgBuilder with(String key, String template) {
        checkKey(key);
        return new ArgBuilder().with(key, spec.template(template));
    }

    public ArgBuilder with(String key, Object arg) {
        checkKey(key);
        return new ArgBuilder().with(key, arg);
    }

    public ArgBuilder with(Map<String, Object> args) {
        for (String key : args.keySet()) {
            checkKey(key);
        }
        return new ArgBuilder(args);
    }

    public Text render(Map<String, Object> args) {
        return renderTemplate(args);
    }

    @Override
    public String toString() {
        return template;
    }

    ArgBuilder withUnchecked(Map<?, ?> args) {
        return new ArgBuilder(args);
    }

    Text renderTemplate(Map<?, ?> args) {
        return new MDParser(spec, template, args).parse();
    }

    public class ArgBuilder {

        private final Map<String, Object> args = new HashMap<>();

        private ArgBuilder() {}

        ArgBuilder(Map<?, ?> args) {
            for (Map.Entry<?, ?> e : args.entrySet()) {
                this.args.put(e.getKey().toString(), e.getValue());
            }
        }

        ArgBuilder with(Object child) {
            this.args.put(".", child);
            return this;
        }

        ArgBuilder with(Map.Entry<?, ?> entry) {
            this.args.put(".key", entry.getKey());
            this.args.put(".value", entry.getValue());
            return this;
        }

        public ArgBuilder with(String key, Object arg) {
            checkKey(key);
            args.put(key, arg);
            return this;
        }

        public ArgBuilder with(String key, String template) {
            checkKey(key);
            args.put(key, MarkdownTemplate.this.spec.template(template));
            return this;
        }

        public Text render() {
            return MarkdownTemplate.this.renderTemplate(args);
        }

        Text renderTemplate() {
            return MarkdownTemplate.this.renderTemplate(args);
        }
    }

    private static void checkKey(String key) {
        if (key.equals(".") || key.equals(".key") || key.equals(".value")) {
            String error = String.format("Key: '%s' is a reserved key name!", key);
            throw new UnsupportedOperationException(error);
        }
    }
}
