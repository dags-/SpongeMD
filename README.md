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
    Text example0 = MarkdownSpec.create().render("[green,underlined](this is some green & underlined text!)");
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
show text | represented by a Markdown statement | `[yellow](this the hover text!)`
suggest command | represented by a string starting with a single forward-slash | `/command`
run command | represented by a string starting with two forward-slashes | `//command arg`
open url | represented by a url | `https://address.com`
insert text | represented by any other plain text string | `some plain text`

#### Content
The content is the visible text to which color, style, and action parameters will be applied.  
Content can include nested statements that have different parameters applied to the surround text.  
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
`[yellow,underlined,[green](click me!),https://github.com](some text that shows a hover message and opens a url)`|

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
Permission | Description
:---|:---
`markdown.colors.aqua` | allow use of aqua
`markdown.colors.black` | allow use of black
`markdown.colors.blue` | allow use of blue
`markdown.colors.dark_aqua` | allow use of dark_aqua
`markdown.colors.dark_blue` | allow use of dark_blue
`markdown.colors.dark_gray` | allow use of dark_gray
`markdown.colors.dark_green` | allow use of dark_green
`markdown.colors.dark_purple` | allow use of dark_purple
`markdown.colors.dark_red` | allow use of dark_red
`markdown.colors.gold` | allow use of gold
`markdown.colors.gray` | allow use of gray
`markdown.colors.green` | allow use of green
`markdown.colors.light_purple` | allow use of light_purple
`markdown.colors.none` | allow use of none
`markdown.colors.red` | allow use of red
`markdown.colors.reset` | allow use of reset
`markdown.colors.white` | allow use of white
`markdown.colors.yellow` | allow use of yellow
`markdown.styles.bold` | allow use of bold
`markdown.styles.italic` | allow use of italic
`markdown.styles.obfuscated` | allow use of obfuscated
`markdown.styles.reset` | allow use of reset
`markdown.styles.strikethrough` | allow use of strikethrough
`markdown.styles.underline` |  allow use of underline
`markdown.actions.command.run` | allow use of run command action
`markdown.actions.command.suggest` | allow use of suggest command action
`markdown.actions.text.show` | allow use of show text action
`markdown.actions.text.insert` | allow use of insert text action
`markdown.actions.url.open` | allow use of open url action
