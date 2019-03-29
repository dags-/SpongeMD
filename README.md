# TextMU
A small, Markdown inspired syntax for SpongeAPI  
_This is a library, not a standalone plugin!_

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

## Example Code
See the [example plugin](https://github.com/dags-/TextMU/blob/master/src/test/java/ExamplePlugin.java)
