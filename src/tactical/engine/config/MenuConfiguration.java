package tactical.engine.config;

public interface MenuConfiguration {
	public String getPriestSavingText();
	
	public String getPriestNoOneToCureText();
	public int getPriestCureCost(String[] battleEffectNames, int[] battleEffectLevels);
	public String getPriestSelectSomeoneToCureText(String targetName, String[] ailments, int cost);
	public String getPriestNotEnoughGoldToCureText();
	public String getPriestTargetHasBeenCuredText(String targetName);
	
	public String getPriestNoOneToPromoteText();
	public String getPriestSelectSomeoneToPromoteText(String targetName, String targetClass, String itemUsed);
	public String getPriestTargetHasBeenPromotedText(String targetName, String targetClass, String itemUsed);
	
	public String getPriestNoOneToResurrectText();
	public int getPriestResurrectCost(int level, boolean promoted);
	public String getPriestSelectSomeoneToResurrectText(String targetName, int cost);
	public String getPriestNotEnoughGoldToResurrectText();
	public String getPriestTargetHasBeenResurrectedText(String targetName);
	
	public String getPriestSaveText();
	
	public String getPriestMenuClosedText();
	
	
	public String getShopMenuClosedText();
	public String getShopNoDealsText();
	public String getShopPromptSellDealText(String itemName, String cost);
	public String getShopPromptSellNormalText(String itemName, String cost);
	public String getShopPromptRepairBrokenText(String itemName, String cost);
	public String getShopPromptRepairDamagedText(String itemName, String cost);
	public String getShopItemRepairedText();
	public String getShopItemNotDamagedText(String itemName);
	public String getShopTransactionCancelledText();
	public String getShopRepairCancelledText();
	public String getShopTransactionSuccessfulText();
	public String getShopNotEnoughGoldText();
	public String getShopLookAtDealsText();
	public String getShopLookAtNormalText();
	public String getShopPromptPurchaseCostText(String itemName, String cost);
	public String getShopPromptWhoGetsItemText();
	public String getShopPromptEquipItNowText();
	public String getShopCantCarryMoreText(String personName);
	public String getShopNoMoreDealsText();
	public String getShopPromptSellOldText(String itemName, String cost);
	
	public String getNoItemInChestText();
	public String getItemInChestText(String itemName);
	public String getItemInChestTextNoRoom(String itemName);
	
	public String getItemRecievedText(String heroName, String itemName);
	
	public String getGiveToWhoText(String heroName, String itemName);
	public String getGiveSuccessText(String giverName, String itemName, String targetName);
	public String getDropConfirmText(String itemName);
	public String getDropSuccessText(String itemName);
	public String getUseTargetText(String itemName);
	public String getUseFailedText(String heroName, String itemName);
}
