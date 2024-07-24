# CustomDiscs

This plugin will add you ability to create custom music discs with any music on it.

### Warning:
Plugin is in beta and may have bugs

## Features
* **Add custom music discs**: Add any music to disc
* **Vanilla-like Experience**: The plugin aims to replicate the familiar mechanics of vanilla Minecraft's music discs, ensuring a intuitive player experience.
* **Customizable messages and various options**: Configure almost entire plugin

## Commands:
The main command is `/customdiscs` or `/cd`
* `add <base item> <sound> <custom model data> <name>` - create new disc. Note that discs must have different sound
* `list` - get list of created discs. `S` means sound that disc will play, `N` means name that will be displayed, `CMD` means custom model data number that can be used for custom disc texture, `M` means what item is used for base of disc
* `del <disc id>` - delete disc
* `get <disc id> [to player]` - gives disc to you or to other player if specified
* `help` - sends a help message
* `reload` - reload plugin and config

### Example of adding new disc:
* `/cd add minecraft:music_disc_11 minecraft:ambient.underwater.loop.additions.rare 666 &bUnderwater`
* `/cd add minecraft:music_disc_chirp my_discs:ping 2006 &cExyl - Ping`

## How to play any music?
* Any music that exists in Minecraft client can be played. You can search for guides of adding custom music to game and read [this](https://minecraft.wiki/w/Sounds.json) article on Minecraft wiki, then you can use custom sound event name. If player doesn't have this sound nothing will happen.

## How to add custom texture to disc?
* You can use Custom Model Data number for vanilla texture changing or use OptiFine CIT feature

## Config:
* [link to config](https://github.com/BoBkiNN/CustomDiscs/blob/master/src/main/resources/config.yml)
