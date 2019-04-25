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

package me.dags.text.preset;

import com.google.common.collect.ImmutableMap;
import me.dags.template.CharReader;
import me.dags.text.syntax.Property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MUPresets {

    public static final MUPresets NONE = new MUPresets(Collections.emptyMap());

    public static final MUPresets MARKDOWN = MUPresets.builder()
            .add('*', "strong", "bold")
            .add('_', "light", "italic")
            .add('~', "strike", "strikethrough")
            .build();

    private final Map<Character, MUStyle> styles;

    private MUPresets(Map<Character, MUStyle> styles) {
        this.styles = styles;
    }

    public String apply(String input, Property.Predicate predicate) {
        if (isEmpty()) {
            return input;
        } else {
            StringBuilder buf = new StringBuilder(input.length());
            apply(input, buf, 0, CharReader.EOF, predicate);
            return buf.toString();
        }
    }

    public Map<Character, MUStyle> getStyles() {
        return styles;
    }

    public boolean isEmpty() {
        return styles.isEmpty();
    }

    private int apply(String source, StringBuilder dest, int start, char stop, Property.Predicate predicate) {
        boolean charEscape = false;
        boolean stringEscape = false;

        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);

            if (stringEscape) {
                if (c == '`') {
                    stringEscape = false;
                    continue;
                } else {
                    dest.append(c);
                    continue;
                }
            }

            if (charEscape) {
                charEscape = false;
                dest.append(c);
                continue;
            }

            if (c == '`') {
                stringEscape = true;
                continue;
            }

            if (c == '\\') {
                charEscape = true;
                continue;
            }

            if (c == stop) {
                return i;
            }

            MUStyle style = styles.getOrDefault(c, null);
            if (style == null || !predicate.test(style.getName())) {
                dest.append(c);
                continue;
            }

            StringBuilder buf = new StringBuilder();
            int end = apply(source, buf, i + 1, c, predicate);
            if (end > -1 && source.charAt(end) == c) {
                dest.append('[');
                dest.append(buf.toString());
                dest.append("](");
                dest.append(style.getProperties());
                dest.append(')');
                i = end;
            } else {
                dest.append(c);
            }
        }
        return -1;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Character, MUStyle> styles = new HashMap<>();

        public Builder add(char c, String name, String properties) {
            if (name.isEmpty() || properties.isEmpty()) {
                return this;
            }
            if (c == CharReader.EOF || c == '\\' || c == '`' || c == '[' || c == ']' || c == '(' || c == ')' || c == ',') {
                return this;
            } else {
                styles.put(c, new MUStyle(name, properties));
                return this;
            }
        }

        public MUPresets build() {
            return new MUPresets(ImmutableMap.copyOf(styles));
        }
    }
}
