package rs.primitiveevolution.actions;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Frost;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.relics.GoldPlatedCables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import rs.lazymankits.abstracts.LMCustomGameAction;

import java.util.function.Predicate;

public class ImpulseOrbsAction extends LMCustomGameAction {
    private Predicate<AbstractOrb> which;
    
    public ImpulseOrbsAction(Predicate<AbstractOrb> which) {
        this.which = which;
        actionType = ActionType.SPECIAL;
        duration = startDuration = Settings.ACTION_DUR_XFAST;
    }
    
    public ImpulseOrbsAction() {
        this(o -> true);
    }
    
    @NotNull
    @Contract(" -> new")
    public static ImpulseOrbsAction Frost() {
        return new ImpulseOrbsAction(o -> o instanceof Frost);
    }
    
    @NotNull
    @Contract(" -> new")
    public static ImpulseOrbsAction Lightning() {
        return new ImpulseOrbsAction(o -> o instanceof Lightning);
    }
    
    @Override
    public void update() {
        if (duration == startDuration) {
            if (!cpr().orbs.isEmpty()) {
                for (AbstractOrb orb : cpr().orbs) {
                    if (which.test(orb)) {
                        orb.onStartOfTurn();
                        orb.onEndOfTurn();
                    }
                }
                if (cpr().hasRelic(GoldPlatedCables.ID) && which.test(cpr().orbs.get(0))) {
                    cpr().orbs.get(0).onStartOfTurn();
                    cpr().orbs.get(0).onEndOfTurn();
                }
            }
        }
        tickDuration();
    }
}