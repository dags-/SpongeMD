package me.dags.spongemd;

import com.google.common.base.Preconditions;
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
public class MarkdownPerms {

    public static final TextTemplate.Arg PERMISSION_ID = TextTemplate.arg("id").build();

    private final String colorRoot;
    private final String styleRoot;
    private final String suggestCommand;
    private final String runCommand;
    private final String insertText;
    private final String showText;
    private final String openUrl;
    private final String reset;

    private MarkdownPerms(Builder builder) {
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

    public MarkdownSpec createSpec(Subject subject) {
        return MarkdownSpec.create(subject, this);
    }

    public void registerPermissions(Object plugin) {
        registerColorPermissions(plugin, TextTemplate.of("Allow use of Color: ", PERMISSION_ID));
        registerStylePermissions(plugin, TextTemplate.of("Allow use of Style: ", PERMISSION_ID));
        registerActionPermissions(plugin, TextTemplate.of("Allow use of Action: ", PERMISSION_ID));
    }

    public void registerColorPermissions(Object plugin, TextTemplate template) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        Map<String, Object> map = new HashMap<>();
        MDParam.colors().values().forEach(color -> {
            String permission = getColorNode(color);
            map.put(PERMISSION_ID.getName(), color.getId());
            registerPermission(service, plugin, permission, template.apply(map).build());
        });
    }

    public void registerStylePermissions(Object plugin, TextTemplate template) {
        PermissionService service = Sponge.getServiceManager().provideUnchecked(PermissionService.class);
        Map<String, Object> map = new HashMap<>();
        MDParam.styles().values().forEach(style -> {
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

        private String colorRoot = "markdown.colors";
        private String styleRoot = "markdown.styles";
        private String suggestCommand = "markdown.actions.command.suggest";
        private String runCommand = "markdown.actions.command.run";
        private String insertText = "markdown.actions.text.insert";
        private String showText = "markdown.actions.text.show";
        private String openUrl = "markdown.actions.url.open";
        private String reset = "markdown.format.reset";

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

        public MarkdownPerms build() {
            return new MarkdownPerms(this);
        }

        private static void checkLength(String node) {
            if (node.length() < 2) {
                throw new UnsupportedOperationException("Invalid permission node: " + node);
            }
        }
    }
}
