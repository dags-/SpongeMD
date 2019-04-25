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

package impl;

import org.spongepowered.api.text.format.TextStyle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestStyle extends TextStyle.Base {

    private static final Map<String, Base> all = new HashMap<>();
    public static final Map<String, Base> REGISTRY = Collections.unmodifiableMap(all);

    private final String name;

    private TestStyle(String name, Boolean bold, Boolean italic, Boolean underline, Boolean strike, Boolean obf) {
        super(bold, italic, underline, strike, obf);
        this.name = name;
        all.put(name, this);
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static final TextStyle.Base BOLD = new TestStyle("bold", true, null, null, null, null);
    public static final TextStyle.Base ITALIC = new TestStyle("italic", null, true, null, null, null);
    public static final TextStyle.Base NONE = new TestStyle("none", null, null, null, null, null);
    public static final TextStyle.Base OBFUSCATED = new TestStyle("obfuscated", null, null, null, null, true);
    public static final TextStyle.Base RESET = new TestStyle("reset", null, null, null, null, null);
    public static final TextStyle.Base STRIKETHROUGH = new TestStyle("strikethrough", null, null, null, true, null);
    public static final TextStyle.Base UNDERLINE = new TestStyle("underline", null, null, true, null, null);
}
