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

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestColor implements TextColor {

    private static final Map<String, TextColor> all = new HashMap<>();
    public static final Map<String, TextColor> REGISTRY = Collections.unmodifiableMap(all);

    private final String name;

    private TestColor(String name) {
        this.name = name;
        all.put(name, this);
    }

    @Override
    public Color getColor() {
        return Color.WHITE;
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

    public static final TextColor AQUA = new TestColor("aqua");
    public static final TextColor BLACK = new TestColor("black");
    public static final TextColor BLUE = new TestColor("blue");
    public static final TextColor DARK_AQUA = new TestColor("dark_aqua");
    public static final TextColor DARK_BLUE = new TestColor("dark_blue");
    public static final TextColor DARK_GRAY = new TestColor("dark_gray");
    public static final TextColor DARK_GREEN = new TestColor("dark_green");
    public static final TextColor DARK_PURPLE = new TestColor("dark_purple");
    public static final TextColor DARK_RED = new TestColor("dark_red");
    public static final TextColor GOLD = new TestColor("gold");
    public static final TextColor GRAY = new TestColor("gray");
    public static final TextColor GREEN = new TestColor("green");
    public static final TextColor LIGHT_PURPLE = new TestColor("light_purple");
    public static final TextColor NONE = new TestColor("none");
    public static final TextColor RED = new TestColor("red");
    public static final TextColor RESET = new TestColor("reset");
    public static final TextColor WHITE = new TestColor("white");
    public static final TextColor YELLOW = new TestColor("yellow");
}
