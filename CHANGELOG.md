## [0.1.1] - 2018-07-29
- Now has a Changelog!
- Mending Charm now always fully repairs (also removed the config). It stores the remaining durability in the Mending Charm itself.
- Changed Mending Charm default config to repair 1.8 durability per xp (vanilla uses 2 durability per xp)
- Changed Mending Charm default config to repair 1 durability per tick instead of 10000 every 5 seconds.
- Removed config for disabling Mending Charm recipe (since it now uses resources pack system for the recipe instead of a custom recipe).
- Mending Charm now has an animation for when it's repairing stuff.
- Config can now be reloaded in game

## [0.1.0] - 2018-07-28
- Updated to Minecraft 1.12.2
- Changed name of project to Sponcy
- Changed crafting recipe for Mending Charm. Now consumes the item (no longer disenchants).
- New texture for the Mending Charm
- Recipe for Mending Charm now shows up in JEI
- Added IngredientEnchanted (sponcy:enchanted) which matches any items that satisfy the enchantment requirements.
- Added Enchanted Item, a placeholder for items that have the specified enchantment.
- Added SQLite wrapper (bundled with the mod)
- Added Shop Manager (doesn't do anything at the moment) and the start of a shop framework that uses a database backend.
- Added Dev Tool (doesn't do anything, used to test code snippets)


## [0.0.2] - 2017-02-28
- Changed: Mending Charm no longer wastes experience (configurable) also added debug messages (configurable)
- Bugfix: Fixed a bad bug in CostHelper (add method was setting the totalCost rather than adding to totalCost)

## [0.0.1] - 2017-02-27
- Initial Release of Spontaneous Collection for Minecraft 1.10.2
- Added: Mending Charm (repairs items enchanted with Mending using player experience)
