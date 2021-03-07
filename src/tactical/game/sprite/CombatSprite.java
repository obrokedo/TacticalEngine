package tactical.game.sprite;

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import lombok.Getter;
import lombok.Setter;
import tactical.engine.TacticalGame;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.Range;
import tactical.game.ai.AI;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.LevelUpResult;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.constants.Direction;
import tactical.game.dev.DevHeroAI;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.hudmenu.SpriteContextPanel;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
import tactical.game.resource.HeroResource;
import tactical.game.resource.ItemResource;
import tactical.loading.ResourceManager;
import tactical.utils.AnimSprite;
import tactical.utils.Animation;
import tactical.utils.SpriteAnims;

public class CombatSprite extends AnimatedSprite
{
	private static final long serialVersionUID = 1L;
	public static final int MAXIMUM_ITEM_AMOUNT = 4;

	protected transient Color fadeColor = new Color(255, 255, 255, 255);

	@Getter @Setter protected int currentHP, maxHP,
				currentMP, maxMP,
				currentInit,
				currentSpeed, maxSpeed,
				currentMove, maxMove,
				currentAttack, maxAttack,
				currentDefense, maxDefense,
				level, exp;	

	@Getter @Setter protected AI ai;

	@Getter protected boolean isHero = false;
	@Getter protected boolean isLeader = false;
	@Getter protected boolean isPromoted = false;

	// This value provides a mean of differentiating between multiple enemies of the same name,
	// in addition this value can be user specified for enemies so that they may be the target
	// of triggers
	@Getter @Setter protected int uniqueEnemyId = -1;
	@Getter @Setter protected int clientId = 0;

	@Setter protected ArrayList<KnownSpell> spells;
	protected ArrayList<Item> items;
	@Getter protected ArrayList<Boolean> equipped;
	protected int[] usuableWeapons;
	protected int[] usuableArmor;
	@Getter protected HeroProgression heroProgression;
	// -1 when not promoted, 0 when promoted by generic, > 0 when special promotion where
	// promotionPath - 1 = index of the special promotion
	protected int promotionPath = -1;
	@Getter @Setter protected String movementType;
	@Getter protected int kills;
	@Getter protected int defeat;
	@Getter protected ArrayList<BattleEffect> battleEffects;
	@Getter protected transient Image currentWeaponImage = null;
	@Getter protected transient SpriteAnims currentWeaponAnim = null;
	protected String attackEffectId;
	protected int attackEffectChance;
	protected int attackEffectLevel;
	@Getter protected boolean drawShadow = true;
	@Getter @Setter protected transient String customMusic = null;


	/**
	 * A boolean indicating whether the combat sprite dodges or blocks attacks, dodges if true, blocks if false
	 */
	@Getter private boolean dodges;

	/**
	 * Constructor to create an enemy CombatSprite
	 */
	public CombatSprite(boolean isLeader,
			String name, String imageName, int hp, int mp, int attack, int defense, int speed, int move,
				String movementType, int level,
				int enemyId, ArrayList<KnownSpell> spells, int id,
				String attackEffectId, int attackEffectChance, int attackEffectLevel)
	{
		this(isLeader, name, imageName, null, level, 0, false, id);
		this.spells = spells;
		this.uniqueEnemyId = enemyId;
		this.isHero = false;
		this.attackEffectChance = attackEffectChance;
		this.attackEffectId = attackEffectId;
		this.attackEffectLevel = attackEffectLevel;

		// Set base states
		currentHP = hp;
		maxHP = hp;
		currentMP = mp;
		maxMP = mp;
		maxSpeed = speed;
		currentSpeed = speed;
		currentMove = move;
		maxMove = move;
		this.movementType = movementType;
		currentAttack = attack;
		maxAttack = attack;
		currentDefense = defense;
		maxDefense = defense;
		this.battleEffects = new ArrayList<>();
	}


	/**
	 * Constructor to create a hero CombatSprite
	 */
	public CombatSprite(boolean isLeader,
			String name, String imageName, HeroProgression heroProgression,
			int level, int exp, boolean promoted, int id)
	{
		super(0, 0, imageName, id);

		setInitialStatistics(isLeader, name, imageName, heroProgression, level, promoted, id);

		if (TacticalGame.TEST_MODE_ENABLED)
		{
			this.ai = new DevHeroAI(1);
			if (this.isHero && this.isLeader && !TacticalGame.BATTLE_MODE_OPTIMIZE)
			{
				this.setMaxHP(99);
				this.setMaxAttack(99);
				this.setMaxDefense(99);
				this.setMaxSpeed(99);
			}
		}
	}


	public void setInitialStatistics(boolean isLeader, String name, String imageName, HeroProgression heroProgression,
			int level, boolean promoted, int id) {
		this.isPromoted = promoted;
		// If a CombatSprite is created as promoted then it must not be on a special promotion path
		if (isPromoted)
			this.promotionPath = 0;
		this.level = level;
		this.exp = 0;
		

		dodges = true;

		this.heroProgression = heroProgression;
		
		// Stats in the progression are set up as [0] = stat progression, [1] = stat start, [2] = stat end
		if (heroProgression != null)
		{
			currentHP = maxHP = (int) this.getCurrentProgression().getHp()[1];
			currentMP = maxMP = (int) this.getCurrentProgression().getMp()[1];
			currentSpeed = maxSpeed = (int) this.getCurrentProgression().getSpeed()[1];
			currentMove = maxMove = this.getCurrentProgression().getMove();
			movementType = this.getCurrentProgression().getMovementType();
			currentAttack = maxAttack = (int) this.getCurrentProgression().getAttack()[1];
			currentDefense = maxDefense = (int) this.getCurrentProgression().getDefense()[1];
			setNonRandomStats();			
		}		

		this.isHero = true;
		this.isLeader = isLeader;
		this.name = name;
		this.imageName = imageName;
		this.items = new ArrayList<Item>();
		this.equipped = new ArrayList<Boolean>();

		if (heroProgression != null)
		{
			this.usuableWeapons = this.getCurrentProgression().getUsuableWeapons();
			this.usuableArmor = this.getCurrentProgression().getUsuableArmor();
		}

		this.battleEffects = new ArrayList<>();
		
		this.spriteType = Sprite.TYPE_COMBAT;
		this.id = id;
		this.attackEffectId = null;
	}

	

	public void setNonRandomStats() {
		this.usuableWeapons = this.getCurrentProgression().getUsuableWeapons();
		this.usuableArmor = this.getCurrentProgression().getUsuableArmor();
		setSpellsKnownByProgression();
	}


	private void setSpellsKnownByProgression() {
		// Set up spells that are currently known
		ArrayList<int[]> spellProgression = this.getCurrentProgression().getSpellLevelLearned();
		ArrayList<KnownSpell> knownSpells = new ArrayList<KnownSpell>();
		for (int i = 0; i < spellProgression.size(); i++)
		{
			// Check what spells are already known
			boolean known = false;
			int maxLevel = 0;
			for (int j = 0; j < spellProgression.get(i).length; j++)
			{
				if (spellProgression.get(i)[j] <= level)
				{
					maxLevel = j + 1;
					known = true;
				}
			}

			if (known)
				knownSpells.add(new KnownSpell(this.getCurrentProgression().getSpellIds().get(i), (byte) maxLevel));
		}
		this.spells = knownSpells;
	}

	//TODO Need to have a way to init a sprite without resetting stats
	@Override
	public void initializeSprite(ResourceManager fcrm)
	{
		super.initializeSprite(fcrm);

		drawShadow = TacticalGame.ENGINE_CONFIGURATIOR.getConfigurationValues().isAffectedByTerrain(this.movementType);

		currentAnim = spriteAnims.getCharacterAnimation("Down", this.isPromoted);

		if (spells != null && spells.size() > 0)
		{
			for (KnownSpell sd : spells)
				sd.initializeFromLoad(fcrm);
		}

		// TODO Does this work?!? We are persisting a jython object
		for (BattleEffect effect : battleEffects)
			effect.initializeAnimation(fcrm);

		//TODO Remove (all?) battle effects if this isn't an init mid battle

		for (Item item : items)
		{
			ItemResource.initializeItem(item, fcrm);
		}

		fadeColor = new Color(255, 255, 255, 255);
	}
	
	public void initializeStats()
	{	
		this.visible = true;
		this.currentAttack = this.maxAttack;
		this.currentDefense = this.maxDefense;
		this.currentSpeed = this.maxSpeed;
		if (currentHP > 0)
		{
			this.currentHP = this.maxHP;
			this.currentMP = this.maxMP;
		}
		else
		{
			currentHP = 0;
			currentMP = 0;
		}

		this.currentMove = this.maxMove;
		
		// Clear out non-persistent battle effects
		Iterator<BattleEffect> beItr = this.battleEffects.iterator();
		
		while (beItr.hasNext()) {
			BattleEffect be = beItr.next();
			if (!be.doesEffectPersistAfterBattle())
				beItr.remove();
		}
		
		if (isHero)
		{
			super.setLocX(-1, 0);
			super.setLocY(-1, 0);
		}
	
		// addBattleEffect(GlobalPythonFactory.createJBattleEffect("Burn", 1));
	}

	@Override
	public void update(StateInfo stateInfo)
	{
		super.update(stateInfo);

		if (currentHP <= 0)
		{
			if (this.getFacing() == Direction.DOWN)
				this.setFacing(Direction.RIGHT);
			else if (this.getFacing() == Direction.RIGHT)
				this.setFacing(Direction.UP);
			else if (this.getFacing() == Direction.UP)
				this.setFacing(Direction.LEFT);
			else if (this.getFacing() == Direction.LEFT)
				this.setFacing(Direction.DOWN);			
		}
	}

	@Override
	public void render(Camera camera, Graphics graphics, GameContainer cont, int tileHeight)
	{
		float xPos = this.getLocX() - camera.getLocationX();
		float yPos = this.getLocY() - camera.getLocationY() - tileHeight / 3;
		renderDirect(xPos, yPos, camera, graphics, cont, tileHeight);
	}
	
	public void renderDirect(float xPos, float yPos, Camera camera, Graphics graphics, GameContainer cont, int tileHeight) {		
		for (AnimSprite as : currentAnim.frames.get(imageIndex).sprites)
		{
			Image im = spriteAnims.getImageAtIndex(as.imageIndex);
			if (as.flipH) {
				im = im.getFlippedCopy(true, false);
			}
			if (as.flipV) {
				im = im.getFlippedCopy(false, true);
			}

			if (drawShadow)
			{
				AnimatedSprite.drawShadow(im, xPos, yPos, camera, true, tileHeight);
			}

			graphics.drawImage(im, xPos, yPos, fadeColor);
		}
	}

	/************************/
	/* Handle item stuff	*/
	/************************/
	public Item getItem(int i) {
		return items.get(i);
	}

	public int getItemsSize() {
		return items.size();
	}

	public void removeItem(Item item)
	{
		int indexOf = items.indexOf(item);
		if (equipped.get(indexOf)) {
			this.unequipItem((EquippableItem) item);
		}
		items.remove(indexOf);
		equipped.remove(indexOf);
	}

	public void addItem(Item item)
	{
		items.add(item);
		equipped.add(false);
	}
	
	public void swapItemPositions(int item1Idx, int item2Idx) {
		Item item = items.get(item1Idx);
		boolean eq = equipped.get(item1Idx);
		items.set(item1Idx, items.get(item2Idx));
		equipped.set(item1Idx, equipped.get(item2Idx));
		items.set(item2Idx, item);
		equipped.set(item2Idx, eq);
	}

	public EquippableItem getEquippedWeapon()
	{
		for (int i = 0; i < items.size(); i++)
		{
			if (items.get(i) instanceof EquippableItem && ((EquippableItem) items.get(i)).getItemType() == EquippableItem.TYPE_WEAPON && equipped.get(i))
			{
				return (EquippableItem) items.get(i);
			}
		}
		return null;
	}

	public EquippableItem getEquippedArmor()
	{
		for (int i = 0; i < items.size(); i++)
		{
			if (items.get(i) instanceof EquippableItem && ((EquippableItem) items.get(i)).getItemType() == EquippableItem.TYPE_ARMOR && equipped.get(i))
			{
				return (EquippableItem) items.get(i);
			}
		}
		return null;
	}

	public EquippableItem getEquippedRing()
	{
		for (int i = 0; i < items.size(); i++)
		{
			if (items.get(i) instanceof EquippableItem && ((EquippableItem) items.get(i)).getItemType() == EquippableItem.TYPE_RING && equipped.get(i))
			{
				return (EquippableItem) items.get(i);
			}
		}
		return null;
	}

	public boolean isEquippable(EquippableItem item)
	{
		if (item.isPromotedOnly() && !isPromoted)
			return false;
			
		
		if (EquippableItem.TYPE_WEAPON == item.getItemType())
		{
			for (int i = 0; i < usuableWeapons.length; i++)
				if (usuableWeapons[i] == item.getItemStyle())
					return true;
			return false;
		}
		else if (EquippableItem.TYPE_ARMOR == item.getItemType())
		{
			for (int i = 0; i < usuableArmor.length; i++)
				if (usuableArmor[i] == item.getItemStyle())
					return true;
			return false;
		}
		return true;
	}

	public EquippableItem equipItem(EquippableItem item)
	{
		EquippableItem oldItem = null;
		switch (item.getItemType())
		{
			case EquippableItem.TYPE_ARMOR:
				oldItem = getEquippedArmor();
				break;
			case EquippableItem.TYPE_RING:
				oldItem = getEquippedRing();
				break;
			case EquippableItem.TYPE_WEAPON:
				oldItem = getEquippedWeapon();
				break;
		}

		if (oldItem != null)
		{
			if (this.isHero)
			{
				this.unequipItem(oldItem);
				/*
				this.currentAttack -= oldItem.getAttack();
				this.currentDefense -= oldItem.getDefense();
				this.currentSpeed -= oldItem.getSpeed();
				this.maxAttack -= oldItem.getAttack();
				this.maxDefense -= oldItem.getDefense();
				this.maxSpeed -= oldItem.getSpeed();
				int index = items.indexOf(oldItem);
				this.equipped.set(index, false);
				*/
			}
		}

		// TODO Do enemies get item bonuses?
		if (this.isHero)
		{
			toggleEquipWeapon(item, true);
		}

		int index = items.lastIndexOf(item);
		this.equipped.set(index, true);

		return oldItem;
	}

	public void unequipItem(EquippableItem item)
	{
		toggleEquipWeapon(item, false);
		int index = items.indexOf(item);
		this.equipped.set(index, false);
	}

	protected void toggleEquipWeapon(EquippableItem item, boolean equip)
	{
		// Non extended stats
		this.currentAttack += ((equip ? 1 : -1) * item.getAttack());
		this.currentDefense += ((equip ? 1 : -1) * item.getDefense());
		this.currentSpeed += ((equip ? 1 : -1) * item.getSpeed());
		this.maxAttack += ((equip ? 1 : -1) * item.getAttack());
		this.maxDefense += ((equip ? 1 : -1) * item.getDefense());
		this.maxSpeed += ((equip ? 1 : -1) * item.getSpeed());
	}

	public Range getAttackRange()
	{
		EquippableItem equippedWeapon = this.getEquippedWeapon();
		if (equippedWeapon != null)
			return equippedWeapon.getRange();
		return Range.ONE_ONLY;
	}

	/*******************************************/
	/* MUTATOR AND ACCESSOR METHODS START HERE */
	/*******************************************/
	public String levelUpCustomStatistics()
	{
		return "";
	}
	
	public void setFadeAmount(int amt) {
		currentHP = amt;
		fadeColor.a = (255 + currentHP) / 255.0f;
	}

	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		fadeColor.a = alpha / 255.0f;
	}


	@Override
	public void setFacing(Direction dir)
	{
		switch (dir)
		{
			case UP:
				currentAnim = spriteAnims.getCharacterAnimation("Up", this.isPromoted);
				break;
			case DOWN:
				currentAnim = spriteAnims.getCharacterAnimation("Down", this.isPromoted);
				break;
			case LEFT:
				currentAnim = spriteAnims.getCharacterAnimation("Left", this.isPromoted);
				break;
			case RIGHT:
				currentAnim = spriteAnims.getCharacterAnimation("Right", this.isPromoted);
				break;
		}
		facing = dir;
	}

	public void setCurrentHP(int currentHP) {
		if (currentHP > 0)
			fadeColor.a = 1;
		this.alpha = 255;
		this.currentHP = currentHP;
	}

	public void modifyCurrentHP(int amount)
	{
		if (amount < 0 && this.ai != null)
			ai.setVision(Integer.MAX_VALUE);
		currentHP = Math.min(maxHP, Math.max(0, currentHP + amount));
	}

	public void modifyCurrentMP(int amount)
	{
		currentMP = Math.min(maxMP, Math.max(0, currentMP + amount));
	}

	public Animation getAnimation(String animation)
	{
		return spriteAnims.getCharacterAnimation(animation, this.isPromoted);
	}

	public boolean hasAnimation(String animation)
	{
		return spriteAnims.hasCharacterAnimation(animation, this.isPromoted);
	}

	public Image getAnimationImageAtIndex(int index)
	{
		return spriteAnims.getImageAtIndex(index);
	}

	/************************/
	/* Handle Progression	*/
	/************************/	
	public void levelUp() {
		LevelUpResult lur = getHeroProgression().getLevelUpResults(this);
		this.exp += 100;
		getHeroProgression().levelUp(this, lur);		
	}
	
	public void levelDown() {
		this.upgradeHeroToLevel(this.level - 1);
	}
	
	public void upgradeHeroToLevel(int newLevel) {
		boolean promoted = this.isPromoted;
		int proPath = this.promotionPath;
		
		resetHero();
		if (!this.isPromoted && promoted) {
			while (this.level < 10) {
				this.levelUp();
			}
			this.setPromoted(true, proPath);
		}
		
		while (this.level < newLevel)
			this.levelUp();
	}
	
	public void resetHero() {
		this.equipped.clear();
		CombatSprite cs = HeroResource.getHero(this.id);
		this.setInitialStatistics(cs.isLeader, name, imageName, 
				cs.heroProgression, cs.level, 
				// Is this a reasonable way to test for someone being promoted?
				cs.getHeroProgression().getUnpromotedProgression() == null, id);
		
		// Stats in the progression are set up as [0] = stat progression, [1] = stat start, [2] = stat end
		currentHP = maxHP = cs.getMaxHP();
		currentMP = maxMP = cs.getMaxMP();
		currentSpeed = maxSpeed = cs.getCurrentSpeed();
		currentMove = maxMove = cs.getCurrentMove();
		movementType = cs.getMovementType();
		currentAttack = maxAttack = cs.getCurrentAttack();
		currentDefense = maxDefense = cs.getCurrentDefense();		
		
		this.spells = cs.spells;
		this.items = cs.items;
		this.equipped = cs.equipped;
		
		// Add items to the combat sprite
		for (int i = 0; i < items.size(); i++)
		{
			if (equipped.get(i)) {
				// this.equipped.set(i, false);
				this.equipItem((EquippableItem) items.get(i));
			}
		}
	}

	public ArrayList<KnownSpell> getSpellsDescriptors() {
		return spells;
	}
	
	public void setPromoted(boolean isPromoted, int promotionPath) {
		this.isPromoted = isPromoted;
		this.promotionPath = promotionPath;
		this.setLevel(1);
	}

	public Progression getCurrentProgression() {
		if (isPromoted) {
			if (promotionPath == 0) {
				return heroProgression.getPromotedProgression();
			} else {
				return heroProgression.getSpecialProgressions().get(promotionPath - 1);
			}
		}
		else
			return heroProgression.getUnpromotedProgression();
	}

	public void addBattleEffect(BattleEffect battleEffect)
	{
		if (ai != null && battleEffect.isNegativeEffect())
			ai.setVision(Integer.MAX_VALUE);
		
		for (int i = 0; i < this.battleEffects.size(); i++)
		{
			BattleEffect be = this.battleEffects.get(i);

			if (be.getBattleEffectId().equalsIgnoreCase(battleEffect.getBattleEffectId()))
			{
				be.effectEnded(this);
				battleEffects.remove(i);
				i--;
			}
		}
		this.battleEffects.add(battleEffect);
	}

	public void removeBattleEffect(BattleEffect battleEffect)
	{
		Log.debug("Removed " + battleEffect.getBattleEffectId() + " from " + this.getName());
		this.battleEffects.remove(battleEffect);
	}

	public BattleEffect getAttackEffect()
	{
		BattleEffect eff = null;
		if (attackEffectId != null)
		{
			eff = TacticalGame.ENGINE_CONFIGURATIOR.getBattleEffectFactory().createEffect(attackEffectId, attackEffectLevel);
			eff.setEffectChance(attackEffectChance);
		}
		return eff;
	}

	public void setAttackEffect(String attackEffectId, int attackEffectChance, int attackEffectLevel)
	{
		this.attackEffectChance = attackEffectChance;
		this.attackEffectId = attackEffectId;
		this.attackEffectLevel = attackEffectLevel;
	}

	public void initializeBattleAttributes(ResourceManager frm)
	{
		for (BattleEffect be : this.battleEffects)
			be.initializeAnimation(frm);
		
		if (this.getEquippedWeapon() == null) {
			this.currentWeaponImage = null;
			this.currentWeaponAnim = null;
		} else if (this.getEquippedWeapon().getWeaponAnim() != null) {
			this.currentWeaponImage = null;
			this.currentWeaponAnim = frm.getSpriteAnimation(this.getEquippedWeapon().getWeaponAnim());
		} else {			
			this.currentWeaponAnim = null;
			currentWeaponImage = frm.getImage(this.getEquippedWeapon().getWeaponImage());			
		}
	}

	public void triggerButton1Event(StateInfo stateInfo)
	{
		stateInfo.sendMessage(new SpriteContextMessage(MessageType.SHOW_HERO, this));
	}

	public void triggerOverEvent(StateInfo stateInfo)
	{
		stateInfo.addPanel(new SpriteContextPanel(PanelType.PANEL_HEALTH_BAR, this, 
				TacticalGame.ENGINE_CONFIGURATIOR.getHealthPanelRenderer(),
				stateInfo.getResourceManager(), stateInfo.getPaddedGameContainer()));
	}

	public boolean isCaster() {		
		return false;
	}
	
	public String toXMLString() {
		String out = "<hero name=" + getName() + " level=" + getLevel() + 
				" exp=" + getExp() + " promoted=" + isPromoted() + " promotionPath=" + this.promotionPath + " ";
		String item = "";
		String eqp = "";
		for (int i = 0; i < this.items.size(); i++) {
			if (i == 0) {
				item = ""+ items.get(i).getItemId();
				eqp = "" + equipped.get(i);
			} else {
				item += ","+ items.get(i).getItemId();
				eqp += "," + equipped.get(i);
			}
		}
		
		out = out + "item=" + item + " eqp=" + eqp + "/>";
		return out;
	}

	@Override
	public String toString() {
		return "CombatSprite [name=" + name+ " level=" + level + ", isHero=" + isHero + ", isLeader=" + isLeader + ", isPromoted="
				+ isPromoted + ", uniqueEnemyId=" + uniqueEnemyId + ", id=" + id + ", tileX=" + this.getTileX() + ", tileY=" + this.getTileY() + "]";
	}	
}
