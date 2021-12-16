package rs.primitiveevolution.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import javassist.*;
import org.jetbrains.annotations.NotNull;
import rs.primitiveevolution.Nature;
import rs.primitiveevolution.interfaces.EvolvableCard;

import java.lang.reflect.Method;
import java.util.List;

public abstract class Evolution {
    public static final String EVO_ZHS = "可进化";
    public static final String EVO_ZHT = "可進化";
    public static final String EVO_ENG = "Evolvable";
    
    public static final int OnlyOnAttackingOne = 1;
    
    public static UIStrings uiStrings;
    public static String[] TEXT;
    
    public static String GetEvoKeyword() {
        Settings.GameLanguage lang = Settings.language;
        switch (lang) {
            case ZHS:
                return EVO_ZHS;
            case ZHT:
                return EVO_ZHT;
            default:
                return EVO_ENG;
        }
    }
    
    public static void Initialize() {
        uiStrings = CardCrawlGame.languagePack.getUIString(Nature.MakeID("EvolutionUI"));
        TEXT = uiStrings.TEXT;
    }
    
    public static boolean turnHasEnded() {
        return AbstractDungeon.actionManager.turnHasEnded;
    }
    
    public static String getCantUseMessage(int flag) {
        if (flag == OnlyOnAttackingOne) {
            return TEXT[10];
        }
        return "";
    }
    
    public static String getPlayHandCardUI(boolean exhaust, int times) {
        switch (Lang()) {
            case ZHS:
                return TEXT[7] + (times > 1 ? (times + TEXT[9]) : "") + (exhaust ? TEXT[8] : "");
            default:
                return TEXT[7] + (times > 1 ? (times + TEXT[9]) : "") + (exhaust ? TEXT[8] : "");
        }
    }
    
    public static String getGridToHandSelectUI(int opts, boolean anyNumber) {
        switch (Lang()) {
            case ZHS:
                return TEXT[3] + (anyNumber ? TEXT[4] : "") + opts + TEXT[5] + TEXT[6];
            default:
                return TEXT[3] + (anyNumber ? TEXT[4] : "") + opts + TEXT[5] + TEXT[6];
        }
    }
    
    public static String getIncreasePropertiesUI(int increment) {
        switch (Lang()) {
            case ZHS:
                return TEXT[0] + TEXT[2] + increment;
            default:
                return TEXT[2] + increment;
        }
    }
    
    public static String getUnplayableUI(int turns) {
        switch (Lang()) {
            case ZHS:
                return TEXT[0] + turns + TEXT[1];
            default:
                return TEXT[0] + turns + TEXT[1];
        }
    }
    
    public static String getGridUpgradeUI(int amount) {
        switch (Lang()) {
            case ZHS:
                return TEXT[3] + TEXT[11] + amount + TEXT[5];
            default:
                return TEXT[3] + amount + TEXT[5] + TEXT[11];
        }
    }
    
    private static Settings.GameLanguage Lang() {
        switch (Settings.language) {
            case ZHS:
            case ZHT:
                return Settings.GameLanguage.ZHS;
            default:
                return Settings.language;
        }
    }
    
    public static void upgradeEvolvedTexts(AbstractCard card, int branchID) {
        upgradeEvolvedName(card, branchID);
        upgradeEvolvedDescription(card, branchID);
    }
    
    public static void upgradeEvolvedDescription(AbstractCard card, int branchID) {
        if (card instanceof EvolvableCard) {
            card.rawDescription = ((EvolvableCard) card).getEvovledText(branchID);
            card.initializeDescription();
        }
    }

    public static void upgradeDescription(@NotNull AbstractCard card, String text) {
        card.rawDescription = text;
        card.initializeDescription();
    }
    
    public static void upgradeName(AbstractCard card) {
        try {
            Method upgradeName = AbstractCard.class.getDeclaredMethod("upgradeName");
            upgradeName.setAccessible(true);
            upgradeName.invoke(card);
        } catch (Exception e) {
            Nature.Log("Failed to upgrade " + card.name + "'s name");
        }
    }

    public static void upgradeEvolvedName(AbstractCard card, int branchID) {
        if (card instanceof EvolvableCard) {
            try {
                card.timesUpgraded++;
                card.upgraded = true;
                card.name = ((EvolvableCard) card).getEvovledName(branchID);
                Method initializeTitle = AbstractCard.class.getDeclaredMethod("initializeTitle");
                initializeTitle.setAccessible(true);
                initializeTitle.invoke(card);
            } catch (Exception e) {
                Nature.Log("Failed to upgrade " + card.name + "'s name to " + ((EvolvableCard) card).getEvovledName(branchID));
            }
        }
    }
    
    public static void upgradeDamage(AbstractCard card, int amt) {
        try {
            Method upgradeDamage = AbstractCard.class.getDeclaredMethod("upgradeDamage", int.class);
            upgradeDamage.setAccessible(true);
            upgradeDamage.invoke(card, amt);
        } catch (Exception e) {
            Nature.Log("Failed to upgrade " + card.name + "'s damage by " + amt);
        }
    }

    public static void setDamage(@NotNull AbstractCard card, int amt) {
        card.baseDamage = amt;
        card.upgradedDamage = true;
    }

    public static void upgradeBlock(AbstractCard card, int amt) {
        try {
            Method upgradeBlock = AbstractCard.class.getDeclaredMethod("upgradeBlock", int.class);
            upgradeBlock.setAccessible(true);
            upgradeBlock.invoke(card, amt);
        } catch (Exception e) {
            Nature.Log("Failed to upgrade " + card.name + "'s block by " + amt);
        }
    }

    public static void setBlock(@NotNull AbstractCard card, int amt) {
        card.baseBlock = amt;
        card.upgradedBlock = true;
    }

    public static void upgradeMagic(AbstractCard card, int amt) {
        try {
            Method upgradeMagic = AbstractCard.class.getDeclaredMethod("upgradeMagicNumber", int.class);
            upgradeMagic.setAccessible(true);
            upgradeMagic.invoke(card, amt);
        } catch (Exception e) {
            Nature.Log("Failed to upgrade " + card.name + "'s magic by " + amt);
        }
    }

    public static void setMagic(@NotNull AbstractCard card, int amt) {
        card.magicNumber = card.baseMagicNumber = amt;
        card.upgradedMagicNumber = true;
    }

    public static void upgradeBaseCost(AbstractCard card, int amt) {
        try {
            Method upgradeBaseCost = AbstractCard.class.getDeclaredMethod("upgradeBaseCost", int.class);
            upgradeBaseCost.setAccessible(true);
            upgradeBaseCost.invoke(card, amt);
        } catch (Exception e) {
            Nature.Log("Failed to upgrade " + card.name + "'s base cost to " + amt);
        }
    }
    
    public static void setBaseCost(@NotNull AbstractCard card, int amt) {
        int diff = card.costForTurn - card.cost;
        card.cost = amt;
        card.costForTurn = card.cost + diff;
        card.upgradedCost = true;
    }
    
    public static void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }
    
    public static void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }
    
    public static boolean ShouldRecallOnSL(AbstractCard card) {
        return card instanceof EvolvableCard && ((EvolvableCard) card).chosenBranch() != ((EvolvableCard) card).defaultBranch();
    }
    
    public static boolean manipulateMethods(Class<?> clazz, CtBehavior ctMethodToPatch) {
        try {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctEvoCard = pool.get(EvolvableCard.class.getName());
            ctClass.addInterface(ctEvoCard);
            CtClass ctEvolanchType = pool.get(int.class.getName());
            CtClass ctBranchType = pool.get(List.class.getName());
            CtClass ctUpgradeType = pool.get(void.class.getName());
            CtClass ctRecallType = pool.get(boolean.class.getName());
            CtMethod evolanch = CtNewMethod.make(ctEvolanchType, "evolanch", new CtClass[0], null,
                    "return " + clazz.getName() + ".branchID(this);", ctClass);
            CtMethod branches = CtNewMethod.make(ctBranchType, "possibleBranches", new CtClass[0], null,
                    "return " + clazz.getName() + ".branches(this);", ctClass);
            CtMethod upgrade = CtNewMethod.make(ctUpgradeType, "upgradeCalledOnSL", new CtClass[0], null,
                    "return " + clazz.getName() + ".upgrade(this);", ctClass);
            CtMethod recall = CtNewMethod.make(ctRecallType, "callUpgradeOnSL", new CtClass[0], null, 
                    "return " + ctEvoCard.getPackageName() + ".EvolvableCard.super.callUpgradeOnSL() && "
                            + Evolution.class.getName() + ".ShouldRecallOnSL(this);", ctClass);
            ctClass.addMethod(evolanch);
            ctClass.addMethod(branches);
            ctClass.addMethod(upgrade);
            ctClass.addMethod(recall);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}