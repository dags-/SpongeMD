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

package me.dags.text.template;

import com.google.common.collect.ImmutableMap;
import me.dags.text.syntax.Property;
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

public class MUApplier extends SimpleTextTemplateApplier {

    private final MUTemplate template;
    private final Map<String, Object> arguments = new HashMap<>();
    private Property.Predicate predicate;

    MUApplier(MUTemplate template) {
        this.template = template;
        this.predicate = template.predicate;
    }

    @Override
    public ImmutableMap<String, TextElement> getParameters() {
        return ImmutableMap.copyOf(arguments.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> template.spec.render(e.getValue().toString())
        )));
    }

    @Override
    public void setParameter(String key, TextElement element) {
        checkNotNull(key, "key");
        Text.Builder builder = Text.builder();
        element.applyTo(builder);
        String value = template.spec.write(builder.build());
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

    public MUApplier subject(Subject subject) {
        predicate = template.spec.getPermissions().wrap(subject);
        return this;
    }

    public MUApplier with(String key, Object value) {
        if (value instanceof TextElement) {
            setParameter(key, (TextElement) value);
        } else {
            arguments.put(key, value);
        }
        return this;
    }

    public MUApplier with(Map map) {
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            with(entry.getKey().toString(), entry.getValue());
        }
        return this;
    }

    public MUApplier with(SimpleTextFormatter partition) {
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
            template.apply(arguments, writer);
            writer.flush();
            return template.spec.render(predicate, writer.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            return Text.EMPTY;
        }
    }
}
