# TextMU
A small, Markdown inspired, TextSerializer for SpongeAPI  
_This is a library, not a standalone plugin!_

### Contents
1. [Dependencies](#dependencies)
2. [Usage](#usage)
 1. [Deserializing](#renderingdeserialization)
 2. [Serializing](#writingserialization)
 3. [Templating](#templating)
3. [Specification](#notation-specification)
4. [Examples](#examples)
5. [Permissions](#permissions)

### Dependencies
See releases for the [latest version](https://github.com/dags-/TextMU/releases/latest).

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
    compile ('com.github.dags-:TextMU:{insert_version}') {
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
        <artifactId>TextMU</artifactId>
        <version>{insert_version}</version>
    </dependency>
</dependencies>

<!--- handle shading of dependencies --->
```

====

### Usage
#### Rendering/Deserialization
To parse TextMU strings you must obtain a MarkupSpec object.  
The Spec controls which features (colors, styles & actions) will be rendered in the output Text object.    
As shown in the following example, there are three different ways of obtaining a new MarkupSpec, each offering
 different ways of restricting formatting/actions.

``` java
public void example(Player player) {
    // All formatting colors/styles/actions allowed
    Text example0 = MarkupSpec.create().render("[green,underline](this is some green & underlined text!)");
    player.sendMessage(example0);

    // Restrict colors/styles/actions to those allowed by the Player's permissions
    Text example1 = MarkupSpec.create(player).render("[https://github.com](Click this text to open github)");
    player.sendMessage(example1);

    // Restrict colors/styles/actions to those set by the builder
    Text example2 = MarkupSpec.builder()
            .allow(TextColors.GREEN)
            .allow(TextStyles.ITALIC)
            .build()
            .render("[green,italic,bold](This text is green and italic, but not bold)");
    player.sendMessage(example2);
}
```
#### Writing/Serialization
MarkupSpecs can also be used to write TextMU strings from Text objects.  
The same color/style/action rules are applied by the Spec when writing the TextMU string as they are when rendering
 them.

``` java
public void example(Text text, MarkupSpec spec) {
    // Write the given text to a TextMU string.
    // Formatting/Actions may be removed depending on what the Spec allows
    String exmaple0 = spec.write(text);
    System.out.println(example0);
}
```
#### Templating
TextMU implements a basic templating system that allows you to define preset TextMU strings containing named
 variables which get replaced by the arguments you provide each time you render the string to a Text object.
 
Variables are defined by encapsulating the variable name inside braces: `{my_variable}`  
Templates are created using the MarkupSpec object:

``` java
MarkupTemplate template = spec.template("[red](Some variable = {my_variable})";
```

To use the template, arguments are provided via the chaining method `.with("<variable_name>", <variable_value>)`  
For example:

``` java
MarkupTemplate template = spec.template("[red](Some variable = {my_variable})";
Text text = template.with("my_variable", 12345).render();
// Produces 'Some variable = 12345' in red text
```

Templates themselves can be provided as named arguments and called within other templates. Template variables are
 defined by prefixing a colon (`:`) to the variable name: `{:my_template_variable}`.  
This template will inherit the same set of arguments provided to the parent template during the render method.

A specific argument can be passed to the template by stating it's name _before_ the template definition: `{variable:template}`.
Arguments passed to a template in this way are named `.` (referenced as `{.}` in the target template).

Array and Iterable arguments are iterated over and each child element is passed to the template individually.  
If the argument happens to be a Map, each key/value pair is passed to the template with the names `.key` & `.value`.  
A separator String can be inserted between each child by providing a third parameter in the template definition:
 `{variable:template:separator}` (this is handy for rendering comma-separated lists and such).

``` java
private static void templates(CommandSource source, List<String> list, Map<Object, Object> map) {
    MarkupSpec spec = MarkupSpec.create();
    
    // Create a template that passes the elements of a list to a second template which defines how they be formatted
    MarkupTemplate listTemplate = spec.template("Example #1: [green](List: {list:element:, })");
    MarkupTemplate elementFormat = spec.template("[blue]({.})");
    Text text1 = listTemplate.with("element", elementFormat).with("list", list).render();
    source.sendMessage(text1);
    
    // Create a template that passes the key/value pairs of a map to a second template that defines how they be formatted
    MarkupTemplate mapTemplate = spec.template("Example #2: [red](Map: {map:entry:; }");
    MarkupTemplate entryFormat = spec.template("[yellow]({.key}={.value})");
    Text text2 = mapTemplate.with("entry", entryFormat).with("map", map).render();
    source.sendMessage(text2);
}
```

====

### Notation Specification
The notation takes inspiration from the Markdown link format.  
A typical TextMU string looks like the following:

`[arguments...](content)`

<sub>_Unlike Markdown links, the visible portion is placed on the right-hand side of the statement, within the parentheses
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
show text | represented by a Markup statement | `[yellow](hover text!)`
suggest command | represented by a string starting with a single forward-slash | `/command`
run command | represented by a string starting with two forward-slashes | `//command arg`
open url | represented by a url | `https://address.com`
insert text | represented by any other plain text string | `some plain text`

#### Content
The content is the visible text to which color, style, and action arguments will be applied.  
Content can include nested statements that have different arguments applied to them than the surrounding text.  
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
`aqua` or `b` | `markup.colors.aqua` | allow use of aqua
`black` or `0` '| `markup.colors.black` | allow use of black
`blue` or `9` | `markup.colors.blue` | allow use of blue
`dark_aqua` or `3` | `markup.colors.dark_aqua` | allow use of dark_aqua
`dark_blue` or `1` | `markup.colors.dark_blue` | allow use of dark_blue
`dark_gray` or `8` | `markup.colors.dark_gray` | allow use of dark_gray
`dark_green` or `2` | `markup.colors.dark_green` | allow use of dark_green
`dark_purple` or `5` | `markup.colors.dark_purple` | allow use of dark_purple
`dark_red` or `4` | `markup.colors.dark_red` | allow use of dark_red
`gold` or `6` | `markup.colors.gold` | allow use of gold
`gray` or `7` | `markup.colors.gray` | allow use of gray
`green` or `a` | `markup.colors.green` | allow use of green
`light_purple` or `d` | `markup.colors.light_purple` | allow use of light_purple
`red` or `c` | `markup.colors.red` | allow use of red
`white` or `f` | `markup.colors.white` | allow use of white
`yellow` or `e` | `markup.colors.yellow` | allow use of yellow
`bold` or `l` | `markup.styles.bold` | allow use of bold
`italic` or `o` | `markup.styles.italic` | allow use of italic
`obfuscated` or `k` | `markup.styles.obfuscated` | allow use of obfuscated
`yellow` or `e` | `markup.styles.strikethrough` | allow use of strikethrough
`underline` or `n` | `markup.styles.underline` |  allow use of underline
`reset` or `r` | `markup.format.reset` | allow use of reset
`//<command>` | `markup.actions.command.run` | allow use of run command action
`/<command>` | `markup.actions.command.suggest` | allow use of suggest command action
`[param](content)` | `markup.actions.text.show` | allow use of show text action
`<plain text>` | `markup.actions.text.insert` | allow use of insert text action
`http://url.com` | `markup.actions.url.open` | allow use of open url action
