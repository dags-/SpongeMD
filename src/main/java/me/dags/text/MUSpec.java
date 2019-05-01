/*
 * MIT License
 *
 * Copyright (c) 2019 dags
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.dags.text;

import me.dags.template.CharReader;
import me.dags.template.Template;
import me.dags.text.preset.MUPresets;
import me.dags.text.syntax.Parser;
import me.dags.text.syntax.Property;
import me.dags.text.syntax.Writer;
import me.dags.text.template.MUTemplate;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class MUSpec implements TextSerializer {

    private static final MUSpec global = new MUSpec("global", "textmu:global", MUPerms.DEFAULTS, MUPresets.NONE, MUPerms.ANY);

    private final String id;
    private final String name;
    private final MUPresets presets;
    private final MUPerms permissions;
    private final Property.Predicate defaults;

    private MUSpec(MUPerms permissions, MUPresets presets, Property.Predicate defaults) {
        this("spec", "textmu:spec", permissions, presets, defaults);
    }

    private MUSpec(String name, String id, MUPerms permissions, MUPresets presets, Property.Predicate defaults) {
        this.permissions = permissions.withPresets(presets);
        this.presets = presets;
        this.defaults = defaults;
        this.name = name;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public MUPresets getPresets() {
        return presets;
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
            CharReader reader = new CharReader(input);
            return new Parser(reader, presets, predicate).parse().build();
        } catch (IOException e) {
            return Text.EMPTY;
        }
    }

    public Text render(Property.Predicate predicate, Reader reader) {
        try {
            CharReader charReader = new CharReader(reader);
            return new Parser(charReader, presets, predicate).parse().build();
        } catch (IOException e) {
            return Text.EMPTY;
        }
    }

    @Override
    public String serialize(Text text) {
        return write(text);
    }

    @Override
    public Text deserialize(String input) throws TextParseException {
        return render(input);
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
        write(text, writer, false);
    }

    public void write(Text text, java.io.Writer writer, boolean escape) {
        try {
            Writer textWriter = new Writer(writer);
            textWriter.write(text, escape);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String writeEscaped(Text text) {
        StringWriter writer = new StringWriter();
        write(text, writer, true);
        return writer.toString();
    }

    public static MUSpec create() {
        return create(MUPerms.DEFAULTS, MUPresets.NONE);
    }

    public static MUSpec create(MUPerms permissions) {
        return create(permissions, MUPresets.NONE, MUPerms.ANY);
    }

    public static MUSpec create(MUPresets presets) {
        return create(MUPerms.DEFAULTS, presets, MUPerms.ANY);
    }

    public static MUSpec create(MUPerms permissions, MUPresets presets) {
        return create(permissions, presets, MUPerms.ANY);
    }

    public static MUSpec create(MUPerms permissions, MUPresets presets, Property.Predicate defaults) {
        return new MUSpec(permissions, presets, defaults);
    }

    public static MUSpec global() {
        return global;
    }
}
