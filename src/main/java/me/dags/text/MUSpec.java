package me.dags.text;

import me.dags.template.CharReader;
import me.dags.template.Template;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class MUSpec {

    private static final MUSpec global = create();

    private final MUPerms permissions;
    private final Property.Predicate defaults;

    private MUSpec(MUPerms permissions, Property.Predicate defaults) {
        this.permissions = permissions;
        this.defaults = defaults;
    }

    public MUPerms getPermissions() {
        return permissions;
    }

    public Text render(Subject subject, String input) {
        Property.Predicate predicate = permissions.wrap(subject);
        return render(predicate, input);
    }

    public Text render(String input) {
        return render(defaults, input);
    }

    public Text render(Property.Predicate predicate, String input) {
        try {
            return Parser.parse(input, predicate);
        } catch (IOException e) {
            return Text.EMPTY;
        }
    }

    public Text render(Property.Predicate predicate, Reader reader) {
        try {
            return Parser.parse(reader, predicate);
        } catch (IOException e) {
            return Text.EMPTY;
        }
    }

    public MUTemplate template(String input) {
        try {
            return new MUTemplate(this, Template.parse(new CharReader(input)), defaults);
        } catch (IOException e) {
            e.printStackTrace();
            return MUTemplate.EMPTY;
        }
    }

    public MUTemplate template(Reader reader) {
        try {
            return new MUTemplate(this, Template.parse(new CharReader(reader)), defaults);
        } catch (IOException e) {
            e.printStackTrace();
            return MUTemplate.EMPTY;
        }
    }

    public String write(Text text) {
        StringWriter writer = new StringWriter();
        write(text, writer);
        return writer.toString();
    }

    public void write(Text text, java.io.Writer writer) {
        try {
            Writer textWriter = new Writer(writer);
            textWriter.write(text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MUSpec create() {
        return create(MUPerms.DEFAULTS);
    }

    public static MUSpec create(MUPerms permissions) {
        return new MUSpec(permissions, MUPerms.ANY);
    }

    public static MUSpec create(MUPerms permissions, Property.Predicate defaults) {
        return new MUSpec(permissions, defaults);
    }

    public static MUSpec global() {
        return global;
    }
}
