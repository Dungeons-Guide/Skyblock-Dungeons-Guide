/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.utils.cursor;

import com.google.common.io.LittleEndianDataInputStream;
import com.twelvemonkeys.imageio.plugins.bmp.CURImageReader;
import lombok.Data;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CursorReader {
    public static List<CursorData> readFromInputStream(InputStream inputStream) throws IOException {
        LittleEndianDataInputStream dataInputStream = new LittleEndianDataInputStream(new BufferedInputStream(inputStream));
        dataInputStream.mark(Integer.MAX_VALUE);


        int magicValue = dataInputStream.readUnsignedShort();
        if (magicValue != 0) throw new RuntimeException("Invalid Cursor file");
        int type = dataInputStream.readUnsignedShort();
        if (type != 2) throw new RuntimeException("not cursor");
        int size = dataInputStream.readShort();

        List<CursorData> directoryList = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            CursorData directory = new CursorData();
            directory.setWidth((short) dataInputStream.readUnsignedByte());
            directory.setHeight((short) dataInputStream.readUnsignedByte());
            directory.setColorCnt((short) dataInputStream.readUnsignedByte());
            directory.setMagicValue(dataInputStream.readByte());
            directory.setXHotSpot(dataInputStream.readShort());
            directory.setYHotSpot(dataInputStream.readShort());
            directory.setSizeBitmap(dataInputStream.readInt() & 0x00000000ffffffffL);
            directory.setOffset(dataInputStream.readInt() & 0x00000000ffffffffL);

            directoryList.add(directory);
        }
        dataInputStream.reset();

        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(dataInputStream)) {
            CURImageReader imageReader = new CURImageReader();
            imageReader.setInput(imageInputStream);

            for (int i = 0; i < directoryList.size(); i++) {
                directoryList.get(i).setBufferedImage(imageReader.read(i));
            }
        }
        inputStream.close();

        return directoryList;
    }

    private static void setIntLittleEndian(byte[] bytes, int index, int value) {
        byte[] ins = new byte[] {
                (byte) ((value >> 24)& 0xFF), (byte) ((value >> 16)& 0xFF), (byte) ((value >> 8)& 0xFF), (byte) (value & 0xFF)
        };
        bytes[index+3] = ins[0];
        bytes[index+2] = ins[1];
        bytes[index+1] = ins[2];
        bytes[index] = ins[3];
    }


    @Data
    public static class CursorData {
        private short width, height, colorCnt, magicValue;
        private int xHotSpot, yHotSpot;
        private long sizeBitmap;
        private long offset;
        private BufferedImage bufferedImage;
    }
}
