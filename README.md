# Shadowrun Helper

A small CLI tool to help manage Shadowrun 6 games.

## Compile the tool

You need the Scala Build Tool (SBT). You will probably need to compile Lanterna before compiling this, so that it is 
present in your local Maven repository. See my [Lanterna2 fork](https://github.com/zyuiop/lanterna.git) for instructions.

Once you compiled Lanterna, you can compile this program with `sbt dist`. This will generate a zip file in `target/universal`.
This zip file should be identical from the ones you can grab from the releases.

## Run the tool

Unzip the release file and run the `bin/main` (Windows: `bin/main.bat`) file.

## Configuration / data

The tool should save all of its data in `.config/shadowrun`. On first run, it should
create a basic file hierarchy, and, in particular, it should save two ennemies from the Core Rule Book in the
ennemies folder. These can be used to build roosters and scenes.

## Features

 - Handle ennemy roosters. A rooster is a pack of enemy you can quickly load to a scene.
 - Handle scenes. A scene is a pack of ennemies _with some state_ (in particular health). 
   - This is useful for bookkeeping!
   - You can select an ennemy and press `[D]` to take one damage, `[H]` to heal one damage