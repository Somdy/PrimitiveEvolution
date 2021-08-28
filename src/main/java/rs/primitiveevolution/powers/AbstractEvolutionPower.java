package rs.primitiveevolution.powers;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import rs.lazymankits.abstracts.LMCustomPower;
import rs.primitiveevolution.Nature;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractEvolutionPower extends LMCustomPower {
    private static final PowerStrings MountPwrStrings = CardCrawlGame.languagePack.getPowerStrings(Nature.MakeID("EvolutionPower"));
    private static final Pattern NamePattern = Pattern.compile("\\[(owner_name)]", Pattern.MULTILINE);
    private static final Pattern AmtPattern = Pattern.compile("\\[(amt_(\\d))]", Pattern.MULTILINE);
    private static final Pattern CrtPattern = Pattern.compile("\\[(crt_(\\d))]", Pattern.MULTILINE);

    public final PowerStrings powerStrings;
    protected String NAME;
    protected String[] DESCRIPTIONS;
    protected String owner_name;
    protected String[] crt_names;
    protected String[] amts;
    
    public AbstractEvolutionPower(String POWER_ID, String img, PowerType type, AbstractCreature owner) {
        powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
        NAME = powerStrings.NAME;
        DESCRIPTIONS = powerStrings.DESCRIPTIONS;
        ID = POWER_ID;
        name = NAME;
        this.type = type;
        crt_names = new String[10];
        amts = new String[10];
        Arrays.fill(crt_names, "missing_name");
        Arrays.fill(amts, "missing_value");
        this.owner = owner;
        loadImg(img);
    }

    @Override
    protected void loadImg(String name) {
        if (name == null)
            return;
        region128 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("PEAssets/images/powers/128/" + name + ".png"), 
                0, 0, 128, 128);
        region48 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage("PEAssets/images/powers/48/" + name + ".png"),
                0, 0, 48, 48);
    }

    protected void setValues(int amount) {
        super.setValues(owner, null, amount);
        setOwnerName();
    }

    protected void setValues(int amount, int extraAmt) {
        super.setValues(owner, null, amount, extraAmt);
        setOwnerName();
    }

    protected void setValues(AbstractCreature source, int amount) {
        super.setValues(owner, source, amount);
        setOwnerName();
    }

    protected void setValues(AbstractCreature source, int amount, int extraAmt) {
        super.setValues(owner, source, amount, extraAmt);
        setOwnerName();
    }

    protected void setOwnerName() {
        owner_name = owner.isPlayer ? MountPwrStrings.DESCRIPTIONS[0] : owner.name;
    }

    protected void setAmtValue(int slot, int value) {
        if (slot > amts.length - 1)
            slot = amts.length - 1;
        amts[slot] = String.valueOf(value);
    }

    protected void setAmtValue(int slot, float value) {
        if (slot > amts.length - 1)
            slot = amts.length - 1;
        amts[slot] = String.valueOf(value);
    }
    
    protected void setCrtName(int slot, String value) {
        if (slot > crt_names.length - 1)
            slot = crt_names.length - 1;
        crt_names[slot] = value;
    }

    @Override
    public void updateDescription() {
        description = preSetDescription();
        description = checkWithPatterns(description);
    }

    protected String checkWithPatterns(String origin) {
        origin = checkOwnerName(origin);
        origin = checkAmtValue(origin);
        origin = checkCrtValue(origin);
        return origin;
    }

    private String checkAmtValue(String origin) {
        final Matcher matcher = AmtPattern.matcher(origin);
        while (matcher.find() && matcher.groupCount() >= 2) {
            int slot = Integer.parseInt(matcher.group(2));
            if (slot <= amts.length - 1) {
                origin = origin.replace(matcher.group(0), amts[slot]);
            }
        }
        return origin;
    }
    
    private String checkCrtValue(String origin) {
        final Matcher matcher = CrtPattern.matcher(origin);
        while (matcher.find() && matcher.groupCount() >= 2) {
            int slot = Integer.parseInt(matcher.group(2));
            if (slot <= crt_names.length - 1) {
                origin = origin.replace(matcher.group(0), crt_names[slot]);
            }
        }
        return origin;
    }

    private String checkOwnerName(String origin) {
        final Matcher matcher = NamePattern.matcher(origin);
        if (matcher.find())
            origin = origin.replace(matcher.group(0), owner_name);
        return origin;
    }

    @Override
    protected TextureAtlas getPowerAtlas() {
        return null;
    }

    public abstract String preSetDescription();
}