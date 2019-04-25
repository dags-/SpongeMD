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

import impl.Init;
import impl.TestColor;
import impl.TestStyle;
import me.dags.text.MUSpec;
import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.translation.FixedTranslation;

public class WriteTests {

    static {
        Init.init();
    }

    @Test
    public void test0() {
        test(Text.of("hello world"), "hello world");
    }

    @Test
    public void test1() {
        test(Text.of("hello [world]"), "hello [world]");
    }

    @Test
    public void test2() {
        test(Text.of("hello [world]("), "hello [world](");
    }

    @Test
    public void test3() {
        test(
                Text.builder("hello ")
                        .append(Text.builder("world").color(TestColor.RED).build())
                        .build(),
                "hello [world](red)"
        );
    }

    @Test
    public void test4() {
        test(
                Text.builder("hello ")
                        .append(Text.builder("world").color(TestColor.RED).build())
                        .style(TestStyle.BOLD)
                        .build(),
                "[hello [world](red)](bold)"
        );

        TranslatableText.builder("pls").build();
    }

    @Test
    public void test5() {
        test(
                TranslatableText.of(new FixedTranslation("hello world")),
                "hello world"
        );
    }

    @Test
    public void test6() {
        test(
                Text.builder(new FixedTranslation("hello %s"), Text.of("world"))
                        .color(TestColor.GREEN)
                        .build(),
                "[hello world](green)"
        );
    }

    @Test
    public void test7() {
        Text text = Text.builder("[Player](this is a weird, chat format)")
                .color(TestColor.AQUA)
                .build();
        String result1 = MUSpec.global().writeEscaped(text);
        String expected1 = "[`[Player](this is a weird, chat format)`](aqua)";
        Assert.assertEquals(expected1, result1);
    }

    @Test
    public void test8() {
        test(
                Text.builder("Hover me")
                        .onHover(TextActions.showText(Text.of("This is the (hover) text")))
                        .color(TestColor.RED)
                        .build(),
                "[Hover me](`This is the (hover) text`,red)"
        );
    }

    private static void test(Text text, String expected) {
        String result = MUSpec.global().write(text);
        Assert.assertEquals(expected, result);
    }
}
