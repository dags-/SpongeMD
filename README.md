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

## Templates
Templates take the following form:
```
"Hello {name}" + {name: "world"} => "Hello World"
```
The text between the braces ("{}") is used as a key/getter for a value held in the root object.  
To refer to the root object itself, you can use the `{.}` notation.


Nested values are supported:
```
"Hello {planet|{name}}" + {planet: {name: "World"}} => "Hello World"
```
The left-hand side of the pipe ("|") defines the key/getter of the desired value.  
The right-hand side of the pipe defines a new template to be applied to said value.  

Iterable values are supported:
```
"Planets: {planets|{name}|, }" + {planets: [{name: "Earth"}, {name: "Mars"}, {name: "Pluto"}]} => "Planets: Earth, Mars, Pluto"
```
The left-hand side of the first pipe ("|") defines the key/getter of the desired iterable value.  
The right-hand side of the first pipe defines a new template to be applied to each of the elements.  
The right-hand side of the _second_ pipe is repeated between each element.

## Example Code
See the [example plugin](https://github.com/dags-/TextMU/blob/master/src/test/java/ExamplePlugin.java)
