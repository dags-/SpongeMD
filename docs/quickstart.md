# Quickstart

### Gradle Dependency

```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "com.github.dags-:TextMU:0.3.4"
}
```

### Examples

#### Basic text rendering
```java
Text text = MUSpec.global().render("Hello [World](blue)");
Sponge.getServer().getBroadcastChannel().send(text);
```

_Renders the text "Hello World", where "World" will be colored blue_


#### Permission-based text rendering
```java
Text text = MUSpec.global().render(player, "Hello [World!](green)");
Sponge.getServer().getBroadcastChannel().send(text);
```
_Renders the text "Hello World!", where "World!" will be colored green if the given player has the permission `textmu.color.green`_
