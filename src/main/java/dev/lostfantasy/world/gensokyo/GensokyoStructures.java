package dev.lostfantasy.world.gensokyo;

/** One immutable plan per generator; no world state is retained by the architecture. */
public final class GensokyoStructures {
    private GensokyoStructures() {}
    public static GensokyoBlueprint create() {
        GensokyoBlueprint plan=new GensokyoBlueprint();
        HakureiShrine.build(plan);
        FairyTreeHomes.build(plan,GensokyoAtlas.FAIRY_TREE,false);
        FairyTreeHomes.build(plan,GensokyoAtlas.FAIRY_OLD_TREE,true);
        ScarletMansion.build(plan);
        ForestHomes.marisa(plan);
        ForestHomes.alice(plan);
        Kourindou.build(plan);
        MystiaStall.build(plan);
        Suzunaan.build(plan);
        Terakoya.build(plan);
        HiedaEstate.build(plan);
        VillageHomes.build(plan);
        RicePaddies.build(plan);
        TanukiClearing.build(plan);
        MyourenTemple.build(plan);
        MausoleumEntrance.build(plan);
        PalanquinShip.build(plan);
        ShiningNeedleCastle.build(plan);
        Eientei.build(plan);
        MokouHome.build(plan);
        MoriyaShrine.build(plan);
        MountainCascade.build(plan);
        CucumberFactory.build(plan);
        KappaWorkshop.build(plan);
        GenbuCaves.build(plan);
        TenguSettlement.build(plan);
        NemunoHome.build(plan);
        RainbowMine.build(plan);
        UndergroundEntrances.build(plan);
        GeyserCenter.build(plan);
        AquaticMarket.build(plan);
        YamawaroBase.build(plan);
        KomakusaDen.build(plan);
        KasenEntrance.build(plan);
        PeonyField.build(plan);
        MayohigaVillage.build(plan);
        ToadPond.build(plan);
        BoundaryEntrance.build(plan);
        GhostHouse.build(plan);
        FairySignalTower.build(plan);
        Muenzuka.build(plan);
        LarvaHollow.build(plan);
        FlowerLandscapes.build(plan);
        CirnoIceHouse.build(plan);
        PrismriverMansion.build(plan);
        Ropeway.build(plan);
        NetherworldEntrance.build(plan);
        LiminalMarket.build(plan);SanzuLanding.build(plan);
        plan.seal();
        return plan;
    }
}
