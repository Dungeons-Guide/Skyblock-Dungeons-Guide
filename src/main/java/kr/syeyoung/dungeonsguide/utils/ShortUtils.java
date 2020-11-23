package kr.syeyoung.dungeonsguide.utils;

public class ShortUtils {
    public static short rotateCounterClockwise(short integer) {
        int res = 0;
        for(int i=0; i<16; i++){
            int x = i % 4;
            int y = i / 4;
            res |= (integer >> i & 0x1) << ((4-x-1)*4 + y);
        }
        return (short) (res & 0xFFFF);
    }
    public static short rotateClockwise(short integer) {
        int res = 0;
        for(int i=0; i<16; i++){
            int x = i % 4;
            int y = i / 4;
            res |= (integer >> i & 0x1) << (x *4 +(4 - y - 1));
        }
        return (short) (res & 0xFFFF);
    }

    public static short topLeftifyInt(short integer) {
        int it = integer & 0xFFFF;
        while ((it & (0x1111)) == 0) it >>= 1;
        while ((it & (0xF)) == 0) it >>= 4;
        return (short) (it & 0xFFFF);
    }
}
