package rs.primitiveevolution.actions;

import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Dark;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Lightning;
import rs.lazymankits.abstracts.LMCustomGameAction;
import rs.primitiveevolution.orbs.DarkOL;
import rs.primitiveevolution.orbs.FrostOL;
import rs.primitiveevolution.orbs.LightningOL;

public class ChannelOverloadedOrbAction extends LMCustomGameAction {
    private AbstractOrb orb;
    
    public ChannelOverloadedOrbAction(OrbType orbType, int evoke, int passive, boolean defaultamt) {
        setOrb(orbType, evoke, passive, defaultamt);
    }
    
    public ChannelOverloadedOrbAction(OrbType orbType) {
        this(orbType, 0, 0, true);
    }
    
    public ChannelOverloadedOrbAction(AbstractOrb relative, int evoke, int passive, boolean defaultamt) {
        OrbType type = getRelativeType(relative);
        setOrb(type, evoke, passive, defaultamt);
    }
    
    public ChannelOverloadedOrbAction(AbstractOrb relative) {
        this(relative, 0, 0, true);
    }
    
    private void setOrb(OrbType orbType, int evoke, int passive, boolean defaultamt) {
        switch (orbType) {
            case Lightning:
                orb = defaultamt ? new LightningOL() : new LightningOL(evoke, passive);
                break;
            case Frost:
                orb = defaultamt ? new FrostOL() : new FrostOL(evoke, passive);
                break;
            case Dark:
                orb = defaultamt ? new DarkOL() : new DarkOL(evoke, passive);
                break;
            default:
                orb = null;
        }
    }
    
    private OrbType getRelativeType(AbstractOrb from) {
        if (from instanceof Lightning || from instanceof LightningOL)
            return OrbType.Lightning;
        if (from instanceof Frost || from instanceof FrostOL)
            return OrbType.Frost;
        if (from instanceof Dark || from instanceof DarkOL)
            return OrbType.Dark;
        return OrbType.None;
    }
    
    public enum OrbType {
        Lightning, Frost, Dark, None
    }
    
    @Override
    public void update() {
        isDone = true;
        addToTop(new ChannelAction(orb));
    }
}