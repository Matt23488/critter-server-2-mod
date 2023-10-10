execute unless entity @e[type=#minecraft:skeletons,distance=..32,predicate=!critter:bewitched] run playsound minecraft:entity.villager.no player @s
execute at @e[type=#minecraft:skeletons,distance=..32,predicate=!critter:bewitched] run playsound minecraft:entity.illusioner.cast_spell neutral @a ~ ~ ~
effect give @e[type=#minecraft:skeletons,distance=..32] critter:bewitched infinite