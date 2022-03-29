package net.runelite.client.plugins.NQuickPot;

import net.runelite.api.Skill;

public enum NQuickPotType
{
	ATTACK(Skill.ATTACK),
	STRENGTH(Skill.STRENGTH),
	DEFENCE(Skill.DEFENCE),
	RANGED(Skill.RANGED),
	MAGIC(Skill.MAGIC);
	/*PRAYER(Skill.PRAYER),
	RUNECRAFT(Skill.RUNECRAFT),
	CONSTRUCTION(Skill.CONSTRUCTION),
	HITPOINTS(Skill.HITPOINTS),
	AGILITY(Skill.AGILITY),
	HERBLORE(Skill.HERBLORE),
	THIEVING(Skill.THIEVING),
	CRAFTING(Skill.CRAFTING),
	FLETCHING(Skill.FLETCHING),
	SLAYER(Skill.SLAYER),
	HUNTER(Skill.HUNTER),
	MINING(Skill.MINING),
	SMITHING(Skill.SMITHING),
	FISHING(Skill.FISHING),
	COOOKING(Skill.COOKING),
	FIREMAKING(Skill.FIREMAKING),
	WOODCUTTING(Skill.WOODCUTTING),
	FARMING(Skill.FARMING);*/
	public Skill skill;
	NQuickPotType(Skill ids)
	{
		this.skill = ids;
	}
}
