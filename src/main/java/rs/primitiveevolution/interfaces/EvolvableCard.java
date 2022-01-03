package rs.primitiveevolution.interfaces;

import com.megacrit.cardcrawl.cards.AbstractCard;
import org.apache.commons.lang3.NotImplementedException;
import rs.lazymankits.interfaces.cards.BranchableUpgradeCard;
import rs.primitiveevolution.datas.DataPool;

public interface EvolvableCard extends BranchableUpgradeCard {
    int evolanch();
    
    default String getEvolvedName(int branchID) {
        if (this instanceof AbstractCard) {
            switch (((AbstractCard) this).color) {
                case RED:
                    return DataPool.IRONCLADS.getData(branchID).getName();
                case GREEN:
                    return DataPool.SILENTS.getData(branchID).getName();
                case BLUE:
                    return DataPool.DEFECTS.getData(branchID).getName();
                case PURPLE:
                    return DataPool.WATCHERS.getData(branchID).getName();
                case COLORLESS:
                    return DataPool.COLORLESS.getData(branchID).getName();
                default:
                    return null;
            }
        } else {
            throw new NotImplementedException("Not a evolvable card");
        }
    }
    
    default String getEvolvedText(int branchID) {
        if (this instanceof AbstractCard) {
            switch (((AbstractCard) this).color) {
                case RED:
                    return DataPool.IRONCLADS.getData(branchID).getText();
                case GREEN:
                    return DataPool.SILENTS.getData(branchID).getText();
                case BLUE:
                    return DataPool.DEFECTS.getData(branchID).getText();
                case PURPLE:
                    return DataPool.WATCHERS.getData(branchID).getText();
                case COLORLESS:
                    return DataPool.COLORLESS.getData(branchID).getText();
                default:
                    return null;
            }
        } else {
            throw new NotImplementedException("Not a evolvable card");
        }
    }
    
    default String getEvolvedMsg(int branchID, int slot) {
        if (this instanceof AbstractCard) {
            switch (((AbstractCard) this).color) {
                case RED:
                    return DataPool.IRONCLADS.getData(branchID).getMsg(slot);
                case GREEN:
                    return DataPool.SILENTS.getData(branchID).getMsg(slot);
                case BLUE:
                    return DataPool.DEFECTS.getData(branchID).getMsg(slot);
                case PURPLE:
                    return DataPool.WATCHERS.getData(branchID).getMsg(slot);
                case COLORLESS:
                    return DataPool.COLORLESS.getData(branchID).getMsg(slot);
                default:
                    return null;
            }
        } else {
            throw new NotImplementedException("Not a evolvable card");
        }
    }
}