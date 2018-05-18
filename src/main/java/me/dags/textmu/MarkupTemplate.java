package me.dags.textmu;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.transform.SimpleTextFormatter;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public final class MarkupTemplate {

    public static final TypeToken<MarkupTemplate> TYPE_TOKEN = TypeToken.of(MarkupTemplate.class);
    public static final TypeSerializer<MarkupTemplate> TYPE_SERIALIZER = new Serializer();

    static {
        TypeSerializers.getDefaultSerializers().registerType(TYPE_TOKEN, TYPE_SERIALIZER);
    }

    private final MarkupSpec spec;
    private final String template;

    MarkupTemplate(MarkupSpec spec, String template) {
        this.spec = spec;
        this.template = template;
    }

    /**
     * Create a new TemplateApplier for this MarkupTemplate
     *
     * @return The newly created Applier
     */
    public Applier applier() {
        return new Applier(this);
    }

    /**
     * Create a new TemplateApplier with the value reference '{.}'
     *
     * @param value The value assigned to '{.}'
     * @return The newly created Applier
     */
    public Applier with(Object value) {
        return applier().with(value);
    }

    /**
     * Create a new TemplateApplier with the key/value pair referenced '{.key}' & '{.value}'
     *
     * @param entry The key/value pair assigned to '{.key}' & '{.value}'
     * @return The newly created Applier
     */
    public Applier with(Map.Entry<?, ?> entry) {
        return applier().with(entry);
    }

    /**
     * Create a new TemplateApplier with the given key/value pair
     *
     * @param key The named variable used in the template
     * @param value The value that this variable should be assigned
     * @return The newly created Applier
     */
    public Applier with(String key, Object value) {
        return applier().with(key, value);
    }

    /**
     * Create a new TemplateApplier with the given map of key/value pairs
     *
     * @param map The named variable used in the template
     * @return The newly created Applier
     */
    public Applier with(Map<String, Object> map) {
        return applier().with(map);
    }

    /**
     * Create a new TemplateApplier with the Permission Options from the given Subject and Contents
     *
     * @param subject The Subject whose permission options will be used
     * @param contexts The Contexts used to determine which Options are applicable
     * @return The new created Applier
     */
    public Applier withOptions(Subject subject, Set<Context> contexts) {
        return applier().withOptions(subject, contexts);
    }

    /**
     * Create a new TemplateApplier with the NamedCauses from the given Cause object
     *
     * @param cause The Cause containing the NamedCauses to be used
     * @return The current Applied
     */
    public Applier withCause(Cause cause) {
        return applier().withCause(cause);
    }

    @Override
    public String toString() {
        return template;
    }

    Text renderTemplate(Map<?, ?> args) {
        return new MUParser(spec, template, args).parse();
    }

    private static void validKey(String key) {
        Preconditions.checkNotNull(key);

        if (key.isEmpty() || !key.matches("^[a-zA-Z0-9_]*$")) {
            String error = String.format("Key: '%s' is a reserved key name!", key);
            throw new UnsupportedOperationException(error);
        }
    }

    public class Applier extends SimpleTextTemplateApplier {

        private final MarkupTemplate template;
        private final Map<String, Object> arguments = new HashMap<>();

        Applier(MarkupTemplate template) {
            this.template = template;
        }

        /**
         * @return Any TextElement objects stored in this Applier. Note that this Applier may hold other object types
         *          that will not be returned in the resulting map
         */
        @Override
        public ImmutableMap<String, TextElement> getParameters() {
            ImmutableMap.Builder<String, TextElement> map = ImmutableMap.builder();
            arguments.entrySet().stream()
                    .filter(entry -> TextElement.class.isInstance(entry.getValue()))
                    .forEach(entry -> map.put(entry.getKey(), TextElement.class.cast(entry.getValue())));
            return map.build();
        }

        /**
         * As base method but applies Markup rendering to any TextRepresentables
         * @param key The argument name
         * @param value The value for the argument
         */
        @Override
        public void setParameter(String key, TextElement value) {
            validKey(key);

            if (value instanceof TextRepresentable) {
                String md = spec.writeUnescaped((TextRepresentable) value);
                value = spec.render(md);
            }

            this.arguments.put(key, value);
        }

        /**
         * @return Always returns an empty template
         */
        @Override
        public TextTemplate getTemplate() {
            return TextTemplate.EMPTY;
        }

        /**
         * @param template Does not store a TextTemplate, value is always ignored
         */
        @Override
        public void setTemplate(TextTemplate template) {}

        @Override
        public Text toText() {
            return render();
        }

        public Text render() {
            return template.renderTemplate(arguments);
        }

        public Applier with(Object object) {
            arguments.put(".", object);
            return this;
        }

        public Applier with(Map.Entry<?, ?> entry) {
            arguments.put(".key", entry.getKey());
            arguments.put(".value", entry.getValue());
            return this;
        }

        /**
         * Provide a key/value pair to be used by the template
         *
         * @param key The named variable used in the template
         * @param value The value that this variable should be assigned
         * @return The current Applier
         */
        public Applier with(String key, Object value) {
            validKey(key);
            this.arguments.put(key, value);
            return this;
        }

        /**
         * Provide a map of key/value pairs to be used by the template
         *
         * @param map A map of key/values
         * @return The current Applier
         */
        public Applier with(Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                validKey(entry.getKey().toString());
                arguments.put(entry.getKey().toString(), entry.getValue());
            }
            return this;
        }

        /**
         * Provide a Subject whose Permission Options should be used by the template
         *
         * @param subject The Subject whose Permission Options will be used
         * @param contexts The Contexts used to determine which Options are applicable
         * @return The current Applier
         */
        public Applier withOptions(Subject subject, Set<Context> contexts) {
            SubjectData data = subject.getSubjectData();
            return with(data.getOptions(contexts));
        }

        /**
         * Provide a Cause whose NamedCauses should be used by the template
         *
         * @param cause The Cause containing the NamedCauses to be used
         * @return The current Applied
         */
        public Applier withCause(Cause cause) {
            return with(cause.getContext().asMap());
        }

        /**
         * Inherit arguments from a TextFormatter partition
         *
         * @param partition The partition to inherit arguments from
         * @return The current Applier
         */
        public Applier inherit(SimpleTextFormatter partition) {
            for (SimpleTextTemplateApplier applier : partition.getAll()) {
                for (Map.Entry<String, TextElement> e : applier.getParameters().entrySet()) {
                    setParameter(e.getKey(), e.getValue());
                }
            }
            return this;
        }

        Applier withUnchecked(Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                arguments.put(entry.getKey().toString(), entry.getValue());
            }
            return this;
        }
    }

    private static class Serializer implements TypeSerializer<MarkupTemplate> {

        @Override
        public MarkupTemplate deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            MarkupSpec spec = node.getValue(MarkupSpec.TYPE_TOKEN);
            String template = node.getNode("template").getString("");
            return spec.template(template);
        }

        @Override
        public void serialize(TypeToken<?> type, MarkupTemplate template, ConfigurationNode node) throws ObjectMappingException {
            node.setValue(MarkupSpec.TYPE_TOKEN, template.spec);
            node.getNode("template").setValue(template.template);
        }
    }
}
