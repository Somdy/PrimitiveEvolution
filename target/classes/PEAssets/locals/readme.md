# Instruction for Translators

* Most of the basic strings in game are stored in _zhs_ directory. If there's already an English version, you can find it in _eng_.
* For each of 4 different vanllia characters, their cards strings are stored in 4 different directories: _ironcald_, _silent_, _defect_ and _watcher_.
* For other strings in game, they are stored in _json_ files such as _keywords.json_.

### To translate the card strings
Take _defect's_ _powers_ as an example:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<LocalSets>
    <BaseTextData ID = "Defect">
        <SData branch = "44">
            <name>机器研究</name>
            <description>在你的回合内，除回合开始抽牌外，每次抽牌时，额外抽 !M! 张牌。</description>
        </SData>
        <SData branch = "45">
            <name>机器博士</name>
            <description>固有 。 NL 抽 !M! 张技能牌，这些牌本回合耗能减少1点。</description>
        </SData>
        <SData branch = "46">
            <name>机器发明</name>
            <description>固有 。 NL 抽 !M! 张能力牌，这些牌本回合耗能减少1点并获得 虚无 。</description>
        </SData>
    </BaseTextData>
</LocalSets>
```
If you have already known how xml works, it's much easier for you to translate. I will try my best to tell you how to translate it correctly, but I'm poor in English so I may speak wrong of some xml's terminology.

#### Things you should not change
* The tag names such as `LocalSets`, `BaseTextData` and `SData` are the ones you should never ever change. Changing them (even if just deleting a letter) will stop the game from loading the base data.
* The attributes such as `ID = "Defect"` and `branch = "44"` are as well the ones you should not change. They tells the game which card or character this information belongs to.

#### Things you should translate
* You should translate all texts in `name` and `description` tag which are the main text value shown on the cards. Be advised that there're some keywords in them that you should translate them the same as you do in `keywords.json`.

There's an example of translating above texts into English as following:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<LocalSets>
    <BaseTextData ID = "Defect">
        <SData branch = "44">
            <name>Machine Research</name>
            <description>Except the draw at turn-starting, you can draw !M! more cards whenever you draw during your turn.</description>
        </SData>
        <SData branch = "45">
            <name>Doctor Machine</name>
            <description>Innate. NL Draw !M! skills and make them cost 1 less this turn.</description>
        </SData>
        <SData branch = "46">
            <name>Machine Inventor</name>
            <description>Innate. NL Draw !M! powers and make them cost 1 less with Ethereal.</description>
        </SData>
    </BaseTextData>
</LocalSets>
```
I am not sure if the translation above is in STS's style or if correct in English so it will not appear in game. Hope you can do a better translation.

### To translate other strings

If you have already translated other mods before, it would be easier for you to translate the strings in _json_. Take _powers.json_ as an example:
```json
"prevolution:Bruise": {
        "NAME": "瘀伤",
        "DESCRIPTIONS": [
            "[owner_name] 额外受到 #b[amt_0] 点攻击伤害。回合结束时，瘀伤减少 #b[amt_1] 层。",
            ""
        ]
    }
```
#### Things you should not change
* You should not change the tag names (or rather ID names ? I don't know what it's in English) such as `prevolution:Bruise` and `NAME`.
* The identifiers in the text such `[owner_name]`, `[amt_0]` and the color identifier like `#b` should not be translated, too.

#### Things you should translate
* Except the identifiers you should not translate, translate the texts in `NAME` and `DESCRIPTIONS`.

There's an example of translating above texts into English as following:
```json
"prevolution:Bruise": {
        "NAME": "Bruise",
        "DESCRIPTIONS": [
            "[owner_name] takes (don't mind third-person usage if this is on player) #b[amt_0] more attack damage. At the end of its turn, Bruise is reduced by #b[amt_1] .",
            ""
        ]
    }
```
I am also not sure if the translation above is in STS's style or if correct in English so it will not appear in game. Hope you can do a better translation.

All the basic things you should know are told above I think. It may be hard to understand a card's effect if you do not know Chinese but you can easily get this card and upgrade it using _Armaments_ to get the branch so that you may understand its description more by playing it in game to see the actual effects if you know how to use `console` provided by _BaseMod_.

If you cannot understand something I say above, feel free to ask me at Workshop by leaving a comment. Thanks in advance if you can translate this mod.