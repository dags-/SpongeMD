package me.dags.text;

import com.google.common.collect.ImmutableMap;
import me.dags.template.Template;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.transform.SimpleTextFormatter;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class MUTemplate {

    public static final MUTemplate EMPTY = new MUTemplate(MUSpec.create(), Template.EMPTY, MUPerms.NONE);

    private final MUSpec spec;
    private final Template template;
    private final Property.Predicate predicate;

    public MUTemplate(MUSpec spec, Template template, Property.Predicate predicate) {
        this.spec = spec;
        this.template = template;
        this.predicate = predicate;
    }

    public Applier with(String key, Object value) {
        return applier().with(key, value);
    }

    public Applier with(Map map) {
        return applier().with(map);
    }

    public Applier with(SimpleTextFormatter partition) {
        return applier().with(partition);
    }

    public Applier applier() {
        return new Applier();
    }

    @Override
    public String toString() {
        return template.toString();
    }

    public class Applier extends SimpleTextTemplateApplier {

        private final Map<String, Object> arguments = new HashMap<>();
        private Property.Predicate predicate = MUTemplate.this.predicate;

        private Applier() {
        }

        @Override
        public ImmutableMap<String, TextElement> getParameters() {
            return ImmutableMap.copyOf(arguments.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> spec.render(e.getValue().toString())
            )));
        }

        @Override
        public void setParameter(String key, TextElement element) {
            checkNotNull(key, "key");
            Text.Builder builder = Text.builder();
            element.applyTo(builder);
            String value = spec.write(builder.build());
            arguments.put(key, value);
        }

        @Override
        public org.spongepowered.api.text.TextTemplate getTemplate() {
            return org.spongepowered.api.text.TextTemplate.EMPTY;
        }

        @Override
        public void setTemplate(org.spongepowered.api.text.TextTemplate template) {

        }

        @Override
        public Text toText() {
            return render();
        }

        public Applier subject(Subject subject) {
            predicate = spec.getPermissions().wrap(subject);
            return this;
        }

        public Applier with(String key, Object value) {
            if (value instanceof TextElement) {
                setParameter(key, (TextElement) value);
            } else {
                arguments.put(key, value);
            }
            return this;
        }

        public Applier with(Map map) {
            for (Object o : map.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                with(entry.getKey().toString(), entry.getValue());
            }
            return this;
        }

        public Applier with(SimpleTextFormatter partition) {
            for (SimpleTextTemplateApplier applier : partition.getAll()) {
                for (Map.Entry<String, TextElement> e : applier.getParameters().entrySet()) {
                    setParameter(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        public Text render() {
            try {
                StringWriter writer = new StringWriter();
                MUTemplate.this.template.apply(arguments, writer);
                return spec.render(predicate, writer.toString());
            } catch (Throwable t) {
                t.printStackTrace();
                return Text.EMPTY;
            }
        }
    }
}
