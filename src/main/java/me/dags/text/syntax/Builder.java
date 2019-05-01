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

package me.dags.text.syntax;

import me.dags.template.CharReader;
import me.dags.text.preset.MUPresets;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class Builder {

    private final StringBuilder pre = new StringBuilder();
    private final List<Property> properties = new LinkedList<>();
    private final List<Builder> children = new LinkedList<>();

    private boolean valid = true;
    private char failChar = CharReader.EOF;

    public boolean isPlain() {
        return properties.isEmpty() && children.isEmpty();
    }

    public boolean isValid() {
        return valid;
    }

    public Builder fail(char c) {
        failChar = c;
        valid = false;
        return this;
    }

    public char failChar() {
        return failChar;
    }

    public String getPlain() {
        return pre.toString();
    }

    public Builder text(char c) {
        if (children.isEmpty()) {
            pre.append(c);
        } else {
            Builder post = new Builder();
            post.text(c);
            children.add(post);
            return post;
        }
        return this;
    }

    public Builder text(String s) {
        if (!s.isEmpty()) {
            if (children.isEmpty()) {
                pre.append(s);
            } else {
                return child(new Builder().text(s));
            }
        }
        return this;
    }

    public Builder property(Property p) {
        if (p != Property.NONE) {
            properties.add(p);
        }
        return this;
    }

    public Builder child(Builder child) {
        if (child.pre.length() == 0 && child.properties.isEmpty()) {
            children.addAll(child.children);
        } else {
            children.add(child);
        }
        return this;
    }

    public Text.Builder build(MUPresets preset, Property.Predicate predicate) throws IOException {
        Text.Builder builder;
        Iterator<Builder> iterator = children.iterator();

        if (pre.length() > 0) {
            String text = pre.toString();
            String plain = preset.apply(text, predicate);
            if (plain.length() == text.length()) {
                builder = Text.builder(plain);
            } else {
                builder = Parser.parse(plain, MUPresets.NONE, predicate);
            }
        } else if (iterator.hasNext()) {
            builder = iterator.next().build(preset, predicate);
        } else {
            return Text.EMPTY.toBuilder();
        }

        while (iterator.hasNext()) {
            builder.append(iterator.next().build(preset, predicate).build());
        }

        for (Property property : properties) {
            property.apply(builder);
        }

        return builder;
    }
}
