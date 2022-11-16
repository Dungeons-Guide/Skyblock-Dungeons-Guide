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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.interfacestruct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.NativeGameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordEntitlement;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordSku;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordStruct;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordResult;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordSnowflake;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.Int32;

import java.util.Arrays;
import java.util.List;

public class IDiscordStoreManager extends DiscordStruct { public IDiscordStoreManager() {super();} public IDiscordStoreManager(Pointer pointer) {super(pointer);}
    public interface FetchSkusCallback extends GameSDKCallback { void fetchSkus(IDiscordStoreManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public FetchSkusCallback FetchSkus;

    public interface CountSkusCallback extends GameSDKCallback { void countSkus(IDiscordStoreManager manager, IntByReference count); }
    public CountSkusCallback CountSkus;

    public interface GetSkuCallback extends GameSDKCallback { EDiscordResult getSku(IDiscordStoreManager manager, DiscordSnowflake skuId, DiscordSku sku); }
    public GetSkuCallback GetSku;

    public interface GetSkuAtCallback extends GameSDKCallback { EDiscordResult getSkuAt(IDiscordStoreManager manager, Int32 index, DiscordSku sku); }
    public GetSkuAtCallback GetSkuAt;

    public interface FetchEntitlementsCallback extends GameSDKCallback { void fetchEntitlements(IDiscordStoreManager manager, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public FetchEntitlementsCallback FetchEntitlements;

    public interface CountEntitlementsCallback extends GameSDKCallback { void countEntitlements(IDiscordStoreManager manager, IntByReference count); }
    public CountEntitlementsCallback CountEntitlements;

    public interface GetEntitlementCallback extends GameSDKCallback { EDiscordResult getEntitlement(IDiscordStoreManager manager, DiscordSnowflake entitlementId, DiscordEntitlement entitlement); }
    public GetEntitlementCallback GetEntitlement;

    public interface GetEntitlementAtCallback extends GameSDKCallback { EDiscordResult getEntitlementAt(IDiscordStoreManager manager, Int32 index, DiscordEntitlement entitlement); }
    public GetEntitlementAtCallback GetEntitlementAt;

    public interface HasSkuEntitlementCallback extends GameSDKCallback { EDiscordResult hasSkuEntitlement(IDiscordStoreManager manager, DiscordSnowflake skuId, ByteByReference hasEntitlement); } // hasEntitlement bool ptr
    public HasSkuEntitlementCallback HasSkuEntitlement;

    public interface StartPurchaseCallback extends GameSDKCallback { void startPurchase(IDiscordStoreManager manager, DiscordSnowflake skuId, Pointer callbackData, NativeGameSDK.DiscordCallback callback); }
    public StartPurchaseCallback StartPurchase;



    public static class ByReference extends IDiscordStoreManager implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends IDiscordStoreManager implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override protected List getFieldOrder() { return Arrays.asList("FetchSkus", "CountSkus", "GetSku", "GetSkuAt", "FetchEntitlements", "CountEntitlements", "GetEntitlement", "GetEntitlementAt", "HasSkuEntitlement", "StartPurchase"); }
}
