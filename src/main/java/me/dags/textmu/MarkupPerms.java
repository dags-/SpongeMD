package me.dags.textmu;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class MarkupPerms {

    public static final TypeToken<MarkupPerms> TYPE_TOKEN = TypeToken.of(MarkupPerms.class);
    public static final TypeSerializer<MarkupPerms> TYPE_SERIALIZER = new Serializer();
    public static final TextTemplate.Arg PERMISSION_ID = TextTemplate.arg("id").build();

    static {
        TypeSerializers.getDefaultSerializers().registerType(TYPE_TOKEN, TYPE_SERIALIZER);
    }

    private final String colorRoot;
    private final String styleRoot;
    private final String suggestCommand;
    private final String runCommand;
    private final String insertText;
    private final String showText;
    private final String openUrl;
    private final String reset;

    private MarkupPerms(Builder builder) {
        this.colorRoot = builder.colorRoot;
        this.styleRoot = builder.styleRoot;
        this.suggestCommand = builder.suggestCommand;
        this.runCommand = builder.runCommand;
        this.insertText = builder.insertText;
        this.showText = builder.showText;
        this.openUrl = builder.openUrl;
        this.reset = builder.reset;
    }

    String getColorNode(TextColor color) {
        return colorRoot + "." + color.getId().toLowerCase();
    }

    String getStyleNode(TextStyle.Base style) {
        return styleRoot + "." + style.getId().toLowerCase();
    }

    String getSuggestCommand() {
        return suggestCommand;
    }

    String getRunCommand() {
        return runCommand;
    }

    String getInsertText() {
        return insertText;
    }

    String getShowText() {
        return showText;
    }

    String getOpenUrl() {
        return openUrl;
    }

    String getReset() {
        return reset;
    }

    public MarkupSpec createSpec(Subject subject) {
        return MarkupSpec.create(subject, this);
    }

    public void registerPermissions(Object plugin) {
        registerColorPermissions(plugin, TextTemplate.of("Allow use of Color: ", PERMISSION_ID));
        registerStylePermissions(plugin, TextTemplate.of("Allow use of Style: ", PERMISSION_ID));
        registerActionPermissions(plugin, TextTemplate.of("Allow use of Action: ", PERMISSION_ID));
    }

    public void registerColorPermissions(Object plugin, TextTemplate template) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        Map<String, Object> map = new HashMap<>();
        MUParam.colors().values().forEach(color -> {
            String permission = getColorNode(color);
            map.put(PERMISSION_ID.getName(), color.getId());
            registerPermission(service, plugin, permission, template.apply(map).build());
        });
    }

    public void registerStylePermissions(Object plugin, TextTemplate template) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        Map<String, Object> map = new HashMap<>();
        MUParam.styles().values().forEach(style -> {
            String permission = getStyleNode(style);
            map.put(PERMISSION_ID.getName(), style.getId());
            registerPermission(service, plugin, permission, template.apply(map).build());
        });
    }

    public void registerActionPermissions(Object plugin, TextTemplate template) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        Map<String, Object> map = new HashMap<>();

        map.put(PERMISSION_ID.getName(), "Suggest Command");
        registerPermission(service, plugin, getSuggestCommand(), template.apply(map).build());

        map.put(PERMISSION_ID.getName(), "Run Command");
        registerPermission(service, plugin, getRunCommand(), template.apply(map).build());

        map.put(PERMISSION_ID.getName(), "Insert Text");
        registerPermission(service, plugin, getInsertText(), template.apply(map).build());

        map.put(PERMISSION_ID.getName(), "Show Text");
        registerPermission(service, plugin, getShowText(), template.apply(map).build());

        map.put(PERMISSION_ID.getName(), "Open URL");
        registerPermission(service, plugin, getOpenUrl(), template.apply(map).build());

        map.put(PERMISSION_ID.getName(), "Rest");
        registerPermission(service, plugin, getReset(), template.apply(map).build());
    }

    private void registerPermission(PermissionService service, Object plugin, String node, Text description) {
        if (!service.getDescription(node).isPresent()) {
            service.newDescriptionBuilder(plugin).ifPresent(builder -> builder.id(node).description(description).register());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String colorRoot = "markup.colors";
        private String styleRoot = "markup.styles";
        private String suggestCommand = "markup.actions.command.suggest";
        private String runCommand = "markup.actions.command.run";
        private String insertText = "markup.actions.text.insert";
        private String showText = "markup.actions.text.show";
        private String openUrl = "markup.actions.url.open";
        private String reset = "markup.format.reset";

        private Builder(){}

        public Builder colorRoot(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.colorRoot = node.endsWith(".") ? node.substring(0, node.length() - 1) : node;
            return this;
        }

        public Builder styleRoot(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.styleRoot = node.endsWith(".") ? node.substring(0, node.length() - 1) : node;
            return this;
        }

        public Builder suggestCommand(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.suggestCommand = node;
            return this;
        }

        public Builder runCommand(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.runCommand = node;
            return this;
        }

        public Builder insertText(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.insertText = node;
            return this;
        }

        public Builder showText(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.showText = node;
            return this;
        }

        public Builder openUrl(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.openUrl = node;
            return this;
        }

        public Builder reset(String node) {
            Preconditions.checkNotNull(node);
            Builder.checkLength(node);
            this.reset = node;
            return this;
        }

        public MarkupPerms build() {
            return new MarkupPerms(this);
        }

        private static void checkLength(String node) {
            if (node.length() < 2) {
                throw new UnsupportedOperationException("Invalid permission node: " + node);
            }
        }
    }

    private static class Serializer implements TypeSerializer<MarkupPerms> {

        @Override
        public MarkupPerms deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
            Builder builder = new Builder();
            builder.reset = node.getNode("reset").getString(builder.reset);
            builder.openUrl = node.getNode("open_url").getString(builder.openUrl);
            builder.showText = node.getNode("show_text").getString(builder.showText);
            builder.insertText = node.getNode("insert_text").getString(builder.insertText);
            builder.runCommand = node.getNode("run_command").getString(builder.runCommand);
            builder.suggestCommand = node.getNode("suggest_command").getString(builder.suggestCommand);
            builder.colorRoot = node.getNode("color_root").getString(builder.colorRoot);
            builder.styleRoot = node.getNode("style_root").getString(builder.styleRoot);
            return builder.build();
        }

        @Override
        public void serialize(TypeToken<?> type, MarkupPerms perms, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("reset").setValue(perms.reset);
            node.getNode("open_url").setValue(perms.openUrl);
            node.getNode("show_text").setValue(perms.showText);
            node.getNode("insert_text").setValue(perms.insertText);
            node.getNode("run_command").setValue(perms.runCommand);
            node.getNode("suggest_command").setValue(perms.suggestCommand);
            node.getNode("color_root").setValue(perms.colorRoot);
            node.getNode("style_root").setValue(perms.styleRoot);
        }
    }
}
