# SpongeMD
A small, Markdown inspired, TextSerializer for SpongeAPI  
_This is a library, not a standalone plugin!_

### Contents
1. [Dependencies](#dependencies)
2. [Usage](#usage)
3. [Specification](#notation-specification)
4. [Examples](#examples)
5. [Permissions](#permissions)

### Dependencies
##### Gradle
```
repositories {
    ...
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    ...
    compile ('com.github.dags-:SpongeMD:1.0-SNAPSHOT') {
        exclude module: 'spongeapi'
    }
}

// handle shading of dependencies
```
#### Maven
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.dags-</groupId>
        <artifactId>SpongeMD</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<!--- handle shading of dependencies --->
```

====

### Usage
#### Rendering/Deserialization
To parse SpongeMD strings you must obtain a MarkdownSpec object.  
Depending on what the Spec allows, colors/styles/actions can be stripped out of the resulting Text object.    
As shown in the following example, there are three different ways of obtaining a new MarkdownSpec, each offering
 different ways of restricting formatting/actions.

``` java
public void example(Player player) {
    // All formatting colors/styles/actions allowed
    Text example0 = MarkdownSpec.create().render("[green,underline](this is some green & underlined text!)");
    player.sendMessage(example0);

    // Restrict colors/styles/actions to those allowed by the Player's permissions
    Text example1 = MarkdownSpec.create(player).render("[https://github.com](Click this text to open github)");
    player.sendMessage(example1);

    // Restrict colors/styles/actions to those set by the builder
    Text example2 = MarkdownSpec.builder()
            .allow(TextColors.GREEN)
            .allow(TextStyles.ITALIC)
            .build()
            .render("[green,italic,bold](This text is green and italic, but not bold)");
    player.sendMessage(example2);
}
```
#### Writing/Serialization
MarkdownSpecs can also be used to write SpongeMD strings from Text objects.  
The same color/style/action rules are applied by the Spec when writing the SpongeMD string as they are when rendering
 them.

``` java
public void example(Text text, MarkdownSpec spec) {
    // Write the given text to a SpongeMD string.
    // Formatting/Actions may be removed depending on what the Spec allows
    String exmaple0 = spec.write(text);
    System.out.println(example0);
}
```

====

### Notation Specification
The notation takes inspiration from the Markdown link format.  
A typical SpongeMD string looks like the following:

`[parameters...](content)`

<sub>_Unlike Markdown links, the visible portion is placed on the right-hand side of the statement, within the braces
 `(..)`._</sub>

#### Parameters:
Parameters represent the Colors, Styles, Actions that you want the 'content' text to have, and are separated by commas.
Notation characters used in a parameter (such as an open bracket '[') should be back-slash escaped `\[`, or encapsulated
 within [backticks](http://superuser.com/questions/254076/how-do-i-type-the-tick-and-backtick-characters-on-windows)
  `` ` `` to tell the parser to render it as normal text rather than to interpret as part of a statement.

Parameter | Description | Example
---|---|---
color | represented by the [color name or formatting code](http://minecraft.gamepedia.com/Formatting_codes) | `green` or `a`
style | represented by the [style name or formatting code](http://minecraft.gamepedia.com/Formatting_codes) | `bold` or `l`
show text | represented by a Markdown statement | `[yellow](hover text!)`
suggest command | represented by a string starting with a single forward-slash | `/command`
run command | represented by a string starting with two forward-slashes | `//command arg`
open url | represented by a url | `https://address.com`
insert text | represented by any other plain text string | `some plain text`

#### Content
The content is the visible text to which color, style, and action parameters will be applied.  
Content can include nested statements that have different parameters applied to them than the surrounding text.  
Notation characters (such as an open bracket '[') should be back-slash escaped `\[`, or encapsulated within
 [backticks](http://superuser.com/questions/254076/how-do-i-type-the-tick-and-backtick-characters-on-windows) `` ` ``
 to tell the parser to render it as normal text rather than to interpret as part of a statement.

====

### Examples
Examples |
:---|
`[green,italic](some italic, green text)`|
``[blue](Some escaped text: `[gold](example)`)``|
`plain text and then [green](some green text)`|
`plain text and then [green](some green text) and then plain text again`|
`[red](some red text with [yellow,underline](some yellow, underlined text) and some more red text)`|
`[yellow,underline,[green](click me!),https://github.com](some text that shows a hover message and opens a url)`|

Short-hand |
:---|
`[a,o](some italic, green text)`|
``[9](Some escaped text: `[6](example)`)``|
`plain text and then [a](some green text)`|
`plain text and then [a](some green text) and then plain text again`|
`[c](some red text with [e,n](some yellow, underlined text) and some more red text)`|
`[e,n,[a](click me!),https://github.com](some text that shows a hover message and opens a url)`|

====

### Permissions
Parameter | Permission | Description
:---|:---|:---
`aqua` or `b` | `markdown.colors.aqua` | allow use of aqua
`black` or `0` '| `markdown.colors.black` | allow use of black
`blue` or `9` | `markdown.colors.blue` | allow use of blue
`dark_aqua` or `3` | `markdown.colors.dark_aqua` | allow use of dark_aqua
`dark_blue` or `1` | `markdown.colors.dark_blue` | allow use of dark_blue
`dark_gray` or `8` | `markdown.colors.dark_gray` | allow use of dark_gray
`dark_green` or `2` | `markdown.colors.dark_green` | allow use of dark_green
`dark_purple` or `5` | `markdown.colors.dark_purple` | allow use of dark_purple
`dark_red` or `4` | `markdown.colors.dark_red` | allow use of dark_red
`gold` or `6` | `markdown.colors.gold` | allow use of gold
`gray` or `7` | `markdown.colors.gray` | allow use of gray
`green` or `a` | `markdown.colors.green` | allow use of green
`light_purple` or `d` | `markdown.colors.light_purple` | allow use of light_purple
`none` | `markdown.colors.none` | allow use of none
`red` or `c` | `markdown.colors.red` | allow use of red
`reset` | `markdown.colors.reset` | allow use of reset
`white` or `f` | `markdown.colors.white` | allow use of white
`yellow` or `e` | `markdown.colors.yellow` | allow use of yellow
`bold` or `l` | `markdown.styles.bold` | allow use of bold
`italic` or `o` | `markdown.styles.italic` | allow use of italic
`obfuscated` or `k` | `markdown.styles.obfuscated` | allow use of obfuscated
`reset` or `r` | `markdown.styles.reset` | allow use of reset
`yellow` or `e` | `markdown.styles.strikethrough` | allow use of strikethrough
`underline` or `n` | `markdown.styles.underline` |  allow use of underline
`//<command>` | `markdown.actions.command.run` | allow use of run command action
`/<command>` | `markdown.actions.command.suggest` | allow use of suggest command action
`[param](content)` | `markdown.actions.text.show` | allow use of show text action
`<plain text>` | `markdown.actions.text.insert` | allow use of insert text action
`http://url.com` | `markdown.actions.url.open` | allow use of open url action
