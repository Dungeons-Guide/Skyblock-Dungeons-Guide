package kr.syeyoung.modapi.rendering;

public interface UFontCalculator extends FontMetrics {
    public int getFontHeight();
    public int getStringWidth(String text);
}
