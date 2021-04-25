package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.features.impl.party.api.Skill;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

public class XPUtils {
    @Data
    public static class XPCalcResult {
        private int level;
        private double remainingXp;
        private double nextLvXp;
    }

    private static TreeMap<Double, Integer> catacombXp = new TreeMap<>();
    private static TreeMap<Double, Integer> skillXp = new TreeMap<>();
    private static TreeMap<Double, Integer> skillXp2 = new TreeMap<>();
    static {
        catacombXp.put(50.0, 1);
        catacombXp.put(125.0, 2);
        catacombXp.put(235.0, 3);
        catacombXp.put(395.0, 4);
        catacombXp.put(625.0, 5);
        catacombXp.put(955.0, 6);
        catacombXp.put(1425.0, 7);
        catacombXp.put(2095.0, 8);
        catacombXp.put(3045.0, 9);
        catacombXp.put(4385.0, 10);
        catacombXp.put(6275.0, 11);
        catacombXp.put(8940.0, 12);
        catacombXp.put(12700.0, 13);
        catacombXp.put(17960.0, 14);
        catacombXp.put(25340.0, 15);
        catacombXp.put(35640.0, 16);
        catacombXp.put(50040.0, 17);
        catacombXp.put(70040.0, 18);
        catacombXp.put(97640.0, 19);
        catacombXp.put(135640.0, 20);
        catacombXp.put(188140.0, 21);
        catacombXp.put(259640.0, 22);
        catacombXp.put(356640.0, 23);
        catacombXp.put(488640.0, 24);
        catacombXp.put(668640.0, 25);
        catacombXp.put(911640.0, 26);
        catacombXp.put(1239640.0, 27);
        catacombXp.put(1684640.0, 28);
        catacombXp.put(2284640.0, 29);
        catacombXp.put(3084640.0, 30);
        catacombXp.put(4149640.0, 31);
        catacombXp.put(5559640.0, 32);
        catacombXp.put(7459640.0, 33);
        catacombXp.put(9959640.0, 34);
        catacombXp.put(13259640.0, 35);
        catacombXp.put(17559640.0, 36);
        catacombXp.put(23159640.0, 37);
        catacombXp.put(30359640.0, 38);
        catacombXp.put(39559640.0, 39);
        catacombXp.put(51559640.0, 40);
        catacombXp.put(66559640.0, 41);
        catacombXp.put(85559640.0, 42);
        catacombXp.put(109559640.0, 43);
        catacombXp.put(139559640.0, 44);
        catacombXp.put(177559640.0, 45);
        catacombXp.put(225559640.0, 46);
        catacombXp.put(285559640.0, 47);
        catacombXp.put(360559640.0, 48);
        catacombXp.put(453559640.0, 49);
        catacombXp.put(569809640.0, 50);
        skillXp.put(0.0, 0);
        skillXp.put(50.0, 1);
        skillXp.put(175.0, 2);
        skillXp.put(375.0, 3);
        skillXp.put(675.0, 4);
        skillXp.put(1175.0, 5);
        skillXp.put(1925.0, 6);
        skillXp.put(2925.0, 7);
        skillXp.put(4425.0, 8);
        skillXp.put(6425.0, 9);
        skillXp.put(9925.0, 10);
        skillXp.put(14925.0, 11);
        skillXp.put(22425.0, 12);
        skillXp.put(32425.0, 13);
        skillXp.put(47425.0, 14);
        skillXp.put(67425.0, 15);
        skillXp.put(97425.0, 16);
        skillXp.put(147425.0, 17);
        skillXp.put(222425.0, 18);
        skillXp.put(322425.0, 19);
        skillXp.put(522425.0, 20);
        skillXp.put(822425.0, 21);
        skillXp.put(1222425.0, 22);
        skillXp.put(1722425.0, 23);
        skillXp.put(2322425.0, 24);
        skillXp.put(3022425.0, 25);
        skillXp.put(3822425.0, 26);
        skillXp.put(4722425.0, 27);
        skillXp.put(5722425.0, 28);
        skillXp.put(6822425.0, 29);
        skillXp.put(8022425.0, 30);
        skillXp.put(9322425.0, 31);
        skillXp.put(10722425.0, 32);
        skillXp.put(12222425.0, 33);
        skillXp.put(13722425.0, 34);
        skillXp.put(15522425.0, 35);
        skillXp.put(17322425.0, 36);
        skillXp.put(19222425.0, 37);
        skillXp.put(21222425.0, 38);
        skillXp.put(23322425.0, 39);
        skillXp.put(25522425.0, 40);
        skillXp.put(27822425.0, 41);
        skillXp.put(30222425.0, 42);
        skillXp.put(32722425.0, 43);
        skillXp.put(35322425.0, 44);
        skillXp.put(38072425.0, 45);
        skillXp.put(40972425.0, 46);
        skillXp.put(44072425.0, 47);
        skillXp.put(47472425.0, 48);
        skillXp.put(51172425.0, 49);
        skillXp.put(55172425.0, 50);
        skillXp.put(59472425.0, 51);
        skillXp.put(64072425.0, 52);
        skillXp.put(68972425.0, 53);
        skillXp.put(74172425.0, 54);
        skillXp.put(79672425.0, 55);
        skillXp.put(85472425.0, 56);
        skillXp.put(91572425.0, 57);
        skillXp.put(97972425.0, 58);
        skillXp.put(104672425.0, 59);
        skillXp.put(111672425.0, 60);
        skillXp2.put(0.0, 0);
        skillXp2.put(50.0, 1);
        skillXp2.put(150.0, 2);
        skillXp2.put(275.0, 3);
        skillXp2.put(435.0, 4);
        skillXp2.put(635.0, 5);
        skillXp2.put(885.0, 6);
        skillXp2.put(1200.0, 7);
        skillXp2.put(1600.0, 8);
        skillXp2.put(2100.0, 9);
        skillXp2.put(2725.0, 10);
        skillXp2.put(3510.0, 11);
        skillXp2.put(4510.0, 12);
        skillXp2.put(5760.0, 13);
        skillXp2.put(7324.0, 14);
        skillXp2.put(9325.0, 15);
        skillXp2.put(11825.0, 16);
        skillXp2.put(14950.0, 17);
        skillXp2.put(18950.0, 18);
        skillXp2.put(23950.0, 19);
        skillXp2.put(30200.0, 20);
        skillXp2.put(38050.0, 21);
        skillXp2.put(47850.0, 22);
        skillXp2.put(60100.0, 23);
        skillXp2.put(75400.0, 24);
        skillXp2.put(94450.0, 25);
    }

    public static XPCalcResult getCataXp(double totalXp) {
        Map.Entry<Double, Integer> totalXpEn = catacombXp.floorEntry(totalXp);
        XPCalcResult xpCalcResult = new XPCalcResult();
        xpCalcResult.level = totalXpEn.getValue();
        xpCalcResult.remainingXp = totalXp - totalXpEn.getKey();
        Map.Entry<Double, Integer> asdasd = catacombXp.ceilingEntry(totalXp);
        xpCalcResult.nextLvXp = asdasd == null ? 0 : asdasd.getKey();
        return xpCalcResult;
    }
    public static XPCalcResult getSkillXp(Skill skill, double totalXp) {
        switch(skill) {
            case RUNECRAFTING:
                Map.Entry<Double, Integer> totalXpEn = skillXp2.floorEntry(totalXp);
                XPCalcResult xpCalcResult = new XPCalcResult();
                xpCalcResult.level = totalXpEn.getValue();
                xpCalcResult.remainingXp = totalXp - totalXpEn.getKey();
                Map.Entry<Double, Integer> asdasd = skillXp2.ceilingEntry(totalXp);
                xpCalcResult.nextLvXp = asdasd == null ? 0 : asdasd.getKey();
                return xpCalcResult;
            default:
                totalXpEn = skillXp.floorEntry(totalXp);
                xpCalcResult = new XPCalcResult();
                xpCalcResult.level = totalXpEn.getValue();
                xpCalcResult.remainingXp = totalXp - totalXpEn.getKey();
                asdasd = skillXp.ceilingEntry(totalXp);
                xpCalcResult.nextLvXp = asdasd == null ? 0 : asdasd.getKey();
                return xpCalcResult;
        }
    }
}
