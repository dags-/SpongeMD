# TextMU
A markup syntax library for Sponge

[![](https://jitpack.io/v/dags-/TextMU.svg)](https://jitpack.io/#dags-/TextMU)

## Syntax
The syntax is similar to Markdown links:
```
[the visible text](color,style,actions)
```

Nested syntax is valid:
```
[Some yellow text and [some green text](green)](yellow)
```

Text actions are supported:

- RunCommand - a command prefixed by one forward slash  
`[Click to run command](/say hello world)`
- SuggestCommand - a command prefixed by two forward slashes  
`[Click to suggest command](//say hello world)`
- OpenURL - a valid URL  
`[Click to open google](https://google.com)`
- HoverText - any other string that does not match the above (can include the MU syntax)  
`[Mouse over me](this is a [hidden](italic) message!)`

## Templates
TextMU incorporates a simple string templating engine which it can render to text.  
You can read more about the template syntax and usage [here](https://github.com/dags-/Template/blob/master/README.md)

## Usage/Code Examples
#### Dependency:
```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "com.github.dags-:TextMU:0.3.0"
}
```

#### Basic text rendering:
```
Text text = MUSpec.global().render("Hello [World](blue)");
Sponge.getServer().getBroadcastChannel().send(text);
```

#### Permission-based text rendering:
```
Text text = MUSpec.global().render(player, "Hello [World!](green)");
Sponge.getServer().getBroadcastChannel().send(text);
```
_Here, the output text will only include colors/styles/actions that the player has permission to use_

#### Further examples
See the [example plugin](https://github.com/dags-/TextMU/blob/master/src/test/java/ExamplePlugin.java)
