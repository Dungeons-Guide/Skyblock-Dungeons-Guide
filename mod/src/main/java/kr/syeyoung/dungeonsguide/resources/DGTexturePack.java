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

package kr.syeyoung.dungeonsguide.resources;

import kr.syeyoung.dungeonsguide.launcher.authentication.Authenticator;
import lombok.AllArgsConstructor;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

@AllArgsConstructor
public class DGTexturePack implements IResourcePack {

    private final Authenticator authenticator;

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/assets/dg/"+location.getResourcePath());
        if (inputStream != null) return inputStream;
        return new ByteArrayInputStream(authenticator.getResources().get("assets/dg/"+location.getResourcePath()));
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        return authenticator.getResources().containsKey("assets/dg/"+location.getResourcePath())
        || this.getClass().getResourceAsStream("/assets/dg/"+location.getResourcePath()) != null;
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("dungeonsguide");
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return new BufferedImage(512,512, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public String getPackName() {
        return "Dungeons Guide Default Pack";
    }
}
