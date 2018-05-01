package me.dags.textmu;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.transform.SimpleTextFormatter;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public final class MarkupTemplate {

    public static final TypeToken<MarkupTemplate> TYPE_TOKEN = TypeToken.of(MarkupTemplate.class);
    public static final TypeSerializer<MarkupTemplate> TYPE_SERIALIZER = new MarkupTemplate.Serializer();

    static {
        TypeSerializers.getDefaultSerializers().registerType(TYPE_TOKEN, TYPE_SERIALIZER);
    }

    private final String raw;
    private final MarkupSpec spec;
    private final List<Component> list;

    public MarkupTemplate(MarkupSpec spec, String raw, List<Component> list) {
        this.raw = raw;
        this.spec = spec;
        this.list = list;
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

    public class Applier extends SimpleTextTemplateApplier {

        private final Map<String, Object> arguments = new HashMap<>();
        private final StringBuilder sb = new StringBuilder();

        Applier() {

        }

        @Override
        public ImmutableMap<String, TextElement> getParameters() {
            ImmutableMap.Builder<String, TextElement> builder = ImmutableMap.builder();
            arguments.entrySet().stream()
                    .filter(entry -> TextElement.class.isInstance(entry.getValue()))
                    .forEach(entry -> builder.put(entry.getKey(), TextElement.class.cast(entry.getValue())));
            return builder.build();
        }

        @Override
        public void setParameter(String key, TextElement value) {
            if (value instanceof TextRepresentable) {
                String md = spec.writeUnescaped((TextRepresentable) value);
                value = spec.render(md);
            }

            arguments.put(key, value);
        }

        @Override
        public TextTemplate getTemplate() {
            return TextTemplate.EMPTY;
        }

        @Override
        public void setTemplate(TextTemplate template) {

        }

        @Override
        public Text toText() {
            return render();
        }

        public Text render() {
            return spec.render(apply());
        }

        public String apply() {
            for (Component c : list) {
                c.apply(this);
            }
            return sb.toString();
        }

        public Applier with(String key, Object value) {
            arguments.put(key, value);
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

        private void drop(String key) {
            arguments.remove(key);
        }

        private void render(Object o) {
            sb.append(o);
        }

        private Object get(String key) {
            return arguments.get(key);
        }
    }

    interface Component {

        void apply(Applier applier);
    }

    static class Plain implements Component {

        private final String text;

        Plain(String text) {this.text = text;}

        @Override
        public void apply(Applier applier) {
            applier.render(text);
        }
    }

    static class Arg implements Component {

        private final String key;

        Arg(String key) {this.key = key;}

        @Override
        public void apply(Applier applier) {
            Object value = applier.get(key);
            if (value != null) {
                applier.render(value);
            }
        }
    }

    static class Templ implements Component {

        private final String key;
        private final String separator;
        private final List<Component> components;

        Templ(String key, String separator, List<Component> components) {
            this.key = key;
            this.separator = separator;
            this.components = components;
        }

        @Override
        public void apply(Applier applier) {
            if (key.isEmpty()) {
                for (Component comp : components) {
                    comp.apply(applier);
                }
            } else {
                Object o = applier.get(key);
                if (o == null) {
                    return;
                }

                if (o.getClass() == MarkupTemplate.class) {
                    applyTemplate(applier, o);
                } else if (o instanceof Iterable) {
                    applyIterable(applier, o);
                } else if (o instanceof Map) {
                    applyMap(applier, o);
                } else if (o.getClass().isArray()) {
                    applyArray(applier, o);
                } else {
                    apply(applier, components);
                }
            }
        }

        private void applyTemplate(Applier applier, Object o) {
            MarkupTemplate template = (MarkupTemplate) o;
            String text = template.with(applier.arguments).apply();
            applier.render(text);
        }

        private void applyIterable(Applier applier, Object o) {
            Iterator iterator = ((Iterable) o).iterator();
            while (iterator.hasNext()) {
                applier.with(".", iterator.next());
                apply(applier, components);
                if (iterator.hasNext()) {
                    applier.render(separator);
                }
            }
            applier.drop(".");
        }

        private void applyArray(Applier applier, Object o) {
            for (int i = 0, len = Array.getLength(o); i < len; i++) {
                Object e = Array.get(o, i);
                applier.with(".", e);
                apply(applier, components);
                if (i + 1 < len) {
                    applier.render(separator);
                }
            }
            applier.drop(".");
        }

        private void applyMap(Applier applier, Object o) {
            Iterator iterator = ((Map) o).entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                applier.with(".key", entry.getKey());
                applier.with(".value", entry.getValue());
                apply(applier, components);
                if (iterator.hasNext()) {
                    applier.render(separator);
                }
            }
            applier.drop(".key");
            applier.drop(".value");
        }

        private void apply(Applier applier, List<Component> list) {
            for (Component comp : list) {
                comp.apply(applier);
            }
        }
    }

    private static class Serializer implements TypeSerializer<MarkupTemplate> {

        @Override
        public MarkupTemplate deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            MarkupSpec spec = node.getValue(MarkupSpec.TYPE_TOKEN);
            String template = node.getNode("components").getString("");
            return spec.template(template);
        }

        @Override
        public void serialize(TypeToken<?> type, MarkupTemplate template, ConfigurationNode node) throws ObjectMappingException {
            node.setValue(MarkupSpec.TYPE_TOKEN, template.spec);
            node.getNode("components").setValue(template.raw);
        }
    }
}
