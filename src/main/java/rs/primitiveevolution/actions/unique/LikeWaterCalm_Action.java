package rs.primitiveevolution.actions.unique;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class LikeWaterCalm_Action extends AbstractGameAction {
    @Override
    public void update() {
        if (AbstractDungeon.player.stance.ID.equals("Wrath")) {
            addToTop(new ChangeStanceAction("Calm"));
        }
        this.isDone = true;
    }
}
