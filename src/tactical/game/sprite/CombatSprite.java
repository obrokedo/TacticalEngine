package tactical.game.sprite;

import java.util.ArrayList;
import java.util.Iterator;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import tactical.engine.TacticalGame;
import tactical.engine.message.MessageType;
import tactical.engine.message.SpriteContextMessage;
import tactical.engine.state.StateInfo;
import tactical.game.Camera;
import tactical.game.Range;
import tactical.game.ai.AI;
import tactical.game.battle.BattleEffect;
import tactical.game.battle.spell.KnownSpell;
import tactical.game.constants.Direction;
import tactical.game.dev.DevHeroAI;
import tactical.game.hudmenu.Panel.PanelType;
import tactical.game.hudmenu.SpriteContextPanel;
import tactical.game.item.EquippableItem;
import tactical.game.item.Item;
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

	protected int currentHP, maxHP,
				currentMP, maxMP,
				currentInit,
				currentSpeed, maxSpeed,
				currentMove, maxMove,
				currentAttack, maxAttack,
				currentDefense, maxDefense,
				level, exp;	

	protected AI ai;

	protected boolean isHero = false;
	protected boolean isLeader = false;
	protected boolean isPromoted = false;

	// This value provides a mean of differentiating between multiple enemies of the same name,
	// in addition this value can be user specified for enemies so that they may be the target
	// of triggers
	protected int uniqueEnemyId = -1;
	protected int clientId = 0;

	protected ArrayList<KnownSpell> spells;
	protected ArrayList<Item> items;
	protected ArrayList<Boolean> equipped;
	protected int[] usuableWeapons;
	protected int[] usuableArmor;
	protected HeroProgression heroProgression;
	// -1 when not promoted, 0 when promoted by generic, > 0 when special promotion where
	// promotionPath - 1 = index of the special promotion
	protected int promotionPath = -1;
	protected String movementType;
	protected int kills;
	protected int defeat;
	protected ArrayList<BattleEffect> battleEffects;
	protected transient Image currentWeaponImage = null;
	protected transient SpriteAnims currentWeaponAnim = null;
	protected String attackEffectId;
	protected int attackEffectChance;
	protected int attackEffectLevel;
	protected boolean drawShadow = true;
	protected transient String customMusic = null;


	/**
	 * A boolean indicating whether the combat sprite dodges or blocks attacks, dodges if true, blocks if false
	 */
	private boolean dodges;

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

		this.isPromoted = promoted;
		// If a CombatSprite is created as promoted then it must not be on a special promotion path
		if (isPromoted)
			this.promotionPath = 0;
		this.level = level;
		this.exp = 0;


		dodges = true;

		this.heroProgression = heroProgression;

		// Handle attribute strengths for heroes
		if (heroProgression != null)
		{
			// Stats in the progression are set up as [0] = stat progression, [1] = stat start, [2] = stat end
			currentHP = maxHP = (int) this.getCurrentProgression().getHp()[1];
			maxMP = (int) this.getCurrentProgression().getMp()[1];
			maxSpeed = (int) this.getCurrentProgression().getSpeed()[1];
			maxMove = this.getCurrentProgression().getMove();
			movementType = this.getCurrentProgression().getMovementType();
			maxAttack = (int) this.getCurrentProgression().getAttack()[1];
			maxDefense = (int) this.getCurrentProgression().getDefense()[1];

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

	public ArrayList<Boolean> getEquipped() {
		return equipped;
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
	
	public void asetFadeAmount(int amt) {
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

	public int getCurrentHP() {
		return currentHP;
	}

	public void setCurrentHP(int currentHP) {
		if (currentHP > 0)
			fadeColor.a = 1;
		this.currentHP = currentHP;
	}

	public int getMaxHP() {
		return this.maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}

	public int getCurrentMP() {
		return currentMP;
	}

	public void setCurrentMP(int currentMP) {
		this.currentMP = currentMP;
	}

	public void modifyCurrentHP(int amount)
	{
		currentHP = Math.min(maxHP, Math.max(0, currentHP + amount));
	}

	public void modifyCurrentMP(int amount)
	{
		currentMP = Math.min(maxMP, Math.max(0, currentMP + amount));
	}

	public int getMaxMP() {
		return maxMP;
	}

	public void setMaxMP(int maxMP) {
		this.maxMP = maxMP;
	}

	public int getCurrentInit() {
		return currentInit;
	}

	public void setCurrentInit(int currentInit) {
		this.currentInit = currentInit;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getMaxAttack() {
		return maxAttack;
	}

	public void setMaxAttack(int maxAttack) {
		this.maxAttack = maxAttack;
	}

	public int getMaxDefense() {
		return maxDefense;
	}

	public void setMaxDefense(int maxDefense) {
		this.maxDefense = maxDefense;
	}

	public int getCurrentAttack() {
		return currentAttack;
	}

	public void setCurrentAttack(int currentAttack) {
		this.currentAttack = currentAttack;
	}

	public int getCurrentDefense() {
		return currentDefense;
	}

	public void setCurrentDefense(int currentDefense) {
		this.currentDefense = currentDefense;
	}

	public boolean isDodges() {
		return dodges;
	}

	public void setDodges(boolean dodges) {
		this.dodges = dodges;
	}

	public int getKills() {
		return kills;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getDefeat() {
		return defeat;
	}

	public void setDefeat(int defeat) {
		this.defeat = defeat;
	}

	public AI getAi() {
		return ai;
	}

	public void setAi(AI ai)
	{
		this.ai = ai;
	}

	public int getCurrentMove() {
		return currentMove;
	}

	public void setCurrentMove(int currentMove) {
		this.currentMove = currentMove;
	}

	public int getMaxMove() {
		return maxMove;
	}

	public void setMaxMove(int maxMove) {
		this.maxMove = maxMove;
	}

	public String getMovementType() {
		return movementType;
	}

	public void setMovementType(String characterMovementType) {
		this.movementType = characterMovementType;
	}

	public boolean isHero() {
		return isHero;
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

	public int getUniqueEnemyId()
	{
		return this.uniqueEnemyId;
	}

	public void setUniqueEnemyId(int id)
	{
		this.uniqueEnemyId = id;
	}

	public boolean isLeader() {
		return isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
	}

	/************************/
	/* Handle Progression	*/
	/************************/
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public ArrayList<KnownSpell> getSpellsDescriptors() {
		return spells;
	}

	public void setSpells(ArrayList<KnownSpell> spells) {
		this.spells = spells;
	}

	public boolean isPromoted() {
		return isPromoted;
	}

	public void setPromoted(boolean isPromoted, int promotionPath) {
		this.isPromoted = isPromoted;
		this.promotionPath = promotionPath;
		this.setLevel(1);
	}

	public HeroProgression getHeroProgression() {
		return heroProgression;
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

	public ArrayList<BattleEffect> getBattleEffects() {
		return battleEffects;
	}

	public void addBattleEffect(BattleEffect battleEffect)
	{
		
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

	public Image getCurrentWeaponImage() {
		return currentWeaponImage;
	}

	public SpriteAnims getCurrentWeaponAnim() {
		return currentWeaponAnim;
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

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}


	/**
	 * Returns a boolean indicating whether this CombatSprite should have a shadow drawn for it
	 * and by extension whether the battle platform should be displayed for it
	 * 
	 * @return a boolean indicating whether this CombatSprite should have a shadow drawn for it
	 * and by extension whether the battle platform should be displayed for it
	 */
	public boolean isDrawShadow() {
		return drawShadow;
	}

	public String getCustomMusic() {
		return customMusic;
	}


	public void setCustomMusic(String customMusic) {
		this.customMusic = customMusic;
	}


	@Override
	public String toString() {
		return "CombatSprite [name=" + name+ " level=" + level + ", isHero=" + isHero + ", isLeader=" + isLeader + ", isPromoted="
				+ isPromoted + ", uniqueEnemyId=" + uniqueEnemyId + ", id=" + id + ", tileX=" + this.getTileX() + ", tileY=" + this.getTileY() + "]";
	}	
}
